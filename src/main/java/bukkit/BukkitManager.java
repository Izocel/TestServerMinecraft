package bukkit;

import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import commands.bukkit.ConfirmLinkCmd;
import commands.bukkit.HyperLinksCmd;
import dao.DaoManager;
import events.bukkit.OnPlayerJoin;
import events.bukkit.OnPlayerLogin;
import events.bukkit.OnServerLoad;
import io.sentry.ISpan;
import io.sentry.SpanStatus;
import locals.LocalManager;
import main.WhitelistDmc;
import models.BedrockData;
import models.JavaData;
import services.api.PlayerDbApi;
import services.sentry.SentryService;

public class BukkitManager {
    private WhitelistDmc plugin;
    private Logger logger;

    public BukkitManager(WhitelistDmc plugin) {
        this.logger = Logger.getLogger("WDMC:" + this.getClass().getSimpleName());
        ISpan process = plugin.getSentryService().findWithuniqueName("onEnable")
                .startChild("BukkitManager");

        this.plugin = plugin;
        this.registerEvents(plugin);
        this.registerCommands(plugin);

        process.setStatus(SpanStatus.OK);
        process.finish();
    }

    public Server getServer() {
        return Bukkit.getServer();
    }

    public String getServerInfoString(String lang) {
        final String ip = Bukkit.getServer().getIp();
        final String version = Bukkit.getServer().getVersion();
        final String description = Bukkit.getServer().getMotd();
        final GameMode gameMode = Bukkit.getServer().getDefaultGameMode();
        final boolean onlineMode = Bukkit.getServer().getOnlineMode();
        final boolean usingWhiteList = Bukkit.getServer().hasWhitelist();
        final boolean forceWhitelist = Bukkit.getServer().isWhitelistEnforced();

        final String portJ = this.plugin.getConfigManager().get("portJava", "???");
        final String portB = this.plugin.getConfigManager().get("portBedrock", "???");
        final String javaIp = this.plugin.getConfigManager().get("javaIp", "???");
        final String bedrockIp = this.plugin.getConfigManager().get("bedrockIp", "???");

        final LocalManager LOCAL = WhitelistDmc.LOCALES;
        final String portField = LOCAL.translateBy("PORT", lang);
        final String versionField = LOCAL.translateBy("VERSION", lang);
        final String onlineField = LOCAL.translateBy("ONLINE_MODE", lang);
        final String whitelistField = LOCAL.translateBy("WHITELISTED", lang);
        final String defaultGModefield = LOCAL.translateBy("DEFAULT_GAMEMOD", lang);
        final String descField = LOCAL.translateBy("DESCRIPTION", lang);
        final String WORD_YES = LOCAL.translateBy("WORD_YES", lang);
        final String WORD_NO = LOCAL.translateBy("WORD_NO", lang);

        final String onlineStr = onlineMode ? "`" + WORD_YES + "`" : "`" + WORD_NO + "`";
        final String fwStr = forceWhitelist ? "`" + WORD_YES + "`" : "`" + WORD_NO + "`";

        StringBuilder sb = new StringBuilder();
        sb.append("\n\tJava Ip: `" + javaIp + "`");
        sb.append("\n\tBedrock Ip: `" + bedrockIp + "`");
        sb.append("\n\t" + portField + " Java: `" + portJ + "`");
        sb.append("\n\t" + portField + " Bedrock: `" + portB + "`");
        sb.append("\n\t" + versionField + " : `" + version + "`");
        sb.append("\n\t" + onlineField + " : `" + onlineStr + "`");
        sb.append("\n\t" + whitelistField + " : `" + fwStr + "`");
        sb.append("\n\t" + defaultGModefield + " : `" + gameMode.name() + "`");
        sb.append("\n\t" + descField + " : `" + description + "`");

        return sb.toString();
    }

    private void registerEvents(WhitelistDmc plugin) {
        ISpan process = plugin.getSentryService().findWithuniqueName("onEnable")
                .startChild("BukkitManager.registerEvents");
        try {
            Bukkit.getPluginManager().registerEvents(new OnPlayerLogin(plugin), plugin);
            Bukkit.getPluginManager().registerEvents(new OnPlayerJoin(plugin), plugin);
            Bukkit.getPluginManager().registerEvents(new OnServerLoad(plugin), plugin);
        } catch (Exception e) {
            SentryService.captureEx(e);
        }

        process.setStatus(SpanStatus.OK);
        process.finish();
    }

    private void registerCommands(WhitelistDmc plugin) {
        ISpan process = plugin.getSentryService().findWithuniqueName("onEnable")
                .startChild("BukkitManager.registerCommands");

        final String linkCmd = this.plugin.getConfigManager().get("confirmLinkCmdName", "w-link");

        try {
            this.plugin.getCommand(linkCmd).setExecutor(new ConfirmLinkCmd(this.plugin, linkCmd));
            this.plugin.getCommand("w-hyperlinks").setExecutor(new HyperLinksCmd(this.plugin, "w-hyperlinks"));
        } catch (Exception e) {
            SentryService.captureEx(e);
        }

        process.setStatus(SpanStatus.OK);
        process.finish();
    }

    public boolean banPlayer(String uuid) {
        try {
            final UUID UUID = java.util.UUID.fromString(uuid);
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID);
            plugin.deletePlayerRegistration(UUID);

            if (player != null) {
                player.setWhitelisted(false);
                kickPlayer(uuid);
            }

            return getServer().getPlayer(UUID) == null;
        } catch (Exception e) {
            SentryService.captureEx(e);
            return false;
        }

    }

    public boolean kickPlayer(String uuid) {
        try {
            final UUID UUID = java.util.UUID.fromString(uuid);
            Player onlinePlayer = getServer().getPlayer(UUID);
            if (onlinePlayer == null) {
                return true;
            }

            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                public void run() {
                    onlinePlayer.kickPlayer("§lVous ne faites plus partie de l'aventure...\n");
                }
            });

            return getServer().getPlayer(UUID) == null;

        } catch (Exception e) {
            SentryService.captureEx(e);
            return false;
        }
    }

    public boolean sanitizeAnKickPlayer(String uuid) {
        try {
            if (uuid == null) {
                return true;
            }

            final UUID UUID = java.util.UUID.fromString(uuid);
            if (plugin.deletePlayerRegistration(UUID)) {
                return kickPlayer(uuid);
            }

            return true;

        } catch (Exception e) {
            SentryService.captureEx(e);
            return false;
        }
    }

    public boolean sanitizeAndBanPlayer(String uuid) {
        try {
            if (uuid == null) {
                return true;
            }

            final UUID UUID = java.util.UUID.fromString(uuid);
            if (plugin.deleteAllPlayerData(UUID)) {
                return banPlayer(uuid);
            }

            return true;

        } catch (Exception e) {
            SentryService.captureEx(e);
            return false;
        }
    }

    public boolean setPlayerAsAllowed(Integer userId, String msgId, boolean allowed, String moderatorId, String uuid,
            boolean confirmed, String pseudo) {
        final JavaData javaData = DaoManager.getJavaDataDao().findWithUuid(uuid);
        final BedrockData bedData = DaoManager.getBedrockDataDao().findWithUuid(uuid);

        if (javaData != null && javaData.isAllowed()) {
            javaData.setAsAllowed(msgId, allowed, moderatorId);
            return true;
        }

        else if (bedData != null && bedData.isAllowed()) {
            bedData.setAsAllowed(msgId, allowed, moderatorId);
            return true;
        }

        else {
            final String foundJava = PlayerDbApi.getMinecraftUUID(pseudo);
            final String foundXbox = PlayerDbApi.getXboxUUID(pseudo);

            if (foundJava != null && foundJava.equals(uuid)) {
                JavaData data = new JavaData();
                data.setMcName(pseudo);
                if (allowed)
                    data.setAcceptedBy(moderatorId);
                else
                    data.setRevokedBy(moderatorId);

                data.setUUID(uuid);
                data.setUserId(userId);
                data.setAsAllowed(msgId, allowed, moderatorId);
                data.setAsConfirmed(confirmed);
                data.save(DaoManager.getJavaDataDao());
                return true;
            }

            else if (foundXbox != null && foundXbox.equals(uuid)) {
                BedrockData data = new BedrockData();
                data.setMcName(pseudo);
                if (allowed)
                    data.setAcceptedBy(moderatorId);
                else
                    data.setRevokedBy(moderatorId);

                data.setUUID(uuid);
                data.setUserId(userId);
                data.setAsAllowed(msgId, allowed, moderatorId);
                data.setAsConfirmed(confirmed);
                data.save(DaoManager.getBedrockDataDao());
                return true;
            }
        }

        logger.warning("Could not find any allowed player with UUID: " + uuid);
        return false;
    }

    public void setPlayerAsConfirmed(String uuid) {
        try {
            final JavaData javaData = DaoManager.getJavaDataDao().findWithUuid(uuid);
            final BedrockData bedData = DaoManager.getBedrockDataDao().findWithUuid(uuid);

            if (javaData != null && javaData.isAllowed()) {
                javaData.setAsConfirmed(true);
                javaData.save(DaoManager.getJavaDataDao());
            }

            else if (bedData != null && bedData.isAllowed()) {
                bedData.setAsConfirmed(true);
                bedData.save(DaoManager.getBedrockDataDao());
            }

            else {
                logger.warning("Could not find any allowed player with UUID: " + uuid);
            }

        } catch (Exception e) {
            SentryService.captureEx(e);
        }
    }

    public Object getPlayerData(String uuid) {
        final JavaData javaData = DaoManager.getJavaDataDao().findWithUuid(uuid);
        final BedrockData bedData = DaoManager.getBedrockDataDao().findWithUuid(uuid);
        return javaData != null ? javaData : bedData != null ? bedData : null;
    }

    public String getAvatarUrl(String uuid, String pxSize) {
        try {
            final JavaData javaData = DaoManager.getJavaDataDao().findWithUuid(uuid);
            final BedrockData bedData = DaoManager.getBedrockDataDao().findWithUuid(uuid);

            String type = null;
            if (javaData != null) {
                type = "Java";
            }

            else if (bedData != null) {
                type = "Bedrock";
            }

            return type == "Bedrock"
                    ? "https://api.tydiumcraft.net/v1/players/skin?uuid=" + uuid + "&size=" + pxSize
                    : type == "Java"
                            ? "https://mc-heads.net/body/" + uuid + "/" + pxSize
                            : "https://mc-heads.net/body/08673fd1-1196-43be-bc8b-e93fd2dee36d/" + pxSize;

        } catch (Exception e) {
            SentryService.captureEx(e);
            return "https://mc-heads.net/body/08673fd1-1196-43be-bc8b-e93fd2dee36d/";
        }
    }
}
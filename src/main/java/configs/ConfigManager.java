package configs;

import java.util.HashMap;

public final class ConfigManager {

    /////////////////////////// EDIT THE PRIVATE VARS ONLY \\\\\\\\\\\\\\\\\\\\\\\\\\\

    private final String discordBotToken="MTAxNzk3MTY5MDM0MjQ1NzM1Ng.Gz1fRt.6zMP2hYuyaF-03LW0NPkibe3jCstAqvVzVLYlQ";

    private final String discordServerId="276931890735218689";
    private final String discordAdminChanelId="924682022704787466";
    private final String discordWhitelistChanelId="925583159964352643";
    private final String botLogChannelId="927283856053252106";
    private final String javaLogChannelId="927283856053252106";

    private final String discordAdminRoleId="293133215500075010";
    private final String discordModeratorRoleId="1021054284214833182";
    private final String discordDevRoleId="1021054460438523976";
    
    private final String mysqlHost="localhost";
    private final String mysqlPort="3306";
    private final String mysqlDb="whitelist_je";
    private final String mysqlUser="whitelist_je";
    private final String mysqlPass="@whitelist_je2022";
    private final String mysqlDefTable="users";
    private final String mysqlJdbcUrl="jdbc:mysql://"+ mysqlHost + ":" + mysqlPort + "/" + mysqlDb;
    private final String mysqlMaxConnection="5";

    private final String portJava="25565";
    private final String portBedrock="19132";
    private final String paperMcIp="server.minecraft.tumeniaises.ca";

    /////////////////////////// EDIT THE PRIVATE VARS ONLY \\\\\\\\\\\\\\\\\\\\\\\\\\\

    private HashMap<String, String> configs = new HashMap<>();

    public ConfigManager() {
        configs.put("discordBotToken", this.discordBotToken);
        configs.put("discordServerId", this.discordServerId);
        configs.put("botLogChannelId", this.botLogChannelId);
        configs.put("javaLogChannelId", this.javaLogChannelId);
        configs.put("discordAdminChanelId", this.discordAdminChanelId);
        configs.put("discordWhitelistChanelId", this.discordWhitelistChanelId);
        configs.put("discordAdminRoleId", this.discordAdminRoleId);
        configs.put("discordModeratorRoleId", this.discordModeratorRoleId);
        configs.put("discordDevRoleId", this.discordDevRoleId);
        configs.put("mysqlHost", this.mysqlHost);
        configs.put("mysqlPort", this.mysqlPort);
        configs.put("mysqlDb", this.mysqlDb);
        configs.put("mysqlUser", this.mysqlUser);
        configs.put("mysqlPass", this.mysqlPass);
        configs.put("mysqlDefTable", this.mysqlDefTable);
        configs.put("mysqlJdbcUrl", this.mysqlJdbcUrl);
        configs.put("mysqlMaxConnection", this.mysqlMaxConnection);
        configs.put("portJava", this.portJava);
        configs.put("portBedrock", this.portBedrock);
        configs.put("paperMcIp", this.paperMcIp);
    }

    public String get(String key, String defaultValue) {
        String result = this.configs.get(key);
        return result == null ? defaultValue : result;
    }

    public String get(String key) {
        return this.configs.get(key);
    }


}

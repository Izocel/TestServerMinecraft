package commands.discords;

import java.util.logging.Logger;

import io.sentry.ITransaction;
import io.sentry.Sentry;
import io.sentry.SpanStatus;
import locals.LocalManager;
import main.WhitelistJe;
import models.User;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import services.sentry.SentryService;

public abstract class BaseCmd extends ListenerAdapter {

    protected User user;
    protected Guild guild;
    protected Logger logger;
    protected Member member;
    protected ITransaction tx;
    protected WhitelistJe plugin;
    protected MessageChannel channel;
    protected SlashCommandEvent event;
    protected static LocalManager LOCAL = WhitelistJe.LOCALES;

    protected String childClassName;
    protected String cmdNameTradKey;
    protected String mainTransactionName;
    protected String mainOperationName;
    protected net.dv8tion.jda.api.entities.User eventUser;

    abstract void execute();

    protected BaseCmd(WhitelistJe plugin, String childClassName, String cmdNameTradKey, String mainTransactionName,
            String mainOperationName) {
        this.plugin = plugin;
        this.childClassName = childClassName;
        this.logger = Logger.getLogger("WJE:" + childClassName);
        this.cmdNameTradKey = cmdNameTradKey;
        this.mainTransactionName = mainTransactionName;
        this.mainOperationName = mainOperationName;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (!this.isValidToContinue(event)) {
            return;
        }

        this.user = null;
        this.event = event;
        this.guild = event.getGuild();
        this.member = event.getMember();
        this.eventUser = event.getUser();
        this.channel = event.getChannel();
        this.setWjeUser();

        ITransaction trx = Sentry.startTransaction(this.mainTransactionName, this.mainOperationName);
        trx.setData("CommandClass", childClassName);
        this.tx = trx;

        try {
            this.execute();
        } catch (Exception e) {
            final String reply = LOCAL.translate("CMD_ERROR") + ": " + LOCAL.translate("CONTACT_ADMNIN");

            event.reply(reply).setEphemeral(true).submit(true);
            tx.setData("error-state", "error");
            tx.finish(SpanStatus.INTERNAL_ERROR);
            SentryService.captureEx(e);
        }

        if (!tx.isFinished()) {
            tx.setData("finalState", "unkwown");
            tx.finish(SpanStatus.UNKNOWN);
        }

    }

    protected final boolean isValidToContinue(SlashCommandEvent event) {
        return !event.isAcknowledged()
                && LOCAL.setCheckEventLocal(event.getName(), cmdNameTradKey);
    }

    protected final boolean isValidButtonToContinue(ButtonClickEvent event) {
        return !event.isAcknowledged();
    }

    protected final void setWjeUser() {
        if (member != null)
            this.user = User.getFromMember(member);
    }

    protected final void sendMsgToUser(String msg) {
        if (eventUser == null) {
            logger.warning("Undefined User for cmd event");
            return;
        }

        this.plugin.getDiscordManager().jda.openPrivateChannelById(eventUser.getId()).queue(channel -> {
            channel.sendMessage(msg).submit(true);
        });
    }

    protected final String translateForUser(String key) {
        if (user == null) {
            logger.warning("Undefined User for translate event");
            return LOCAL.translate(key);
        }

        return LOCAL.translateBy(key, this.user.getLang());
    }

    protected final void submitReply(String msg) {
        this.event.reply(msg).submit(true);
    }

    protected final Boolean getBoolParam(String paramTradKey) {
        final OptionMapping option = event.getOption(LOCAL.translate(paramTradKey));
        return option != null ? option.getAsBoolean() : null;
    }

    protected final String getStringParam(String paramTradKey) {
        final OptionMapping option = event.getOption(LOCAL.translate(paramTradKey));
        return option != null ? option.getAsString() : null;
    }

    protected final Long getLongParam(String paramTradKey) {
        final OptionMapping option = event.getOption(LOCAL.translate(paramTradKey));
        return option != null ? option.getAsLong() : null;
    }

    protected final Double getDoubleParam(String paramTradKey) {
        final OptionMapping option = event.getOption(LOCAL.translate(paramTradKey));
        return option != null ? option.getAsDouble() : null;
    }

}

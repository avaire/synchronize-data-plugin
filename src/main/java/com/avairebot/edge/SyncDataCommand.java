package com.avairebot.edge;

import com.avairebot.Constants;
import com.avairebot.commands.Category;
import com.avairebot.commands.CategoryHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.controllers.PlayerController;
import com.avairebot.factories.MessageFactory;
import com.avairebot.language.I18n;
import com.avairebot.utilities.CacheUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SyncDataCommand extends Command {

    private static final LoadingCache<Long, Boolean> cache = CacheBuilder.newBuilder()
        .expireAfterWrite(2, TimeUnit.MINUTES)
        .build(new CacheLoader<Long, Boolean>() {
            @Override
            public Boolean load(@NotNull Long aLong) throws Exception {
                return false;
            }
        });

    private final CuttingEdgeSync plugin;

    SyncDataCommand(CuttingEdgeSync plugin) {
        super(plugin.getAvaire(), false);

        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Sync Data Command";
    }

    @Override
    public String getDescription() {
        return "Syncs the data from the main bot to the cutting edge bot, this will overwrite the existing server and XP data with the data from the main bot.";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("sync-data");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "throttle:guild,1,60",
            "requireOne:user,general.administrator"
        );
    }

    @Override
    public Category getCategory() {
        return CategoryHandler.fromLazyName("Cutting Edge");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        Boolean value = CacheUtil.getUncheckedUnwrapped(cache, context.getGuild().getIdLong());

        if (!value) {
            context.makeInfo("Syncing data with the official bot means any data you have with this bot will be lost, if you're sure you want to continue, use this command again within the next two minutes to start the synchronization progress.").queue();

            cache.put(context.getGuild().getIdLong(), true);

            return false;
        }

        context.makeInfo(buildMessage(false, false)).queue(this::handleGuildSync);

        return true;
    }

    private void handleGuildSync(Message message) {
        try {
            plugin.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", message.getGuild().getIdLong())
                .delete();

            plugin.getDatabase().queryInsert(String.format("INSERT INTO guilds SELECT * FROM %s.guilds WHERE `id` = '%s'",
                plugin.getConfig().getString("database.database"),
                message.getGuild().getIdLong()
            ));

            message.editMessage(MessageFactory.makeInfo(message, buildMessage(true, false)).buildEmbed())
                .queue(this::handlePlayerXpSync);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handlePlayerXpSync(Message message) {
        try {
            plugin.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                .where("guild_id", message.getGuild().getIdLong())
                .delete();

            plugin.getDatabase().queryInsert(String.format("INSERT INTO experiences SELECT * FROM %s.experiences WHERE `guild_id` = '%s'",
                plugin.getConfig().getString("database.database"),
                message.getGuild().getIdLong()
            ));

            message.editMessage(MessageFactory.makeInfo(message, buildMessage(true, true)).buildEmbed())
                .queue(this::handleClearCache);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleClearCache(Message message) {
        GuildController.forgetCache(message.getGuild().getIdLong());

        for (Member user : message.getGuild().getMembers()) {
            PlayerController.cache.invalidate(message.getGuild().getId() + ":" + user.getUser().getId());
        }
    }

    @SuppressWarnings("SameParameterValue")
    private String buildMessage(boolean guildSync, boolean xpSync) {
        String message = I18n.format("**Syncing guild data:** {0}\n**Syncing player experience data:** {1}",
            guildSync ? ":ballot_box_with_check:" : "\uD83D\uDD18",
            xpSync ? ":ballot_box_with_check:" : "\uD83D\uDD18"
        );

        message = (guildSync && xpSync
            ? "~~Starting synchronized process...\n\n~~"
            : "Starting synchronized process...\n\n"
        ) + message;

        if (guildSync && xpSync) {
            message += "\n\nDone! The synchronized process should now be finished!";
        }

        return message;
    }
}

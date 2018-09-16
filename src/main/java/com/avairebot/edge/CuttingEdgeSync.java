package com.avairebot.edge;

import com.avairebot.plugin.JavaPlugin;
import com.avairebot.shared.DiscordConstants;

public class CuttingEdgeSync extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        registerCategory("Cutting Edge", getAvaire().getConfig().getString(
            "default-prefix", DiscordConstants.DEFAULT_COMMAND_PREFIX)
        );

        registerCommand(new SyncDataCommand(this));
    }
}

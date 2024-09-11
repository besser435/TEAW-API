package me.besser;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import static java.util.logging.Level.*;
import static me.besser.TAPILogger.log;

public final class TAPI extends JavaPlugin {
    @Override
    public void onEnable() {
        TAPILogger.initialize(this);

        boolean isEnabledInConfig = getConfig().getBoolean("tapi.enable", true);
        if (!isEnabledInConfig) {
            log(WARNING, "TAPI is disabled in config.yml and will not be fully initialized");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();

        PlayerTracker playerTracker = new PlayerTracker();
        getServer().getPluginManager().registerEvents(playerTracker, this);

        new PlayerDataServer(this, playerTracker);

        log(INFO, ChatColor.AQUA + "TEAW API " + ChatColor.GOLD + "v" + getDescription().getVersion() + ChatColor.RESET + " started!");
    }

    @Override
    public void onDisable() {
        log(INFO, ChatColor.AQUA + "TEAW API " + ChatColor.GOLD + "v" + getDescription().getVersion() + ChatColor.RESET + " stopped!");
    }
}
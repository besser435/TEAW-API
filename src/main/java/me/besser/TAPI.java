package me.besser;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

import static java.util.logging.Level.*;
import static me.besser.TAPILogger.log;

public final class TAPI extends JavaPlugin {
    private static Economy econ = null;

    @Override
    public void onEnable() {
        TAPILogger.initialize(this);

        boolean isEnabledInConfig = getConfig().getBoolean("tapi.enable", true);
        if (!isEnabledInConfig) {
            log(WARNING, "TAPI is disabled in config.yml and will not be fully initialized");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Also kind of relies on essentials, try and see if that's needed too.
        // This might not be needed due to the dependency check in plugin.yml
        if (!setupEconomy()) {
            log(SEVERE, "Vault is not found! This may cause bugs when responding to API requests");
            return;
        }

        saveDefaultConfig();

        PlayerTracker playerTracker = new PlayerTracker();
        getServer().getPluginManager().registerEvents(playerTracker, this);

        new PlayerDataServer(this, playerTracker);

        log(INFO, ChatColor.AQUA + "TEAW API " + ChatColor.GOLD + "v" + getDescription().getVersion() + ChatColor.RESET + " started!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    @Override
    public void onDisable() {
        log(INFO, ChatColor.AQUA + "TEAW API " + ChatColor.GOLD + "v" + getDescription().getVersion() + ChatColor.RESET + " stopped!");
    }
}
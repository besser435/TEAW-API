package me.besser;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

import static me.besser.DIETLogger.*;

public final class TAPI extends JavaPlugin {
    private static Economy econ = null;

    @Override
    public void onEnable() {
        // TODO: clean up private variables and constructors
        DIETLogger.initialize(this);

        boolean isEnabledInConfig = getConfig().getBoolean("tapi.enable", true);
        if (!isEnabledInConfig) {
            log(WARNING, "TAPI is disabled in config.yml and will not start");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // TODO: clean up checks or required plugins. we only have like 2, even though we rely on several plugins.
        if (!setupEconomy()) {
            log(SEVERE, "Vault is not found! This may cause bugs when responding to API requests");
            return;
        }

        saveDefaultConfig();


        // Player info
        PlayerTracker playerTracker = new PlayerTracker(this);
        getServer().getPluginManager().registerEvents(playerTracker, this);

        // Sever info
        ServerInfoTracker serverInfoTracker = new ServerInfoTracker(this);

        // API server
        new EndpointServer(this, playerTracker);

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
        return true;
    }

    public static Economy getEconomy() {
        return econ;
    }

    @Override
    public void onDisable() {
        log(INFO, ChatColor.AQUA + "TEAW API " + ChatColor.GOLD + "v" + getDescription().getVersion() + ChatColor.RESET + " stopped!");
    }
}
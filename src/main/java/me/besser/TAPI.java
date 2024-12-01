package me.besser;

import me.lucko.spark.api.Spark;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.time.Instant;

import static me.besser.DIETLogger.*;

public final class TAPI extends JavaPlugin {
    private static Economy econ = null;
    private static Spark spark = null;


    @Override
    public void onEnable() {
        DIETLogger.initialize(this);

        saveDefaultConfig();

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

        // TODO: should also move essentials econ constructor to a thing like this
        RegisteredServiceProvider<Spark> provider = Bukkit.getServicesManager().getRegistration(Spark.class);
        if (provider != null) {
            spark = provider.getProvider();
        }

        // Create shared tracker objects
        // TODO: clean up how we pass the plugin instance
        PlayerTracker playerTracker = new PlayerTracker(this);
        ChatTracker chatTracker = new ChatTracker();
        TownyTracker townyTracker = new TownyTracker(); // TODO: Should move Essentials constructor to this class
        PlayerStatTracker playerStatTracker = new PlayerStatTracker();
        ServerInfoTracker serverInfoTracker = new ServerInfoTracker(this, spark);

        // Register events for trackers
        getServer().getPluginManager().registerEvents(playerTracker, this);

        // Initialize the API server and pass shared objects
        // TODO: is there a better way to pass objects?
        EndpointServer endpointServer = new EndpointServer(this, playerTracker, chatTracker, townyTracker, playerStatTracker, serverInfoTracker);


        log(INFO, ChatColor.AQUA + "TEAW API " + ChatColor.GOLD + "v" + getDescription().getVersion() + ChatColor.RESET + " started!");

        // TODO: does the method in the tracker class need the synchronized keyword?
        chatTracker.addMessage(new ChatTracker.chatMessage(
                "SERVER", "TEAW started!", Instant.now().toEpochMilli(), ChatTracker.msgType.status)
        );
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

        // No chatTracker shutdown message, as that message would be lost on the restart,
        // and is unlikely to be fetched by the API before the server goes down.
    }
}
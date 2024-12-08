package me.besser;

import com.earth2me.essentials.Essentials;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.time.Instant;

import static me.besser.DIETLogger.*;

public final class TAPI extends JavaPlugin {
    private static Economy econ = null;
    private static Essentials essentials = null;


    @Override
    public void onEnable() {
        DIETLogger.initialize(this);

        saveDefaultConfig();

        boolean isEnabledInConfig = getConfig().getBoolean("tapi.enable", true);
        if (!isEnabledInConfig) {
            log(WARNING, "TAPI is disabled in config.yml and will not start.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!setupEconomy() || !setupEssentials()) {    // TODO: add other dependencies
            log(SEVERE, "Required dependencies are missing. TAPI will not start.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }


        // Create shared tracker objects
        PlayerTracker playerTracker = new PlayerTracker(this);
        ChatTracker chatTracker = new ChatTracker(this);
        TownyTracker townyTracker = new TownyTracker(getEssentials());
        PlayerStatTracker playerStatTracker = new PlayerStatTracker();
        ServerInfoTracker serverInfoTracker = new ServerInfoTracker(this);

        // Register events for trackers
        getServer().getPluginManager().registerEvents(playerTracker, this);
        getServer().getPluginManager().registerEvents(chatTracker, this);

        // Initialize the API server and pass shared objects
        EndpointServer endpointServer = new EndpointServer(
            this,
            playerTracker,
            chatTracker,
            townyTracker,
            playerStatTracker,
            serverInfoTracker
        );


        log(INFO, ChatColor.AQUA + "TEAW API " + ChatColor.GOLD + "v" + getDescription().getVersion() + ChatColor.RESET + " started!");

        chatTracker.addMessage(new ChatTracker.chatMessage(
            "SERVER", "TEAW has started!", Instant.now().toEpochMilli(), ChatTracker.msgType.status)
        );
    }


    private boolean setupEconomy() {
        Plugin vaultPlugin = getServer().getPluginManager().getPlugin("Vault");
        if (vaultPlugin == null) {
            log(SEVERE, "Vault plugin not found!");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            log(SEVERE, "Vault does not have a valid economy provider!");
            return false;
        }

        econ = rsp.getProvider();
        return true;
    }

    private boolean setupEssentials() {
        Plugin essentialsPlugin = getServer().getPluginManager().getPlugin("Essentials");
        if (essentialsPlugin instanceof Essentials) {
            essentials = (Essentials) essentialsPlugin;
            return true;
        } else {
            log(SEVERE, "Essentials plugin not found!");
            return false;
        }
    }


    public static Economy getEconomy() {
        return econ;
    }

    public static Essentials getEssentials() {
        return essentials;
    }


    @Override
    public void onDisable() {
        log(INFO, ChatColor.AQUA + "TEAW API " + ChatColor.GOLD + "v" + getDescription().getVersion() + ChatColor.RESET + " stopped!");

        // No chatTracker shutdown message, as that message would be lost on the restart,
        // and is unlikely to be fetched by the API before the server goes down.
    }
}
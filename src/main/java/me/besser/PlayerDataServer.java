package me.besser;

import static spark.Spark.*;
import com.google.gson.Gson;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import static java.util.logging.Level.*;
import static me.besser.TAPILogger.log;

public class PlayerDataServer {
    private final JavaPlugin plugin;
    private final PlayerTracker playerTracker;
    //private final PlayerStatTracker playerStatTracker = new PlayerStatTracker();

    private final Gson gson = new Gson();

    public PlayerDataServer(JavaPlugin plugin, PlayerTracker playerTracker) {
        this.plugin = plugin;
        this.playerTracker = playerTracker;
        initRoutes();
        log(INFO, "Initialized player data server");
    }

    private void initRoutes() {
        // Should maybe add a short TTL cache
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        int serverPort = config.getInt("server.port", 1850);

        port(serverPort);

        get("/api/online_players", (req, res) -> {
            res.type("application/json");

            // TODO: add catch any errors, return 500 if they occur
            return gson.toJson(playerTracker.getOnlinePlayersInfo());
        });

// Not enabled yet, may be added in a future update
// requires the player to be online to get all stats that aren't in the general category
//        get("/api/full_player_stats/:username", (req, res) -> {  // Should be a UUID
//            String username = req.params("username");
//            res.type("application/json");
//
//            Player player = Bukkit.getPlayer(username);
//            if (player == null) {
//                res.status(404);
//                return gson.toJson("Player not found or may be offline");
//            }
//
//            return gson.toJson(playerStatTracker.getPlayerStatistics(player));
//        });
    }
}

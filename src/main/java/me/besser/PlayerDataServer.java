package me.besser;

import static spark.Spark.*;
import com.google.gson.Gson;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

import static java.util.logging.Level.*;
import static me.besser.TAPILogger.log;

public class PlayerDataServer {
    private final JavaPlugin plugin;
    private final PlayerTracker playerTracker;
    private final TownyTracker townyTracker;

    //private final PlayerStatTracker playerStatTracker = new PlayerStatTracker();
    private final Gson gson = new Gson();

    // TODO: Spark is deprecated. Transition to Javalin

    public PlayerDataServer(JavaPlugin plugin, PlayerTracker playerTracker) {
        this.plugin = plugin;
        this.playerTracker = playerTracker;
        this.townyTracker = new TownyTracker();
        initRoutes();
        log(INFO, "Initialized player data server");
    }

    private void initRoutes() {
        // Should maybe add a short TTL cache
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        int serverPort = config.getInt("server.port", 1850);

        port(serverPort);

        // TODO: add catch any errors, return 500 if they occur
        get("/api/online_players", (request, response) -> {
            response.type("application/json");

            Map<String, Object> onlinePlayers = new HashMap<>();
            onlinePlayers.put("online_players", playerTracker.getOnlinePlayersInfo());

            return gson.toJson(onlinePlayers);
        });

        get("/api/towny", (request, response) -> {
            response.type("application/json");

            Map<String, Object> townyData = new HashMap<>();
            townyData.put("towns", townyTracker.getTownData());
            townyData.put("nations", townyTracker.getNationData());

            return gson.toJson(townyData);
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

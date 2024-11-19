package me.besser;

import static spark.Spark.*;
import com.google.gson.Gson;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.besser.DIETLogger.*;

/* TODO: rename class, it serves more than just player data. */
public class PlayerDataServer {
    // TODO: clean up private variables and constructors
    private final JavaPlugin plugin;
    private final PlayerTracker playerTracker;
    private final TownyTracker townyTracker;
    private final ChatTracker chatTracker = new ChatTracker();

    private final PlayerStatTracker playerStatTracker = new PlayerStatTracker();
    private final ServerInfoTracker serverInfoTracker;

    private final Gson gson = new Gson();


    public PlayerDataServer(JavaPlugin plugin, PlayerTracker playerTracker) {
        this.plugin = plugin;
        this.playerTracker = playerTracker;
        this.townyTracker = new TownyTracker();
        this.serverInfoTracker = new ServerInfoTracker((TAPI) plugin);

        initRoutes();
        log(INFO, "Initialized player data server");
    }

    private void initRoutes() {
        // Should maybe add a short TTL cache
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        int serverPort = config.getInt("server.port", 1850);

        port(serverPort);
        // TODO: Spark is deprecated. Transition to Javalin


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

        get("/api/full_player_stats/:uuid", (request, response) -> {
            // Requires the player to be online

            String uuid = request.params("uuid");
            response.type("application/json");

            Player player = Bukkit.getPlayer(UUID.fromString(uuid));
            if (player == null) {
                response.status(404);
                return gson.toJson("Player not found or may be offline.");// Offline player route: /api/offline_player_stats/:uuid");
            }

            return gson.toJson(playerStatTracker.getPlayerStatistics(player));
        });

        get("/api/server_info", (request, response) -> {
            response.type("application/json");

            Map<String, Object> serverInfo = serverInfoTracker.getServerInfo();
            return gson.toJson(serverInfo);
        });

        get("/api/chat_history", (request, response) -> {
            // TODO: make it so we can query for messages only after a certain timestamp
            response.type("application/json");

            return gson.toJson(chatTracker.getLastMessages());
        });



        // TODO: add endpoint for info about the API. Include version number and build time.
//        get("/api/meta", (request, response) -> {
//            response.type("application/json");
//
//
//
//            return gson.toJson(0);
//        });

        // BUG, when the player is offline this takes ~8s to respond, and returns all data, not just the general stats. this wasn't the case before...
//        get("/api/offline_player_stats/:uuid", (req, res) -> {
//            String uuid = req.params("uuid");
//            res.type("application/json");
//
//            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
//            if (player == null) {
//                res.status(404);
//                return gson.toJson("Player not found");
//            }
//
//            return gson.toJson(playerStatTracker.getPlayerStatistics(player));
//        });
    }
}

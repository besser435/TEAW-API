package me.besser;

import static spark.Spark.*;
import com.google.gson.Gson;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.besser.DIETLogger.*;

public class EndpointServer {
    // TODO: clean up private variables and constructors.
    private final JavaPlugin plugin;
    private final PlayerTracker playerTracker;
    private final TownyTracker townyTracker;
    private final ServerInfoTracker serverInfoTracker;
    private final ChatTracker chatTracker = new ChatTracker();
    private final PlayerStatTracker playerStatTracker = new PlayerStatTracker();

    private final Gson gson = new Gson();

    public EndpointServer(JavaPlugin plugin, PlayerTracker playerTracker) {
        // TODO: Why the hell are these created here and not in the main class
        this.plugin = plugin;
        this.playerTracker = playerTracker;
        this.townyTracker = new TownyTracker();
        this.serverInfoTracker = new ServerInfoTracker((TAPI) plugin);

        DiscordSRV.api.subscribe(chatTracker);

        initRoutes();
    }

    private void initRoutes() {
        // Should maybe add a short TTL cache
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        int serverPort = config.getInt("server.port", 1850);

        port(serverPort);
        // TODO: Spark is deprecated. Transition to Javalin

        get("/api/online_players", (request, response) -> {
            response.type("application/json");

            return gson.toJson(playerTracker.getOnlinePlayersInfo());
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
            // TODO: make it so the UUID param is like the time param in chat, and is not just "/uuid"

            String uuidParam = request.params("uuid");
            response.type("application/json");

            Player player = Bukkit.getPlayer(UUID.fromString(uuidParam));
            if (player == null) {
                response.status(404);
                return gson.toJson("Player not found or may be offline.");// Offline player route: /api/offline_player_stats/:uuid");
            }

            return gson.toJson(playerStatTracker.getPlayerStatistics(player));
        });

        get("/api/chat_history", (request, response) -> {
            response.type("application/json");

            String timeParam = request.queryParams("time");

            // Get the messages, or if provided, only the ones after a certain timestamp
            long timeFilter = 0;
            if (timeParam != null) {
                try {
                    timeFilter = Long.parseLong(timeParam);
                } catch (NumberFormatException e) {
                    response.status(400);
                    return gson.toJson("Invalid time format. Expected a Unix epoch in milliseconds.");
                }
            }

            long finalTimeFilter = timeFilter;
            return gson.toJson(chatTracker.getLastMessages().stream()
                    .filter(message -> message.timestamp() > finalTimeFilter)
                    .toList());
        });

        get("/api/server_info", (request, response) -> {
            response.type("application/json");

            Map<String, Object> serverInfo = serverInfoTracker.getServerInfo();
            return gson.toJson(serverInfo);
        });

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

package me.besser;

import github.scarsz.discordsrv.DiscordSRV;
import io.javalin.Javalin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.besser.DIETLogger.*;

public class EndpointServer {
    private final JavaPlugin plugin;
    private final PlayerTracker playerTracker;
    private final ChatTracker chatTracker;
    private final TownyTracker townyTracker;
    private final PlayerStatTracker playerStatTracker;
    private final ServerInfoTracker serverInfoTracker;
    private final PVPTracker PVPTracker;

    private Javalin app;

    public EndpointServer(
        JavaPlugin plugin, PlayerTracker playerTracker, ChatTracker chatTracker,
        TownyTracker townyTracker, PlayerStatTracker playerStatTracker,
        ServerInfoTracker serverInfoTracker, PVPTracker PVPTracker
    ){
        this.plugin = plugin;
        this.playerTracker = playerTracker;
        this.chatTracker = chatTracker;
        this.townyTracker = townyTracker;
        this.playerStatTracker = playerStatTracker;
        this.serverInfoTracker = serverInfoTracker;
        this.PVPTracker = PVPTracker;

        DiscordSRV.api.subscribe(chatTracker);

        initRoutes();
    }

    private void initRoutes() {
        // Should maybe add a short TTL cache in the event this becomes more widely used than for just one fetcher.
        FileConfiguration config = plugin.getConfig();
        int serverPort = config.getInt("server.port", 1850);

        app = Javalin.create(javalinConfig -> {
            javalinConfig.http.defaultContentType = "application/json";
            javalinConfig.showJavalinBanner = false;
        });

        // Endpoints
        app.get("/api/online_players", ctx -> {
            ctx.json(playerTracker.getOnlinePlayersInfo());
        });

        app.get("/api/towny", ctx -> {
            Map<String, Object> townyData = new HashMap<>();
            townyData.put("towns", townyTracker.getTownData());
            townyData.put("nations", townyTracker.getNationData());

            ctx.json(townyData);
        });

        app.get("/api/full_player_stats/{uuid}", ctx -> {
            String uuidParam = ctx.pathParam("uuid");

            try {
                UUID uuid = UUID.fromString(uuidParam);
                Player player = Bukkit.getPlayer(uuid);

                if (player == null || !player.isOnline()) {
                    ctx.status(404).json(Map.of(
                            "error", "Player not found or offline"
                    ));
                    return;
                }

                ctx.json(playerStatTracker.getPlayerStatistics(player));

            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of(
                        "error", "UUID malformed"
                ));
            }
        });

        app.get("/api/chat_history", ctx -> {
            long timeFilter = ctx.queryParamAsClass("time", Long.class) // Optional
                    .getOrDefault(0L);

            ctx.json(
                    chatTracker.getLastMessages().stream()
                            .filter(message -> message.timestamp() > timeFilter)
                            .toList()
            );
        });

        app.get("/api/server_info", ctx -> {
            ctx.json(serverInfoTracker.getServerInfo());
        });

        app.get("/api/kill_history", ctx -> {
            long timeFilter = ctx.queryParamAsClass("time", Long.class) // Optional
                    .getOrDefault(0L);

            ctx.json(
                    PVPTracker.getLastKills().stream()
                            .filter(kill -> kill.timestamp() > timeFilter)
                            .toList()
            );
        });

        // Errors
        app.error(404, ctx -> {
            ctx.json(Map.of("error", "Not found"));
        });

        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(500).json(Map.of(
                    "error", "Internal server error"
            ));
        });

        app.start(serverPort);
    }

    public void stop() {
        if (app != null) {
            app.stop();
            log(INFO, "Javalin server stopped");
        }
    }
}

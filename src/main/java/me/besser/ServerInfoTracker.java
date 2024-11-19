package me.besser;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static java.util.logging.Level.WARNING;
import static me.besser.DIETLogger.*;

public class ServerInfoTracker {
    private final TAPI plugin;

    public ServerInfoTracker(TAPI plugin) {
        this.plugin = plugin;
    }


    public Map<String, Object> getServerInfo() {    // TODO: there should be this (getServerInfo) and getWorldInfo.
        Map<String, Object> serverInfo = new HashMap<>();

        // World stuff
        World world = Bukkit.getWorlds().get(0);    // might cause issues if using BungeeCord


        // Online players
        // TODO: just add this to the PlayerTracker class, along with the max player count
        //int onlinePlayerCount = Bukkit.getOnlinePlayers().size();
        //serverInfo.put("online_players", onlinePlayerCount);


        // Add the stuff
        serverInfo.put("weather", getWorldWeather(world));
        serverInfo.put("world_time_ticks", world.getTime());
        serverInfo.put("world_time_24h", convertTicksTo24HourFormat(world.getTime()));

        serverInfo.put("tapi_version", getTAPIVersionAndBuildTime());
        serverInfo.put("server_version", Bukkit.getVersion());

        serverInfo.put("server_motd", ChatColor.stripColor(Bukkit.getMotd()));
        serverInfo.put("loaded_chunks", getTotalLoadedChunks());
        serverInfo.put("entities", getTotalEntities());

        return serverInfo;
    }

    private String getWorldWeather(World world) {
        boolean isThundering = world.isThundering();
        boolean hasStorm = world.hasStorm();

        if (isThundering) {
            return "Thunderstorms";
        } else if (hasStorm) {
            return "Rainy";
        } else {
            return "Clear";
        }
    }

    private int getTotalLoadedChunks() {
        return Bukkit.getWorlds().stream()
                .mapToInt(world -> world.getLoadedChunks().length)
                .sum();
    }

    private int getTotalEntities() {
        return Bukkit.getWorlds().stream()
                .mapToInt(world -> world.getEntities().size())
                .sum();
    }

    private String getTAPIVersionAndBuildTime() {
        String buildTime = "Unknown";
        String version = plugin.getDescription().getVersion();

        try {
            java.util.Properties properties = new java.util.Properties();
            properties.load(plugin.getResource("build.properties"));

            buildTime = properties.getProperty("build.timestamp", "Unknown");
        } catch (Exception e) {
            log(WARNING, "Could not load build timestamp");
        }

        return String.format("TAPI v%s, Build %s", version, buildTime); // not really meant to be machine-readable
    }

    private String convertTicksTo24HourFormat(long ticks) {
        long normalizedTicks = ticks % 24000;

        int hours = (int) ((normalizedTicks / 1000 + 6) % 24); // Minecraft day starts at 6am, hence + 6
        int minutes = (int) ((normalizedTicks % 1000) * 60 / 1000);

        // Format as HH:mm
        return String.format("%02d:%02d", hours, minutes);
    }

}

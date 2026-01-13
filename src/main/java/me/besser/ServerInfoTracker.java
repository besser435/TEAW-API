package me.besser;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.util.logging.Level.WARNING;
import static me.besser.DIETLogger.*;

public class ServerInfoTracker {
    private final TAPI plugin;
    private final String buildTimestamp;

    public ServerInfoTracker(TAPI plugin) {
        this.plugin = plugin;
        this.buildTimestamp = loadBuildTimestamp();
    }

    public Map<String, Object> getServerInfo() {
        Map<String, Object> serverInfo = new HashMap<>();

        World world = Bukkit.getWorlds().get(0);    // might cause issues if using BungeeCord. Should use getWorld({world name set in config})

        serverInfo.put("weather", getWorldWeather(world));
        serverInfo.put("world_time_ticks", world.getTime());
        serverInfo.put("world_time_24h", convertTicksTo24HourFormat(world.getTime()));
        serverInfo.put("day", world.getFullTime() / 24000);
        serverInfo.put("loaded_chunks", Arrays.stream(world.getLoadedChunks()).count());

        serverInfo.put("tapi_version", plugin.getDescription().getVersion());
        serverInfo.put("tapi_build", buildTimestamp);
        serverInfo.put("server_version", Bukkit.getVersion());
        serverInfo.put("system_time", Instant.now().toEpochMilli());

        return serverInfo;
    }

    private String getWorldWeather(World world) {
        if (world.isThundering()) return "Thunderstorms";
        if (world.hasStorm()) return "Rainy";
        return "Clear";
    }

    private String loadBuildTimestamp() {
        try {
            Properties properties = new Properties();
            if (plugin.getResource("build.properties") == null) {
                log(WARNING, "build.properties not found in plugin jar");
                return "Unknown";
            }

            properties.load(plugin.getResource("build.properties"));
            return properties.getProperty("build.timestamp", "unknown");
        } catch (Exception e) {
            log(WARNING, "Could not load build timestamp", e);
            return "unknown";
        }
    }

    private String convertTicksTo24HourFormat(long ticks) {
        long normalizedTicks = ticks % 24000;

        int hours = (int) ((normalizedTicks / 1000 + 6) % 24); // Minecraft day starts at 6am, hence + 6
        int minutes = (int) ((normalizedTicks % 1000) * 60 / 1000);

        // Format as HH:mm
        return String.format("%02d:%02d", hours, minutes);
    }

}

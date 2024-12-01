package me.besser;

import me.lucko.spark.api.Spark;
import me.lucko.spark.api.gc.GarbageCollector;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static java.util.logging.Level.WARNING;
import static me.besser.DIETLogger.*;

public class ServerInfoTracker {
    private final TAPI plugin;
    private final Spark spark;

    public ServerInfoTracker(TAPI plugin, Spark spark) {
        this.plugin = plugin;
        this.spark = spark;
    }


    public Map<String, Object> getServerInfo() {
        Map<String, Object> serverInfo = new HashMap<>();

        World world = Bukkit.getWorlds().get(0);    // might cause issues if using BungeeCord. Should use getWorld({world name})

        serverInfo.put("weather", getWorldWeather(world));
        serverInfo.put("world_time_ticks", world.getTime());
        serverInfo.put("world_time_24h", convertTicksTo24HourFormat(world.getTime()));
        serverInfo.put("day", world.getFullTime() / 24000);

        serverInfo.put("tapi_version", getTAPIVersionAndBuildTime());
        serverInfo.put("server_version", Bukkit.getVersion());
        serverInfo.put("system_time", Instant.now().toEpochMilli());
        serverInfo.put("tps", spark.tps());
        serverInfo.put("mspt", spark.mspt());


        Map<String, GarbageCollector> gc = spark.gc();
        double gcTime = 0;
        for (GarbageCollector collector : gc.values()) {
            String gcName = collector.name();
            long gcFrequency = collector.avgFrequency();
            gcTime = collector.avgTime();
        }
        serverInfo.put("gc_time", gcTime);

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

package me.besser;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerStatTracker {

    /** Will not return zero values. Player also needs to be online. */
    public Map<String, Object> getPlayerStatistics(Player player) {
        // The nested loops, redundant recalculations, and the 25,000 loop iterations may look slow,
        // but it only takes a few milliseconds to get a result so who cares :pray:
        Map<String, Object> stats = new HashMap<>();

        Map<String, Integer> generalStats = new HashMap<>();
        Map<String, Map<String, Integer>> itemStats = new HashMap<>();
        Map<String, Map<String, Integer>> mobStats = new HashMap<>();

        for (Statistic statistic : Statistic.values()) {
            try {
                switch (statistic.getType()) {
                    case UNTYPED:
                        int generalValue = player.getStatistic(statistic);
                        if (generalValue > 0) {
                            generalStats.put(statistic.name(), generalValue);
                        }
                        break;

                    case ITEM:
                        for (Material material : Material.values()) {
                            try {
                                int itemValue = player.getStatistic(statistic, material);
                                if (itemValue > 0) {
                                    itemStats.computeIfAbsent(statistic.name(), k -> new HashMap<>())
                                            .put(material.name(), itemValue);
                                }
                            } catch (IllegalArgumentException ignored) {}
                        }
                        break;

                    case ENTITY:
                        for (EntityType entityType : EntityType.values()) {
                            try {
                                int mobValue = player.getStatistic(statistic, entityType);
                                if (mobValue > 0) {
                                    mobStats.computeIfAbsent(statistic.name(), k -> new HashMap<>())
                                            .put(entityType.name(), mobValue);
                                }
                            } catch (IllegalArgumentException ignored) {}
                        }
                        break;

                    case BLOCK:
                        for (Material material : Material.values()) {
                            if (!material.isBlock()) continue;

                            try {
                                int blockValue = player.getStatistic(statistic, material);
                                if (blockValue > 0) {
                                    itemStats.computeIfAbsent(statistic.name(), k -> new HashMap<>())
                                            .put(material.name(), blockValue);
                                }
                            } catch (IllegalArgumentException ignored) {}
                        }
                        break;
                }
            } catch (IllegalArgumentException ignored) {}
        }

        stats.put("general", generalStats);
        stats.put("item", itemStats);
        stats.put("mob", mobStats);

        return stats;
    }
}

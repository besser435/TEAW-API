package me.besser.tapi.listeners;
import me.besser.tapi.TAPI;
import me.besser.tapi.database.InsertMethods;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.HashMap;
import java.util.Map;
public class StatTracker {

    // TODO:
    // Might be nice to have periodic updates, so online players can watch the
    // leaderboard go up as they fish for example. Right now they have to log out to see changes.
    @SubscribeEvent
    public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) syncStats(player);
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {   // Server shutdown doesn't fire PlayerLoggedOutEvent
        TAPI.LOGGER.info("Server stopping: Syncing all player stats...");
        event.getServer().getPlayerList().getPlayers().forEach(this::syncStats);
    }

    public void syncStats(ServerPlayer player) {
        ServerStatsCounter counter = player.getStats();
        Map<String, Map<String, Integer>> allStats = new HashMap<>();

        for (StatType<?> type : BuiltInRegistries.STAT_TYPE) {
            Identifier typeId = BuiltInRegistries.STAT_TYPE.getKey(type);
            if (typeId == null) continue;

            String category = typeId.toString();
            Map<String, Integer> categoryMap = new HashMap<>();

            for (Stat<?> stat : type) {
                int value = counter.getValue(stat);
                if (value > 0) {
                    String statKey = getStatKey(stat);
                    categoryMap.put(statKey, value);
                }
            }

            if (!categoryMap.isEmpty()) {
                allStats.put(category, categoryMap);
            }
        }
        InsertMethods.batchUpdateStats(player.getUUID(), allStats);
    }

    private <T> String getStatKey(Stat<T> stat) {
        T value = stat.getValue();
        StatType<T> type = stat.getType();

        // For 'minecraft:custom' stats, the value is already an Identifier
        if (value instanceof Identifier res) {
            return res.toString();
        }

        // For everything else (Items, Blocks, Entities), we ask the type's registry for the key
        Identifier id = type.getRegistry().getKey(value);
        return (id != null) ? id.toString() : value.toString();
    }
}

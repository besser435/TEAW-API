package me.besser.tapi.listeners;

import me.besser.tapi.TAPIConfig;
import me.besser.tapi.database.InsertMethods;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerTracker {
    private final Map<UUID, Long> joinTime = new HashMap<>();
    private final Map<UUID, Long> lastMoveTime = new HashMap<>();
    private final Map<UUID, Vec3> lastPos = new HashMap<>();

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        UUID uuid = event.getEntity().getUUID();
        long now = Instant.now().getEpochSecond();

        joinTime.put(uuid, now);
        lastMoveTime.put(uuid, now);
        lastPos.put(uuid, event.getEntity().position());

        InsertMethods.initPlayer(uuid, event.getEntity().getName().getString());
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;

        int afkThreshold = TAPIConfig.COMMON.afkTimeout.get();
        UUID uuid = player.getUUID();
        long now = Instant.now().getEpochSecond();
        Vec3 currentPos = player.position();

        // Check for movement
        Vec3 prevPos = lastPos.get(uuid);
        if (prevPos != null && (Math.abs(currentPos.x - prevPos.x) > 1 || Math.abs(currentPos.z - prevPos.z) > 1)) {
            lastMoveTime.put(uuid, now);
            lastPos.put(uuid, currentPos);
        }

        // Calculate durations
        long onlineDuration = now - joinTime.getOrDefault(uuid, now);
        long idleTime = now - lastMoveTime.getOrDefault(uuid, now);
        long afkDuration = (idleTime >= afkThreshold) ? idleTime : 0;

        // Only update DB every 5 seconds
        if (player.tickCount % 100 == 0) {
            InsertMethods.updatePlayerSession(uuid, onlineDuration, afkDuration);
        }
    }

    @SubscribeEvent
    public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();

        InsertMethods.updatePlayerSession(uuid, 0, 0);

        joinTime.remove(uuid);
        lastMoveTime.remove(uuid);
        lastPos.remove(uuid);
    }
}
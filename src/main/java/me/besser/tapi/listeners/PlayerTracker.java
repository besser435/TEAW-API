package me.besser.tapi.listeners;

import me.besser.tapi.TAPIConfig;
import me.besser.tapi.database.InsertMethods;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerTracker {
    private final Map<UUID, SessionData> activeSessions = new HashMap<>();

    private static class SessionData {
        final long joinTime;
        long lastMoveTime;
        Vec3 lastPos;

        SessionData(long time, Vec3 pos) {
            this.joinTime = time;
            this.lastMoveTime = time;
            this.lastPos = pos;
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        UUID uuid = event.getEntity().getUUID();
        long now = System.currentTimeMillis() / 1000;

        activeSessions.put(uuid, new SessionData(now, event.getEntity().position()));
        InsertMethods.initPlayer(uuid, event.getEntity().getName().getString());
    }

    @SubscribeEvent
    public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();

        activeSessions.remove(uuid);
        InsertMethods.updatePlayerSession(uuid, 0, 0);
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID uuid = player.getUUID();

        // Stagger player updates to not happen all on the same tick
        int personalOffset = Math.abs(uuid.hashCode() % 100);
        if (player.tickCount % 100 != personalOffset) return;

        SessionData session = activeSessions.get(uuid);
        if (session == null) return;

        long now = System.currentTimeMillis() / 1000;
        Vec3 currentPos = player.position();

        // Check for movement
        if (Math.abs(currentPos.x - session.lastPos.x) > 1 || Math.abs(currentPos.z - session.lastPos.z) > 1) {
            session.lastMoveTime = now;
            session.lastPos = currentPos;
        }

        // Save data
        saveSessionToDatabase(uuid, session, now);
    }

    private void saveSessionToDatabase(UUID uuid, SessionData session, long now) {
        int afkThreshold = TAPIConfig.COMMON.afkTimeout.get();

        long onlineDuration = now - session.joinTime;
        long idleTime = now - session.lastMoveTime;
        long afkDuration = (idleTime >= afkThreshold) ? idleTime : 0;

        InsertMethods.updatePlayerSession(uuid, onlineDuration, afkDuration);
    }
}

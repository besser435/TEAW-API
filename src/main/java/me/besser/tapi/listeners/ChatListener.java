package me.besser.tapi.listeners;

import me.besser.tapi.database.InsertMethods;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public class ChatListener {

    public enum Type {
        CHAT,
        DISCORD,
        JOIN,
        QUIT,
        ADVANCEMENT,
        DEATH,
        STATUS
    }

    @SubscribeEvent
    public void onChat(ServerChatEvent event) {
        // TODO: TAPI v1 had to check for certain chars to avoid leaking private chats.
        // Ensure this only checks public messages for whatever system we  end up using.
        InsertMethods.logChat(
                event.getPlayer().getUUID(),
                event.getPlayer().getName().getString(),
                event.getRawText(),
                Type.CHAT);
    }

    @SubscribeEvent
    public void onServerStart(ServerStartingEvent event) {
        InsertMethods.logChat(
                null,
                "SERVER",
                "TEAW started!",
                Type.STATUS);
    }

    @SubscribeEvent
    public void onServerStop(ServerStoppingEvent event) {
        InsertMethods.logChat(
                null,
                "SERVER",
                "TEAW stopped!",
                Type.STATUS);
    }

    @SubscribeEvent
    public void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        InsertMethods.logChat(
                event.getEntity().getUUID(),
                "SERVER",
                event.getEntity().getName().getString() + " joined the game",
                Type.JOIN);
    }

    @SubscribeEvent
    public void onQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        InsertMethods.logChat(
                event.getEntity().getUUID(),
                "SERVER",
                event.getEntity().getName().getString() + " left the game",
                Type.QUIT);
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            String deathMsg = event.getSource().getLocalizedDeathMessage(player).getString();

            InsertMethods.logChat(
                    player.getUUID(),
                    "SERVER",
                    deathMsg,
                    Type.DEATH);
        }
    }

    @SubscribeEvent
    public void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        event.getAdvancement().value().display().ifPresent(display -> {
            String msg = event.getEntity().getName().getString() +
                    " has completed the advancement [" +
                    display.getTitle().getString() + "]";

            InsertMethods.logChat(
                    event.getEntity().getUUID(),
                    "SERVER",
                    msg,
                    Type.ADVANCEMENT);
        });
    }
}

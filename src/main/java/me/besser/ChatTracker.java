package me.besser;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static me.besser.DIETLogger.*;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ChatTracker implements Listener {
    private final List<ChatMessage> messageHistory = new LinkedList<>();

    private static final int MAX_MESSAGES = 200;

    public ChatTracker() {
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("TAPI"));
    }

    public synchronized List<ChatMessage> getLastMessages() {
        return new LinkedList<>(messageHistory); // Return a copy to prevent modification
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public synchronized void onPlayerChat(AsyncPlayerChatEvent event) {
        String cleanFormat = event.getFormat().replaceAll("§.", "");

        // Hacky as fuck, to ensure we only get the general chats and not private town/nation ones.
        // See comments in the TownyListenerAttempt file.
        if (cleanFormat.charAt(1) == 'g') {
            addMessage(new ChatMessage(
                    event.getPlayer().getName(),
                    event.getMessage(),
                    Instant.now().getEpochSecond(),
                    "msg"
            ));
        }
    }

    @EventHandler
    public synchronized void onPlayerJoin(PlayerJoinEvent event) {
        String joinMessage = event.getPlayer().getName() + " joined the game";

        addMessage(new ChatMessage(
                "SERVER",
                joinMessage,
                Instant.now().getEpochSecond(),
                "join"
        ));
    }

    @EventHandler
    public synchronized void onPlayerQuit(PlayerQuitEvent event) {
        String quitMessage = event.getPlayer().getName() + " left the game";

        addMessage(new ChatMessage(
                "SERVER",
                quitMessage,
                Instant.now().getEpochSecond(),
                "quit"
        ));
    }

    @EventHandler
    public synchronized void onPlayerDeath(PlayerDeathEvent event) {
        addMessage(new ChatMessage(
                "SERVER",
                event.getDeathMessage(),
                Instant.now().getEpochSecond(),
                "death"
        ));
    }

    @EventHandler
    public synchronized void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        String advancementName = Objects.requireNonNull(event.getAdvancement().getDisplay()).getTitle();
        String message = event.getPlayer().getName() + " has completed the advancement [" + advancementName + "]";

        addMessage(new ChatMessage(
                "SERVER",
                message,
                Instant.now().getEpochSecond(),
                "advancement"
        ));
    }

    private void addMessage(ChatMessage chatMessage) {
        if (messageHistory.size() >= MAX_MESSAGES) {
            messageHistory.remove(0);
        }
        messageHistory.add(chatMessage);
    }

    public record ChatMessage(String sender, String message, long timestamp, String type) { // ok java has some cool features...
        @Override
        public String toString() {
            return String.format("[%d] %s: %s", timestamp, sender, message);
        }
    }
}

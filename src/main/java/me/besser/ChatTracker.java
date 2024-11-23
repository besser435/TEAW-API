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

import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;

import java.util.LinkedList;
import java.util.List;


public class ChatTracker implements Listener {
    private final List<chatMessage> messageHistory = new LinkedList<>();

    private static final int MAX_MESSAGES = 200;

    public ChatTracker() {
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("TAPI"));
    }

    public synchronized List<chatMessage> getLastMessages() {
        return new LinkedList<>(messageHistory); // Return a copy to prevent modification
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public synchronized void onPlayerChat(AsyncPlayerChatEvent event) {
        String cleanFormat = event.getFormat().replaceAll("§.", "");

        // Hacky as fuck. Ensure we only get the general chats and not private town/nation ones.
        // See comments in the TownyListenerAttempt file.
        if (cleanFormat.charAt(1) == 'g') {
            addMessage(new chatMessage(
                event.getPlayer().getName(),
                event.getMessage(),
                System.currentTimeMillis(),
                msgType.chat
            ));
        }
    }

    @EventHandler
    public synchronized void onPlayerJoin(PlayerJoinEvent event) {
        String joinMessage = event.getPlayer().getName() + " joined the game";

        addMessage(new chatMessage(
            "SERVER",
            joinMessage,
            System.currentTimeMillis(),
            msgType.join
        ));
    }

    @EventHandler
    public synchronized void onPlayerQuit(PlayerQuitEvent event) {
        String quitMessage = event.getPlayer().getName() + " left the game";

        addMessage(new chatMessage(
            "SERVER",
            quitMessage,
            System.currentTimeMillis(),
            msgType.quit
        ));
    }

    @EventHandler
    public synchronized void onPlayerDeath(PlayerDeathEvent event) {
        addMessage(new chatMessage(
            "SERVER",
            event.getDeathMessage(),
            System.currentTimeMillis(),
            msgType.death
        ));
    }

    @EventHandler
    public synchronized void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        if (event.getAdvancement().getDisplay() != null) {
            String advancementName = event.getAdvancement().getDisplay().getTitle();
            //String advancementDescription = event.getAdvancement().getDisplay().getDescription();     // disabled due to it showing control codes
            String message = event.getPlayer().getName() + " has completed the advancement [" + advancementName + "]"; // (" + advancementDescription + ")";

            addMessage(new chatMessage(
                "SERVER",
                message,
                System.currentTimeMillis(),
                msgType.advancement
            ));
        }
    }

    @Subscribe(priority = ListenerPriority.NORMAL)
    public void onDiscordMessage(DiscordGuildMessageReceivedEvent event) {
        addMessage(new chatMessage(
            event.getAuthor().getName(),
            event.getMessage().getContentRaw(),
            System.currentTimeMillis(),
            msgType.discord
        ));
    }

    private void addMessage(chatMessage message) {
        if (messageHistory.size() >= MAX_MESSAGES) {
            messageHistory.remove(0);
        }
        messageHistory.add(message);
    }

    public record chatMessage(String sender, String message, long timestamp, msgType type) {}     // ok java has some cool features...

    enum msgType {  // Enums should be capitalized, but they should to be lowercase for the JSON so screw it
        chat,
        discord,
        join,
        quit,
        advancement,
        death
    }
}


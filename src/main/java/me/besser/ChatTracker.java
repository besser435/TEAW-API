package me.besser;

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
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;

import java.util.LinkedList;
import java.util.List;
import java.time.Instant;


public class ChatTracker implements Listener {
    private final List<chatMessage> messageHistory = new LinkedList<>();
    private static final int MAX_MESSAGES = 100;
    private final String DISCORD_CHANNEL_ID;

    public ChatTracker(TAPI plugin) {
        this.DISCORD_CHANNEL_ID = plugin.getConfig().getString("tapi.discord_channel_id", "1201298174736863273");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
                event.getPlayer().getUniqueId().toString(),
                event.getMessage(),
                Instant.now().toEpochMilli(),
                msgType.chat
            ));
        }
    }

    @EventHandler
    public synchronized void onPlayerJoin(PlayerJoinEvent event) {
        String joinMessage = event.getPlayer().getName() + " joined the game";

        addMessage(new chatMessage(
            "SERVER",
            event.getPlayer().getUniqueId().toString(),
            joinMessage,
            Instant.now().toEpochMilli(),
            msgType.join
        ));
    }

    @EventHandler
    public synchronized void onPlayerQuit(PlayerQuitEvent event) {
        String quitMessage = event.getPlayer().getName() + " left the game";

        addMessage(new chatMessage(
            "SERVER",
            event.getPlayer().getUniqueId().toString(),
            quitMessage,
            Instant.now().toEpochMilli(),
            msgType.quit
        ));
    }

    @EventHandler
    public synchronized void onPlayerDeath(PlayerDeathEvent event) {
        addMessage(new chatMessage(
            "SERVER",
            event.getEntity().getUniqueId().toString(),
            event.getDeathMessage(),
            Instant.now().toEpochMilli(),
            msgType.death
        ));
    }

    @EventHandler
    public synchronized void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        if (event.getAdvancement().getDisplay() != null) {
            String advancementName = event.getAdvancement().getDisplay().getTitle();
            //String advancementDescription = event.getAdvancement().getDisplay().getDescription();     // Disabled due to it showing control codes
            String message = event.getPlayer().getName() + " has completed the advancement [" + advancementName + "]"; // (" + advancementDescription + ")";

            addMessage(new chatMessage(
                "SERVER",
                event.getPlayer().getUniqueId().toString(),
                message,
                Instant.now().toEpochMilli(),
                msgType.advancement
            ));
        }
    }

    @Subscribe(priority = ListenerPriority.NORMAL)
    public void onDiscordMessage(DiscordGuildMessageReceivedEvent event) {
        TextChannel channel = event.getChannel();
        String messageDisplay = event.getMessage().getContentDisplay();

        if (!channel.getId().equals(DISCORD_CHANNEL_ID)) return;

        if (messageDisplay.isBlank()) return; // This method would sometimes log empty messages. This *should* fix that.

        addMessage(new chatMessage(
            event.getAuthor().getName(),
            event.getAuthor().getId(),
            messageDisplay,
            Instant.now().toEpochMilli(),
            msgType.discord
        ));
    }

    public synchronized void addMessage(chatMessage message) {
        if (messageHistory.size() >= MAX_MESSAGES) {
            messageHistory.remove(0);
        }
        messageHistory.add(message);
    }

    public record chatMessage(String sender, String sender_uuid, String message, long timestamp, msgType type) {}

    enum msgType {  // Enums should be capitalized, but they should to be lowercase for the JSON so screw it
        chat,
        discord,
        join,
        quit,
        advancement,
        death,
        status
    }
}


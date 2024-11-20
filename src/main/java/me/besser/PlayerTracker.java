package me.besser;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

import net.milkbowl.vault.economy.Economy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlayerTracker implements Listener {
    private final Map<Player, Long> lastMoveTime = new HashMap<>();
    private final int AFK_THRESHOLD;

    public PlayerTracker(TAPI plugin) {
        this.AFK_THRESHOLD = plugin.getConfig().getInt("tapi.afk_timeout", 180);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        lastMoveTime.put(player, Instant.now().getEpochSecond());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        lastMoveTime.put(player, Instant.now().getEpochSecond());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        lastMoveTime.remove(player);
    }

    public boolean isPlayerAFK(Player player) {
        long lastMove = lastMoveTime.getOrDefault(player, Instant.now().getEpochSecond());
        return (Instant.now().getEpochSecond() - lastMove) > AFK_THRESHOLD;
    }

    public JsonObject getOnlinePlayersInfo() {
        JsonObject result = new JsonObject();

        // Add the online player count
        //int onlinePlayerCount = org.bukkit.Bukkit.getOnlinePlayers().size();
        //result.addProperty("online_player_count", onlinePlayerCount);

        // Add player information keyed by UUID
        JsonObject playersObject = new JsonObject();
        Economy economy = TAPI.getEconomy();

        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            boolean isAFK = isPlayerAFK(player);

            JsonObject playerData = new JsonObject();
            playerData.addProperty("name", player.getName());
            playerData.addProperty("afk", isAFK);

            double balance = economy.getBalance(player);
            BigDecimal bd = new BigDecimal(balance).setScale(2, RoundingMode.HALF_UP);
            playerData.addProperty("balance", bd.doubleValue());

            addTownyData(player, playerData);

            playersObject.add(player.getUniqueId().toString(), playerData);
        }

        result.add("online_players", playersObject);
        return result;
    }

    private void addTownyData(Player player, JsonObject playerData) {
        Resident resident = TownyAPI.getInstance().getResident(player.getUniqueId());

        // TODO: clean this, there are too many conditionals. See how its done in TownyTracker.java
        if (resident != null) {
            Town town = resident.getTownOrNull();
            if (town != null) {
                playerData.addProperty("town", town.getName());

                Nation nation = town.getNationOrNull();
                String title = resident.getTitle();
                if (nation != null) {
                    playerData.addProperty("nation", nation.getName());
                } else {
                    playerData.addProperty("nation", "");
                }

                playerData.addProperty("title", Objects.requireNonNullElse(title, ""));
            } else {
                playerData.addProperty("town", "");
                playerData.addProperty("nation", "");
            }
        } else {
            playerData.addProperty("town", "");
            playerData.addProperty("nation", "");
        }
    }
}
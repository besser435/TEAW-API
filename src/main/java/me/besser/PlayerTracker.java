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
    private final Map<Player, Long> joinTime = new HashMap<>();
    private final int AFK_THRESHOLD;

    public PlayerTracker(TAPI plugin) {
        this.AFK_THRESHOLD = plugin.getConfig().getInt("tapi.afk_timeout", 180) * 1000;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        long currentTime = Instant.now().toEpochMilli();
        lastMoveTime.put(player, currentTime);
        joinTime.put(player, currentTime);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        lastMoveTime.put(player, Instant.now().toEpochMilli());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        lastMoveTime.remove(player);
        joinTime.remove(player);
    }

    /**
     Returns the AFK duration for a player in milliseconds if they are AFK.
     If the player has moved within the configured AFK threshold, this method returns 0.

     @return The AFK time in seconds. Returns 0 if the player is not AFK.
     */
    public int getPlayerAFKDuration(Player player) {
        long lastMove = lastMoveTime.getOrDefault(player, Instant.now().toEpochMilli());
        long currentTime = Instant.now().toEpochMilli();
        long afkDuration = currentTime - lastMove;

        return afkDuration > AFK_THRESHOLD ? (int) afkDuration : 0;
    }

    /**
     Returns how long a player has been online in milliseconds. Keeps counting while the player is AFK

     @return The total online time in seconds. Returns 0 if the player is not tracked.
     */
    public int getPlayerOnlineDuration(Player player) {
        long joinTimestamp = joinTime.getOrDefault(player, Instant.now().toEpochMilli());
        long currentTime = Instant.now().toEpochMilli();

        return (int) (currentTime - joinTimestamp);
    }

    public JsonObject getOnlinePlayersInfo() {
        JsonObject result = new JsonObject();

        // Add player information keyed by UUID
        JsonObject playersObject = new JsonObject();
        Economy economy = TAPI.getEconomy();

        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            JsonObject playerData = new JsonObject();

            playerData.addProperty("name", player.getName());

            playerData.addProperty("online_duration", getPlayerOnlineDuration(player));

            playerData.addProperty("afk_duration", getPlayerAFKDuration(player));

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

            String title = resident.getTitle();
            playerData.addProperty("title", Objects.requireNonNullElse(title, ""));
            if (town != null) {
                //playerData.addProperty("town", town.getUUID().toString());
                playerData.addProperty("town_name", town.getName());

                Nation nation = town.getNationOrNull();

                if (nation != null) {
                    //playerData.addProperty("nation", nation.getUUID().toString());
                    playerData.addProperty("nation_name", nation.getName());
                } else {
                    //playerData.addProperty("nation", nation.getUUID().toString());
                    playerData.addProperty("nation_name", "");
                }

            } else {
                //playerData.addProperty("town", "");
                //playerData.addProperty("nation", "");
                playerData.addProperty("town_name", "");
                playerData.addProperty("nation_name", "");
            }
        } else {
            //playerData.addProperty("town", "");
            //playerData.addProperty("nation", "");
            playerData.addProperty("town_name", "");
            playerData.addProperty("nation_name", "");
        }
    }
}
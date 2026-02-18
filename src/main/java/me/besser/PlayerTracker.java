package me.besser;

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

public class PlayerTracker implements Listener {
    private final Map<Player, Long> lastMoveTime = new HashMap<>();
    private final Map<Player, Long> joinTime = new HashMap<>();
    private final int AFK_THRESHOLD;


    public PlayerTracker(TAPI plugin) {
        this.AFK_THRESHOLD = plugin.getConfig().getInt("tapi.afk_timeout", 180) * 1000;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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

    public Map<String, Object> getOnlinePlayersInfo() {
        Map<String, Object> result = new HashMap<>();

        // Add player information keyed by UUID
        Map<String, Object> playersObject = new HashMap<>();
        Economy economy = TAPI.getEconomy();

        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            Map<String, Object> playerData = new HashMap<>();

            playerData.put("name", player.getName());

            playerData.put("online_duration", getPlayerOnlineDuration(player));

            playerData.put("afk_duration", getPlayerAFKDuration(player));

            double balance = economy.getBalance(player);
            BigDecimal bd = new BigDecimal(balance).setScale(2, RoundingMode.HALF_UP);
            playerData.put("balance", bd.doubleValue());

            addTownyData(player, playerData);

            playersObject.put(player.getUniqueId().toString(), playerData);
        }

        result.put("online_players", playersObject);
        return result;
    }

    private void addTownyData(Player player, Map<String, Object> playerData) {
        Resident resident = TownyAPI.getInstance().getResident(player.getUniqueId());

        // Defaults
        playerData.put("title", "");
        playerData.put("town", "");
        playerData.put("town_name", "");
        playerData.put("nation", "");
        playerData.put("nation_name", "");
        playerData.put("first_joined_date", 0);

        if (resident == null) {
            return;
        }

        playerData.put("joined_date", resident.getRegistered());

        String title = resident.getTitle();
        if (title != null) {
            playerData.put("title", title);
        }

        Town town = resident.getTownOrNull();
        if (town == null) {
            return;
        }

        playerData.put("town", town.getUUID().toString());
        playerData.put("town_name", town.getName());

        Nation nation = town.getNationOrNull();
        if (nation == null) {
            return;
        }

        playerData.put("nation", nation.getUUID().toString());
        playerData.put("nation_name", nation.getName());
    }
}
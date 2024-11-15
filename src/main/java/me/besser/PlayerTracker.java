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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerTracker implements Listener {
    private final Map<Player, Long> lastMoveTime = new HashMap<>();
    private final int AFK_THRESHOLD;

    public PlayerTracker(TAPI plugin) {
        this.AFK_THRESHOLD = plugin.getConfig().getInt("tapi.afk_timeout", 180) * 1000;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        lastMoveTime.put(player, System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        lastMoveTime.put(player, System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        lastMoveTime.remove(player);
    }

    public boolean isPlayerAFK(Player player) {
        long lastMove = lastMoveTime.getOrDefault(player, System.currentTimeMillis());
        return (System.currentTimeMillis() - lastMove) > AFK_THRESHOLD;
    }

    /*
    TODO: some things should be seperated into different APIs.
    this should be just a general player API, with a bool for if the player is online or not
    */
    public Map<UUID, Map<String, Object>> getOnlinePlayersInfo() {
        // Should maybe change to a better format, like "uuid":UUID, rather than just UUID
        Map<UUID, Map<String, Object>> playerInfoMap = new HashMap<>();
        Economy economy = TAPI.getEconomy();

        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            boolean isAFK = isPlayerAFK(player);

            Map<String, Object> playerData = new HashMap<>();
            playerData.put("name", player.getName());
            playerData.put("afk", isAFK);


            double balance = economy.getBalance(player);
            BigDecimal bd = new BigDecimal(balance).setScale(2, RoundingMode.HALF_UP);  // unserious programming language
            playerData.put("balance", bd.doubleValue());

            addTownyData(player, playerData);

            playerInfoMap.put(player.getUniqueId(), playerData);
        }
        return playerInfoMap;
    }

    private void addTownyData(Player player, Map<String, Object> playerData) {
        Resident resident = TownyAPI.getInstance().getResident(player.getUniqueId());

        // TODO: clean this, there are too many conditionals. See how its done in TownyTracker.java
        if (resident != null) {
            Town town = resident.getTownOrNull();
            if (town != null) {
                playerData.put("town", town.getName());

                Nation nation = town.getNationOrNull();
                String title = resident.getTitle();
                if (nation != null) {
                    playerData.put("nation", nation.getName());
                } else {
                    playerData.put("nation", "");
                }

                playerData.put("title", Objects.requireNonNullElse(title, ""));
            } else {
                playerData.put("town", "");
                playerData.put("nation", "");
            }
        } else {
            playerData.put("town", "");
            playerData.put("nation", "");
        }
    }
}
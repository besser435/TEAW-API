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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerTracker implements Listener {
    private final Map<Player, Long> lastMoveTime = new HashMap<>();
    private final long AFK_THRESHOLD = 1000 * 180; // 3 minutes in milliseconds TODO: should be seconds in config.yml

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

    public Map<UUID, Map<String, Object>> getOnlinePlayersInfo() {
        // Should maybe change to a better format, like "uuid":UUID, rather than just UUID
        Map<UUID, Map<String, Object>> playerInfoMap = new HashMap<>();
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            boolean isAFK = isPlayerAFK(player);

            Map<String, Object> playerData = new HashMap<>();
            playerData.put("name", player.getName());
            playerData.put("afk", isAFK);

            addTownyData(player, playerData);

            playerInfoMap.put(player.getUniqueId(), playerData);
        }
        return playerInfoMap;
    }

    private void addTownyData(Player player, Map<String, Object> playerData) {
        Resident resident = TownyAPI.getInstance().getResident(player.getUniqueId());

        if (resident != null) {
            Town town = resident.getTownOrNull();
            if (town != null) {
                playerData.put("town", town.getName());

                Nation nation = town.getNationOrNull();
                if (nation != null) {
                    playerData.put("nation", nation.getName());
                } else {
                    playerData.put("nation", "No Nation");
                }
            } else {
                playerData.put("town", "No Town");
                playerData.put("nation", "No Nation");
            }
        } else {
            playerData.put("town", "No Town");
            playerData.put("nation", "No Nation");
        }
    }
}
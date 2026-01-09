package me.besser;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.*;

public class PVPTracker implements Listener {
    private static final int MAX_RECORDS = 50;
    private final List<PVPKill> killHistory = new LinkedList<>();

    public PVPTracker(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public synchronized List<PVPKill> getLastKills() {
        return new LinkedList<>(killHistory); // Return a copy to prevent modification
    }

    public synchronized void addKill(PVPKill kill) {
        if (killHistory.size() >= MAX_RECORDS) {
            killHistory.remove(0);
        }
        killHistory.add(kill);
    }

    public record PVPKill(
            String killer_uuid, String killer_name,
            String victim_uuid, String victim_name,
            String death_message,
            Map<String, Object> weapon,
            long timestamp
    ) {}

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) return;

        ItemStack weapon = killer.getInventory().getItemInMainHand();
        ItemMeta meta = weapon.getItemMeta();

        // Convert weapon info to Map
        Map<String, Object> weaponData = new HashMap<>();
        weaponData.put("type", weapon.getType().name().toLowerCase());

        if (meta != null) {
            // Display name
            if (meta.hasDisplayName()) {
                // Stripped to remove custom colors and lore from item renaming plugin
                String displayName = ChatColor.stripColor(meta.getDisplayName());
                weaponData.put("name", displayName);
            }

            // Enchantments
            if (meta.hasEnchants()) {
                List<Map<String, Object>> enchantments = new ArrayList<>();
                weapon.getEnchantments().forEach((enchant, level) -> {
                    Map<String, Object> enchantData = new HashMap<>();
                    enchantData.put("id", enchant.getKey().getKey());   // TODO: fix deprecated method
                    enchantData.put("level", level);
                    enchantments.add(enchantData);
                });
                weaponData.put("enchantments", enchantments);
            }
        }

        addKill(new PVPKill(
                killer.getUniqueId().toString(), killer.getName(),
                victim.getUniqueId().toString(), victim.getName(),
                ChatColor.stripColor(event.getDeathMessage()),  // Remove minecraft control codes
                weaponData,
                Instant.now().toEpochMilli()
        ));
    }
}

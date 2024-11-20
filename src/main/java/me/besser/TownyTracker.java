package me.besser;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TownyTracker {
    public Map<UUID, Map<String, Object>> getTownData() {
        // TODO: UUIDs should be used in addition to names.
        Map<UUID, Map<String, Object>> townDataMap = new HashMap<>();

        for (Town town : TownyAPI.getInstance().getTowns()) {
            Map<String, Object> townData = new HashMap<>();

            townData.put("name", town.getName());
            townData.put("founding_date", town.getRegistered());

            Nation nation = town.getNationOrNull();
            townData.put("nation", (nation != null) ? nation.getName() : "");

            String founder = town.getFounder();
            townData.put("founder", (founder != null) ? founder : "");

            Resident mayor = town.getMayor();
            townData.put("mayor", (mayor != null) ? mayor.getName() : "");

            townData.put("board", town.getBoard());
            townData.put("balance", town.getAccount().getHoldingBalance());
            townData.put("resident_tax_percent", town.getTaxes());

            List<Resident> residents = town.getResidents();
            townData.put("residents", residents.stream().map(Resident::getUUID).toArray(UUID[]::new));

            int townSize = town.getTownBlocks().size();
            townData.put("claimed_chunks", townSize);

            townDataMap.put(town.getUUID(), townData);
        }
        return townDataMap;
    }

    public Map<UUID, Map<String, Object>> getNationData() {
        // TODO: UUIDs should be used in addition to names.
        // Things like capitol_town should return the name, and UUID.
        Map<UUID, Map<String, Object>> nationDataMap = new HashMap<>();

        for (Nation nation : TownyAPI.getInstance().getNations()) {
            Map<String, Object> nationData = new HashMap<>();

            nationData.put("name", nation.getName());
            nationData.put("founding_date", nation.getRegistered());

            Resident leader = nation.getKing(); // Towny is so not based or woke :(
            nationData.put("leader", (leader != null) ? leader.getName() : "");

            Town capital = nation.getCapital();
            nationData.put("capitol_town", (capital != null) ? capital.getName() : "");

            nationData.put("board", nation.getBoard());
            nationData.put("balance", nation.getAccount().getHoldingBalance());
            nationData.put("town_tax_dollars", nation.getTaxes());

            nationDataMap.put(nation.getUUID(), nationData);
        }
        return nationDataMap;
    }

//    // Checks if 20% or more of the towns residents have been online in the last 14 days
//    private static final long FOURTEEN_DAYS_MILLIS = 1000L * 60 * 60 * 24 * 14;
//
//    public boolean isTownActive(Town town) {
//        List<Resident> residents = town.getResidents();
//        int totalResidents = residents.size();
//
//        long currentTime = System.currentTimeMillis();
//        int activePlayers = 0;
//
//        for (Resident resident : residents) {
//            // Use Essentials API to get the last time someone was online. Spigot can't do this.
//        }
//
//        double activePercentage = (double) activePlayers / totalResidents;
//        return activePercentage >= 0.20;
//    }
}

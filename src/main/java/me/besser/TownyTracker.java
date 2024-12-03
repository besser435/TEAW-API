package me.besser;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static me.besser.DIETLogger.*;

public class TownyTracker {
    private static final long FOURTEEN_DAYS_MILLIS = 1000L * 60 * 60 * 24 * 14;
    private final Essentials essentials;

    public TownyTracker(Essentials essentials) {
        if (essentials == null) {
            throw new IllegalArgumentException("Essentials instance cannot be null.");
        }
        this.essentials = essentials;
    }

    public Map<UUID, Map<String, Object>> getTownData() {
        // TODO: player UUIDs should be used in addition to names.
        Map<UUID, Map<String, Object>> townDataMap = new HashMap<>();

        for (Town town : TownyAPI.getInstance().getTowns()) {
            Map<String, Object> townData = new HashMap<>();

            townData.put("name", town.getName());
            townData.put("founded", town.getRegistered());

            Boolean isActive = isTownActive(town);
            townData.put("is_active", isActive);

            Nation nation = town.getNationOrNull();
            townData.put("nation", (nation != null) ? nation.getUUID() : "");
            townData.put("nation_name", (nation != null) ? nation.getName() : "");

            String founder = town.getFounder();
            townData.put("founder", (founder != null) ? founder : "");

            Resident mayor = town.getMayor();
            townData.put("mayor", (mayor != null) ? mayor.getName() : "");

            townData.put("board", town.getBoard());
            townData.put("tag", town.getTag());
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
        // TODO: player and town UUIDs should be used in addition to names.
        // Things like capitol_town should return the name, and UUID.
        Map<UUID, Map<String, Object>> nationDataMap = new HashMap<>();

        for (Nation nation : TownyAPI.getInstance().getNations()) {
            Map<String, Object> nationData = new HashMap<>();

            nationData.put("name", nation.getName());
            nationData.put("founded", nation.getRegistered());

            Resident leader = nation.getKing(); // Towny is so not based or woke :(
            nationData.put("leader", (leader != null) ? leader.getName() : "");

            Town capital = nation.getCapital();
            nationData.put("capitol_town", (capital != null) ? capital.getUUID() : "");
            nationData.put("capitol_town_name", (capital != null) ? capital.getName() : "");

            nationData.put("board", nation.getBoard());
            nationData.put("tag", nation.getTag());
            nationData.put("balance", nation.getAccount().getHoldingBalance());
            nationData.put("town_tax_dollars", nation.getTaxes());

            nationDataMap.put(nation.getUUID(), nationData);
        }
        return nationDataMap;
    }

    /**
     * Determines whether a town is active based on the activity of its residents.
     * A town is considered active if at least 30% of its residents have been online
     * within the last 14 days. The activity status of each resident is determined
     * using the Essentials API to check their last logout time.
     *
     * @param town the {@link Town} to evaluate for activity.
     * @return {@code true} if the town is active (20% or more of its residents have
     *         logged in within the last 14 days), otherwise {@code false}.
     */
    public boolean isTownActive(Town town) {
        List<Resident> residents = town.getResidents();
        int totalResidents = residents.size();

        if (totalResidents == 0) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        int activePlayers = 0;

        for (Resident resident : residents) {
            UUID uuid = resident.getUUID();
            User user = essentials.getUser(uuid);

            if (user != null) {
                long lastLogout = user.getLastLogout();
                if (currentTime - lastLogout <= FOURTEEN_DAYS_MILLIS) {
                    activePlayers++;
                }
            }
        }

        double activePercentage = (double) activePlayers / totalResidents;
        return activePercentage >= 0.30;
    }
}

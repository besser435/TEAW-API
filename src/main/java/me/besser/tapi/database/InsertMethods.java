package me.besser.tapi.database;

import com.google.gson.JsonObject;
import me.besser.tapi.TAPI;
import me.besser.tapi.listeners.ChatTracker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InsertMethods {
    private static final ExecutorService DB_THREAD_POOL = Executors.newSingleThreadExecutor();

    // Log chat message
    public static void logChat(UUID uuid, String sender, String msg, ChatTracker.Type type) {
        DB_THREAD_POOL.execute(() -> {
            String sql = "INSERT INTO chat(sender_uuid, sender, message, timestamp, type) VALUES(?,?,?,?,?)";

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, uuid != null ? uuid.toString().toLowerCase() : null);
                stmt.setString(2, sender);
                stmt.setString(3, msg);
                stmt.setLong(4, Instant.now().getEpochSecond());
                stmt.setString(5, type.toString().toLowerCase());
                stmt.executeUpdate();

            } catch (SQLException e) {
                TAPI.LOGGER.error("DB error on chat message: {}", e.getMessage());
            }
        });
    }

    // Init/create player
    public static void initPlayer(UUID uuid, String name) {
        DB_THREAD_POOL.execute(() -> {
            long now = Instant.now().getEpochSecond();
            String sql = "INSERT INTO players (uuid, name, last_online, first_joined_date) VALUES (?, ?, ?, ?) " +
                    "ON CONFLICT(uuid) DO UPDATE SET name = excluded.name, last_online = excluded.last_online;";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, name);
                stmt.setLong(3, now);
                stmt.setLong(4, now); // Only used if row is new
                stmt.executeUpdate();
            } catch (SQLException e) {
                TAPI.LOGGER.error("DB Error on player init: {}", e.getMessage());
            }
        });
    }

    // Update player
    public static void updatePlayerSession(UUID uuid, long onlineSec, long afkSec) {
        DB_THREAD_POOL.execute(() -> {
            String sql = "UPDATE players SET online_duration = ?, afk_duration = ?, last_online = ? WHERE uuid = ?;";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, onlineSec);
                stmt.setLong(2, afkSec);
                stmt.setLong(3, Instant.now().getEpochSecond());
                stmt.setString(4, uuid.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                TAPI.LOGGER.error("DB Error on player update: {}", e.getMessage());
            }
        });
    }

    // Log kill
    public static void logKill(UUID kUuid, String kName, UUID vUuid, String vName, String deathMsg, JsonObject weaponJson) {
        DB_THREAD_POOL.execute(() -> {
            String sql = "INSERT INTO kills(killer_uuid, killer_name, victim_uuid, victim_name, death_message, weapon_json, timestamp) VALUES(?,?,?,?,?,?,?)";

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, kUuid.toString());
                stmt.setString(2, kName);
                stmt.setString(3, vUuid.toString());
                stmt.setString(4, vName);
                stmt.setString(5, deathMsg);
                stmt.setString(6, weaponJson.toString());
                stmt.setLong(7, Instant.now().getEpochSecond());
                stmt.executeUpdate();

            } catch (SQLException e) {
                TAPI.LOGGER.error("DB error on kill log: {}", e.getMessage());
            }
        });
    }

    // Log player stats
    public static void batchUpdateStats(UUID uuid, Map<String, Map<String, Integer>> statsMap) {
        DB_THREAD_POOL.execute(() -> {
            String sql = "INSERT INTO player_statistics (player_uuid, category, stat_key, stat_value) VALUES (?, ?, ?, ?) " +
                    "ON CONFLICT(player_uuid, category, stat_key) DO UPDATE SET stat_value = excluded.stat_value;";

            try (Connection conn = DatabaseManager.getConnection()) {
                conn.setAutoCommit(false); // Start Transaction
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    String uuidStr = uuid.toString();

                    for (Map.Entry<String, Map<String, Integer>> categoryEntry : statsMap.entrySet()) {
                        String category = categoryEntry.getKey();
                        for (Map.Entry<String, Integer> statEntry : categoryEntry.getValue().entrySet()) {
                            stmt.setString(1, uuidStr);
                            stmt.setString(2, category);
                            stmt.setString(3, statEntry.getKey());
                            stmt.setInt(4, statEntry.getValue());
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                    conn.commit(); // End Transaction
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                TAPI.LOGGER.error("Batch stat update failed for {}: {}", uuid, e.getMessage());
            }
        });
    }

    // Store variable
    public static void upsertVariable(String variable, String value) {
        DB_THREAD_POOL.execute(() -> {
            String sql = "INSERT INTO variables (variable, value) VALUES (?, ?) " +
                    "ON CONFLICT(variable) DO UPDATE SET value = excluded.value;";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, variable);
                stmt.setString(2, value);
                stmt.executeUpdate();
            } catch (SQLException e) {
                TAPI.LOGGER.error("DB Error upserting variable {}: {}", variable, e.getMessage());
            }
        });
    }

    public static void shutdown() {
        TAPI.LOGGER.info("Shutting down database thread pool...");
        DB_THREAD_POOL.shutdown();
        try {
            // Wait up to 5 seconds
            if (!DB_THREAD_POOL.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                DB_THREAD_POOL.shutdownNow(); // Force close if it takes too long
            }
        } catch (InterruptedException e) {
            DB_THREAD_POOL.shutdownNow();
        }
        TAPI.LOGGER.info("Database thread pool shut down.");
    }
}

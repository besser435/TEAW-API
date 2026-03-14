package me.besser.tapi.database;

import me.besser.tapi.TAPI;
import me.besser.tapi.listeners.ChatListener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InsertMethods {
    private static final ExecutorService DB_THREAD_POOL = Executors.newSingleThreadExecutor();

    public static void logChat(UUID uuid, String sender, String msg, ChatListener.Type type) {
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
                TAPI.LOGGER.error("Failed to log chat message: {}", e.getMessage());
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

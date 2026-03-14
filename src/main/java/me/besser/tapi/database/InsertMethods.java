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
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // TODO will throw null pointer when null gets passed, say from server chat messages
                pstmt.setString(1, uuid != null ? uuid.toString().toLowerCase() : null);
                pstmt.setString(2, sender);
                pstmt.setString(3, msg);
                pstmt.setLong(4, Instant.now().toEpochMilli());
                pstmt.setString(5, type.toString().toLowerCase());
                pstmt.executeUpdate();

            } catch (SQLException e) {
                TAPI.LOGGER.error("Failed to log chat message: {}", e.getMessage());
            }
        });
    }

    public static void shutdown() {
        DB_THREAD_POOL.shutdown();
    }
}

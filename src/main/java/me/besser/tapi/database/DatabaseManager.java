package me.besser.tapi.database;

import me.besser.tapi.TAPI;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:TAPI.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initialize() {
        TAPI.LOGGER.info("Initializing SQLite Database...");

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS chat (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "sender_uuid TEXT," +   // Can be null due to server messages
                    "sender TEXT NOT NULL," +
                    "message TEXT," +
                    "timestamp DATETIME," +
                    "type TEXT);");

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_chat_timestamp ON chat(timestamp);");

            TAPI.LOGGER.info("SQLite Database initialized!");
        } catch (SQLException e) {
            TAPI.LOGGER.error("Database initialization failed: {}", e.getMessage());
        }
    }
}

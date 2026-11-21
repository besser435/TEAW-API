package me.besser.tapi.database;

import me.besser.tapi.TAPI;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final Path DB_DIRECTORY = FMLPaths.GAMEDIR.get().resolve("tapi");
    private static final Path DB_PATH = DB_DIRECTORY.resolve("tapi.db");
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH.toAbsolutePath();

    public static Connection getConnection() throws SQLException {
        try {
            Files.createDirectories(DB_DIRECTORY);
        } catch (IOException e) {
            throw new SQLException("Failed to create TAPI database directory!", e);
        }

        return DriverManager.getConnection(DB_URL);
    }

    public static void initialize() {
        TAPI.LOGGER.info("Initializing SQLite Database...");

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Players table
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS players (" +
                            "uuid TEXT PRIMARY KEY," +
                            "name TEXT NOT NULL," +
                            "online_duration INTEGER DEFAULT 0," +  // Seconds in current session
                            "afk_duration INTEGER DEFAULT 0," +     // Seconds AFK (0 if not AFK)
                            "last_online INTEGER," +
                            "first_joined_date INTEGER," +
                            "played_v4 BOOLEAN DEFAULT 0);"
            );


            // Chat table
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS chat (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "sender_uuid TEXT," +   // Can be null due to server messages
                            "sender TEXT NOT NULL," +
                            "message TEXT," +
                            "timestamp DATETIME," +
                            "type TEXT);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_chat_timestamp ON chat(timestamp);");


            // Kills table
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS kills (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "killer_uuid TEXT," +
                            "killer_name TEXT," +
                            "victim_uuid TEXT," +
                            "victim_name TEXT," +
                            "death_message TEXT," +
                            "weapon_json TEXT," +
                            "timestamp INTEGER);"
            );


            // Player stats table
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS player_statistics (" +
                            "player_uuid TEXT," +
                            "category TEXT," +   // minecraft:mined, minecraft:custom, etc
                            "stat_key TEXT," +   // minecraft:dirt, minecraft:jump, etc
                            "stat_value INTEGER," +
                            "PRIMARY KEY (player_uuid, category, stat_key));"
            );
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_stats_lookup ON player_statistics(stat_key, stat_value);");


            // Variables table
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS variables (" +
                            "variable TEXT PRIMARY KEY," +
                            "value TEXT);"
            );


            TAPI.LOGGER.info("SQLite Database initialized!");
        } catch (SQLException e) {
            throw new RuntimeException("TAPI database initialization failed: ", e);
        }
    }
}

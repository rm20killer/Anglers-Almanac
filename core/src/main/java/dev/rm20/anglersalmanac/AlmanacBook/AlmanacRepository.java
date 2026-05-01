package dev.rm20.anglersalmanac.AlmanacBook;

import dev.rm20.anglersalmanac.AnglersAlmanac;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AlmanacRepository {

    private static final String DB_PATH = "mods/dev.rm20_AnglersAlmanac/Data/BookIDs.db";
    private static Connection connection;

    public static class BookEntry {
        public final String customId;
        public final String playerName;

        public BookEntry(String customId, String playerName) {
            this.customId = customId;
            this.playerName = playerName;
        }
    }

    public AlmanacRepository() {
        init();
    }
    private void init() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite Driver not found in classpath!", e);
        }

        try {
            File dbFile = new File(DB_PATH);
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    AnglersAlmanac.LOGGER.atInfo().log("Created database directory: " + parentDir.getPath());
                }
            }

            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DB_PATH;
            connection = DriverManager.getConnection(url);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL;");
                stmt.execute("PRAGMA synchronous=NORMAL;");
            }

            setupDatabase();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite Driver not found in classpath!", e);
        } catch (SQLException e) {
            AnglersAlmanac.LOGGER.atSevere().withCause(e).log("Failed to initialize SQLite connection");
        }

    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void setupDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS custom_items (" +
                "player_uuid TEXT PRIMARY KEY, " +
                "player_name TEXT NOT NULL, " +
                "custom_id TEXT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    public static void saveBookId(String playerUuid, String customId, String playerName) {
        String sql = "REPLACE INTO custom_items (player_uuid, player_name, custom_id) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid);
            pstmt.setString(2, playerName);
            pstmt.setString(3, customId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Could not save Book ID to database: " + e.getMessage());
        }
    }

    public static BookEntry getBookData(String playerUuid) {
        String sql = "SELECT custom_id, player_name FROM custom_items WHERE player_uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new BookEntry(rs.getString("custom_id"), rs.getString("player_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, BookEntry> getAllSavedBooks() {
        Map<String, BookEntry> books = new HashMap<>();
        String sql = "SELECT player_uuid, player_name, custom_id FROM custom_items";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                books.put(
                        rs.getString("player_uuid"),
                        new BookEntry(rs.getString("custom_id"), rs.getString("player_name"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }


}

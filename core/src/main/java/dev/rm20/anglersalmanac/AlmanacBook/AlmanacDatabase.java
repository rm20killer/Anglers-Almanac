package dev.rm20.anglersalmanac.AlmanacBook;

import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Metadata.MinigamePRating;
import dev.rm20.anglersalmanac.api.IAlmanacProvider;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AlmanacDatabase implements IAlmanacProvider {
    private static final String DB_PATH = "mods/dev.rm20_AnglersAlmanac/Data/almanac.db";
    //private HikariDataSource dataSource;
    private Connection connection;

    public AlmanacDatabase() {
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

            createTables();
        } catch (SQLException e) {
            AnglersAlmanac.LOGGER.atSevere().withCause(e).log("Failed to make folder for almanac.db");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite Driver not found in classpath!", e);
        }
    }



    private Connection getConnection() throws SQLException {
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

    private void createTables() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Player's overall stats
            stmt.execute("CREATE TABLE IF NOT EXISTS players (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "total_catches INTEGER DEFAULT 0, " +
                    "legendary_catches INTEGER DEFAULT 0)");

            // fish counter per player
            stmt.execute("CREATE TABLE IF NOT EXISTS catches (" +
                    "player_uuid TEXT, " +
                    "fish_id TEXT, " +
                    "count INTEGER DEFAULT 0, " +
                    "PRIMARY KEY (player_uuid, fish_id))");

            // Performance
            stmt.execute("CREATE TABLE IF NOT EXISTS performance_stats (" +
                    "player_uuid TEXT, " +
                    "rating TEXT, " + // FAIL, GOOD, GREAT, PERFECT
                    "count INTEGER DEFAULT 0, " +
                    "PRIMARY KEY (player_uuid, rating))");
        }


    }

    public boolean saveCatch(String uuid, String fishId, boolean isLegendary, MinigamePRating.PerformanceRating rating) {
        boolean isFirstTime = false;

        try(Connection conn = getConnection())
        {
            conn.setAutoCommit(false);
            try (var psCheck = conn.prepareStatement("SELECT 1 FROM catches WHERE player_uuid = ? AND fish_id = ?")) {
                psCheck.setString(1, uuid);
                psCheck.setString(2, fishId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    isFirstTime = !rs.next();
                }
            }

            int legValue = isLegendary ? 1 : 0;
            String playerSql = "INSERT INTO players(uuid, total_catches, legendary_catches) VALUES(?, 1, ?) " +
                    "ON CONFLICT (uuid) DO UPDATE SET total_catches = total_catches + 1, legendary_catches = legendary_catches + ?";

            try (var psPlayer = conn.prepareStatement(playerSql)) {
                psPlayer.setString(1, uuid);
                psPlayer.setInt(2, legValue);
                psPlayer.setInt(3, legValue);
                psPlayer.executeUpdate();
            }

            String fishCountSQL = "INSERT INTO catches(player_uuid, fish_id, count) VALUES(?, ?, 1) " +
                    "ON CONFLICT (player_uuid, fish_id) DO UPDATE SET count = count + 1";

            try (var psFish = conn.prepareStatement(fishCountSQL)){
                psFish.setString(1, uuid);
                psFish.setString(2, fishId);
                psFish.executeUpdate();
            }

            String performanceSQL = "INSERT INTO performance_stats(player_uuid, rating, count) VALUES(?, ?, 1) " +
                    "ON CONFLICT (player_uuid, rating) DO UPDATE SET count = count + 1";
            try (var psRating = conn.prepareStatement(performanceSQL)){
                psRating.setString(1, uuid);
                psRating.setString(2, rating.name());
                psRating.executeUpdate();
            }



            conn.commit();
        } catch (SQLException e) {
            AnglersAlmanac.LOGGER.atSevere()
                    .withCause(e)
                    .log("Failed to save player stats for: " + uuid);
            return false;
        }

        return isFirstTime;
    }

    public PlayerStatsData getPlayerStats(String uuid) {
        PlayerStatsData data = new PlayerStatsData();

        String sqlTotal = "SELECT total_catches, legendary_catches FROM players WHERE uuid = ?";
        String sqlTop = "SELECT fish_id, count FROM catches WHERE player_uuid = ? ORDER BY count DESC LIMIT 10";
        String sqlRatings = "SELECT rating, count FROM performance_stats WHERE player_uuid = ?";
        String sqlAllFish = "SELECT fish_id, count FROM catches WHERE player_uuid = ?";

        try (Connection conn = getConnection()) {

            // 1. Get totals
            try (PreparedStatement ps = conn.prepareStatement(sqlTotal)) {
                ps.setString(1, uuid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        data.totalCatches = rs.getInt("total_catches");
                        data.legendaryCount = rs.getInt("legendary_catches");
                    }
                }
            }

            // 2. Get Top 10 fish
            try (PreparedStatement ps = conn.prepareStatement(sqlTop)) {
                ps.setString(1, uuid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        data.topFish.add(new FishEntry(rs.getString("fish_id"), rs.getInt("count")));
                    }
                }
            }

            // 3. Get Performance Ratings
            try (PreparedStatement ps = conn.prepareStatement(sqlRatings)) {
                ps.setString(1, uuid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        data.ratingsMap.put(rs.getString("rating"), rs.getInt("count"));
                    }
                }
            }

            // 4. Get all fish
            try (PreparedStatement ps = conn.prepareStatement(sqlAllFish)) {
                ps.setString(1, uuid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        data.catchMap.put(rs.getString("fish_id"), rs.getInt("count"));
                    }
                }
            }

        } catch (SQLException e) {
            AnglersAlmanac.LOGGER.atSevere()
                    .withCause(e)
                    .log("Failed to load player stats for: " + uuid);
        }

        return data;
    }


    public boolean hasPlayerCaught(String playerUUID, String fishId) {
        String sql = "SELECT 1 FROM catches WHERE player_uuid = ? AND fish_id = ? LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, playerUUID);
            ps.setString(2, fishId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            AnglersAlmanac.LOGGER.atSevere()
                    .withCause(e)
                    .log("Failed to check catch status for player: " + playerUUID);
            return false;
        }
    }

    public void addFishEntry(String uuid, String fishId) {
        String sql = "ON CONFLICT DO NOTHING INTO catches(player_uuid, fish_id, count) VALUES(?, ?, 0)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid);
            ps.setString(2, fishId);
            ps.executeUpdate();

        } catch (SQLException e) {
            AnglersAlmanac.LOGGER.atSevere()
                    .withCause(e)
                    .log("Failed to add fish entry for player: " + uuid);
        }
    }

    public void removeFishEntry(String uuid, String fishId) {
        String sql = fishId.equals("*")
                ? "DELETE FROM catches WHERE player_uuid = ?"
                : "DELETE FROM catches WHERE player_uuid = ? AND fish_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid);
            if (!fishId.equals("*")) {
                ps.setString(2, fishId);
            }

            ps.executeUpdate();

        } catch (SQLException e) {
            AnglersAlmanac.LOGGER.atSevere()
                    .withCause(e)
                    .log("Failed to remove fish entry for player: " + uuid + " (Fish: " + fishId + ")");
        }
    }

    public Map<String, Integer> getAllFishCounts(String playerUUID) {
        Map<String, Integer> counts = new HashMap<>();
        String sql = "SELECT fish_id, count FROM catches WHERE player_uuid = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, playerUUID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getString("fish_id"), rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            AnglersAlmanac.LOGGER.atSevere()
                    .withCause(e)
                    .log("Failed to fetch fish counts for player: " + playerUUID);
        }

        return counts;
    }

}
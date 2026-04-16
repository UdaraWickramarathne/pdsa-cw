package com.pdsa.game5.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Database access layer for Game 5 (Sixteen Queens).
 * Self-contained — does not share connection logic with other games.
 */
public class Game5DB {

    private static final String URL  = "jdbc:mysql://localhost:3306/pdsa_cw?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "root";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** Returns true if solutions table is already populated. */
    public static boolean solutionsExist() {
        String sql = "SELECT COUNT(*) FROM game5_solutions";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[Game5DB] solutionsExist error: " + e.getMessage());
        }
        return false;
    }

    /** Batch-inserts all solutions. Uses batching for performance. */
    public static void saveAllSolutions(List<int[]> solutions) {
        String sql = "INSERT IGNORE INTO game5_solutions (placement) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            int batch = 0;
            for (int[] solution : solutions) {
                ps.setString(1, toPlacementString(solution));
                ps.addBatch();
                if (++batch % 5000 == 0) {
                    ps.executeBatch();
                    conn.commit();
                }
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            System.err.println("[Game5DB] saveAllSolutions error: " + e.getMessage());
        }
    }

    /** Records timing for sequential vs threaded comparison. */
    public static void saveTiming(long sequentialMs, long threadedMs, int totalSolutions) {
        String sql = "INSERT INTO game5_timing (sequential_ms, threaded_ms, total_solutions) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sequentialMs);
            ps.setLong(2, threadedMs);
            ps.setInt(3, totalSolutions);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Game5DB] saveTiming error: " + e.getMessage());
        }
    }

    /**
     * Tries to claim a solution for a player.
     * @return "WIN" if claimed, "ALREADY_CLAIMED" if taken, "NOT_FOUND" if invalid.
     */
    public static String claimSolution(String placement, String playerName) {
        String checkSql = "SELECT solution_id, claimed FROM game5_solutions WHERE placement = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, placement);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return "NOT_FOUND";
                int solutionId = rs.getInt("solution_id");
                boolean claimed  = rs.getBoolean("claimed");
                if (claimed) return "ALREADY_CLAIMED";

                // Mark as claimed
                String markSql = "UPDATE game5_solutions SET claimed = TRUE WHERE solution_id = ?";
                try (PreparedStatement ps2 = conn.prepareStatement(markSql)) {
                    ps2.setInt(1, solutionId);
                    ps2.executeUpdate();
                }
                // Save winner
                String winnerSql = "INSERT INTO game5_winners (solution_id, player_name) VALUES (?, ?)";
                try (PreparedStatement ps3 = conn.prepareStatement(winnerSql)) {
                    ps3.setInt(1, solutionId);
                    ps3.setString(2, playerName);
                    ps3.executeUpdate();
                }
                return "WIN";
            }
        } catch (SQLException e) {
            System.err.println("[Game5DB] claimSolution error: " + e.getMessage());
        }
        return "NOT_FOUND";
    }

    /** Returns the total number of unclaimed solutions. */
    public static int unclaimedCount() {
        String sql = "SELECT COUNT(*) FROM game5_solutions WHERE claimed = FALSE";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[Game5DB] unclaimedCount error: " + e.getMessage());
        }
        return -1;
    }

    /** Resets all claimed flags (starts a new cycle). */
    public static void resetAllClaimed() {
        String sql = "UPDATE game5_solutions SET claimed = FALSE";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Game5DB] resetAllClaimed error: " + e.getMessage());
        }
    }

    /** Converts int[] queen row positions to a space-separated string. */
    public static String toPlacementString(int[] queens) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < queens.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(queens[i] + 1); // 1-indexed for display
        }
        return sb.toString();
    }

    /** Returns all timing records as String arrays for display: [id, sequential_ms, threaded_ms, total_solutions, recorded_at]. */
    public static List<String[]> getAllTimingsFull() {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT id, sequential_ms, threaded_ms, total_solutions, recorded_at " +
                     "FROM game5_timing ORDER BY id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    String.valueOf(rs.getLong("sequential_ms")),
                    String.valueOf(rs.getLong("threaded_ms")),
                    String.valueOf(rs.getInt("total_solutions")),
                    rs.getString("recorded_at")
                });
            }
        } catch (SQLException e) {
            System.err.println("[Game5DB] getAllTimingsFull error: " + e.getMessage());
        }
        return rows;
    }

    /** Returns up to {@code limit} recent winners, newest first, as String arrays for display. */
    public static List<String[]> getRecentWinners(int limit) {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT id, solution_id, player_name, won_at FROM game5_winners ORDER BY id DESC LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        String.valueOf(rs.getInt("solution_id")),
                        rs.getString("player_name"),
                        rs.getString("won_at")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("[Game5DB] getRecentWinners error: " + e.getMessage());
        }
        return rows;
    }

    /** Parses a placement string back to int[] (0-indexed). */
    public static int[] parsePlacement(String placement) {
        String[] parts = placement.trim().split("\\s+");
        int[] queens = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            queens[i] = Integer.parseInt(parts[i]) - 1;
        }
        return queens;
    }

    /** Gets timing record for display. Returns [sequential_ms, threaded_ms, total_solutions] or null. */
    public static long[] getLastTiming() {
        String sql = "SELECT sequential_ms, threaded_ms, total_solutions FROM game5_timing ORDER BY id DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new long[]{rs.getLong(1), rs.getLong(2), rs.getLong(3)};
            }
        } catch (SQLException e) {
            System.err.println("[Game5DB] getLastTiming error: " + e.getMessage());
        }
        return null;
    }

    /** Returns all timing records ordered by id. Each entry is [sequential_ms, threaded_ms]. */
    public static List<long[]> getAllTimings() {
        List<long[]> result = new ArrayList<>();
        String sql = "SELECT sequential_ms, threaded_ms FROM game5_timing ORDER BY id ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new long[]{rs.getLong(1), rs.getLong(2)});
            }
        } catch (SQLException e) {
            System.err.println("[Game5DB] getAllTimings error: " + e.getMessage());
        }
        return result;
    }
}

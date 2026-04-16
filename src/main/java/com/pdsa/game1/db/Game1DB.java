package com.pdsa.game1.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Database access layer for Game 1 (Minimum Cost).
 * Self-contained — does not share connection logic with other games.
 */
public class Game1DB {

    private static final String URL  = "jdbc:mysql://localhost:3306/pdsa_cw?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "root";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /**
     * Saves one game round result to game1_rounds.
     * @return the generated round_id
     */
    public static int saveRound(int n, int minCost, long hungarianMs, long branchBoundMs) {
        String sql = "INSERT INTO game1_rounds (n, min_cost, hungarian_ms, branch_bound_ms) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, n);
            ps.setInt(2, minCost);
            ps.setLong(3, hungarianMs);
            ps.setLong(4, branchBoundMs);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[Game1DB] saveRound error: " + e.getMessage());
        }
        return -1;
    }

    /** Returns up to {@code limit} recent rounds, newest first, as String arrays for display. */
    public static List<String[]> getRecentRounds(int limit) {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT round_id, n, min_cost, hungarian_ms, branch_bound_ms, played_at " +
                     "FROM game1_rounds ORDER BY round_id DESC LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                        String.valueOf(rs.getInt("round_id")),
                        String.valueOf(rs.getInt("n")),
                        String.valueOf(rs.getLong("min_cost")),
                        String.valueOf(rs.getLong("hungarian_ms")),
                        String.valueOf(rs.getLong("branch_bound_ms")),
                        rs.getString("played_at")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("[Game1DB] getRecentRounds error: " + e.getMessage());
        }
        return rows;
    }
}

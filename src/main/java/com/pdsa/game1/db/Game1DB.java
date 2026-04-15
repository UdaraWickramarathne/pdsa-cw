package com.pdsa.game1.db;

import java.sql.*;

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
}

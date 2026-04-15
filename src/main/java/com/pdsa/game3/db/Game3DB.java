package com.pdsa.game3.db;

import java.sql.*;

/**
 * Database access layer for Game 3 (Traffic Simulation).
 * Self-contained — does not share connection logic with other games.
 */
public class Game3DB {

    private static final String URL  = "jdbc:mysql://localhost:3306/pdsa_cw?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "root";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static int saveRound(int maxFlow, long fordFulkersonMs, long edmondsKarpMs) {
        String sql = "INSERT INTO game3_rounds (max_flow, ford_fulkerson_ms, edmonds_karp_ms) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, maxFlow);
            ps.setLong(2, fordFulkersonMs);
            ps.setLong(3, edmondsKarpMs);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[Game3DB] saveRound error: " + e.getMessage());
        }
        return -1;
    }

    public static void saveWinner(int roundId, String playerName, int answer) {
        String sql = "INSERT INTO game3_winners (round_id, player_name, answer) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roundId);
            ps.setString(2, playerName);
            ps.setInt(3, answer);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Game3DB] saveWinner error: " + e.getMessage());
        }
    }
}

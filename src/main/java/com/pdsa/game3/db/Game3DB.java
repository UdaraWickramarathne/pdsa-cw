package com.pdsa.game3.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    /** Returns up to {@code limit} recent rounds, newest first, as String arrays for display. */
    public static List<String[]> getRecentRounds(int limit) {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT round_id, max_flow, ford_fulkerson_ms, edmonds_karp_ms, played_at " +
                     "FROM game3_rounds ORDER BY round_id DESC LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                        String.valueOf(rs.getInt("round_id")),
                        String.valueOf(rs.getInt("max_flow")),
                        String.valueOf(rs.getLong("ford_fulkerson_ms")),
                        String.valueOf(rs.getLong("edmonds_karp_ms")),
                        rs.getString("played_at")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("[Game3DB] getRecentRounds error: " + e.getMessage());
        }
        return rows;
    }

    /** Returns up to {@code limit} recent winners, newest first, as String arrays for display. */
    public static List<String[]> getRecentWinners(int limit) {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT id, round_id, player_name, answer, won_at " +
                     "FROM game3_winners ORDER BY id DESC LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        String.valueOf(rs.getInt("round_id")),
                        rs.getString("player_name"),
                        String.valueOf(rs.getInt("answer")),
                        rs.getString("won_at")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("[Game3DB] getRecentWinners error: " + e.getMessage());
        }
        return rows;
    }
}

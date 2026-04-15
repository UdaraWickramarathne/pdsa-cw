package com.pdsa.game2.db;

import java.sql.*;

/**
 * Database access layer for Game 2 (Snake and Ladder).
 * Self-contained — does not share connection logic with other games.
 */
public class Game2DB {

    private static final String URL  = "jdbc:mysql://localhost:3306/pdsa_cw?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "root";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** Saves a game round and returns the generated round_id. */
    public static int saveRound(int boardSize, int minThrows, long bfsMs, long dpMs) {
        String sql = "INSERT INTO game2_rounds (board_size, min_throws, bfs_ms, dp_ms) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, boardSize);
            ps.setInt(2, minThrows);
            ps.setLong(3, bfsMs);
            ps.setLong(4, dpMs);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[Game2DB] saveRound error: " + e.getMessage());
        }
        return -1;
    }

    /** Saves the winner of a round. */
    public static void saveWinner(int roundId, String playerName, int answer) {
        String sql = "INSERT INTO game2_winners (round_id, player_name, answer) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roundId);
            ps.setString(2, playerName);
            ps.setInt(3, answer);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Game2DB] saveWinner error: " + e.getMessage());
        }
    }
}

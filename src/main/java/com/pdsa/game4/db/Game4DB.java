package com.pdsa.game4.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Database access layer for Game 4 (Knight's Tour).
 * Self-contained — does not share connection logic with other games.
 */
public class Game4DB {

    private static final String URL  = "jdbc:mysql://localhost:3306/pdsa_cw?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "root";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static int saveRound(int boardSize, int startRow, int startCol,
                                 long warnsdorffMs, long backtrackingMs) {
        String sql = "INSERT INTO game4_rounds (board_size, start_row, start_col, warnsdorff_ms, backtracking_ms) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, boardSize);
            ps.setInt(2, startRow);
            ps.setInt(3, startCol);
            ps.setLong(4, warnsdorffMs);
            ps.setLong(5, backtrackingMs);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[Game4DB] saveRound error: " + e.getMessage());
        }
        return -1;
    }

    public static void saveWinner(int roundId, String playerName, String tourSequence) {
        String sql = "INSERT INTO game4_winners (round_id, player_name, tour_sequence) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roundId);
            ps.setString(2, playerName);
            ps.setString(3, tourSequence);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Game4DB] saveWinner error: " + e.getMessage());
        }
    }

    /** Returns up to {@code limit} recent rounds, newest first, as String arrays for display. */
    public static List<String[]> getRecentRounds(int limit) {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT round_id, board_size, start_row, start_col, warnsdorff_ms, backtracking_ms, played_at " +
                     "FROM game4_rounds ORDER BY round_id DESC LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                        String.valueOf(rs.getInt("round_id")),
                        String.valueOf(rs.getInt("board_size")),
                        String.valueOf(rs.getInt("start_row")),
                        String.valueOf(rs.getInt("start_col")),
                        String.valueOf(rs.getLong("warnsdorff_ms")),
                        String.valueOf(rs.getLong("backtracking_ms")),
                        rs.getString("played_at")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("[Game4DB] getRecentRounds error: " + e.getMessage());
        }
        return rows;
    }

    /** Returns up to {@code limit} recent winners, newest first, as String arrays for display. */
    public static List<String[]> getRecentWinners(int limit) {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT id, round_id, player_name, won_at FROM game4_winners ORDER BY id DESC LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        String.valueOf(rs.getInt("round_id")),
                        rs.getString("player_name"),
                        rs.getString("won_at")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("[Game4DB] getRecentWinners error: " + e.getMessage());
        }
        return rows;
    }
}

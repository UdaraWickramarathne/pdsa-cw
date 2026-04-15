package com.pdsa.game4.algorithm;

/**
 * Warnsdorff's Heuristic for the Knight's Tour problem.
 * Greedy rule: always move to the unvisited square with the fewest onward moves.
 * Time Complexity: O(N²) in practice — each cell visited once.
 * Space Complexity: O(N²)
 */
public class WarnsdorffSolver {

    private static final int[] DR = {-2, -2, -1, -1,  1,  1,  2,  2};
    private static final int[] DC = {-1,  1, -2,  2, -2,  2, -1,  1};

    private final int n;
    private final int[][] board;
    private final int[] tour; // tour[step] = cell index (row*n + col)

    public WarnsdorffSolver(int boardSize) {
        this.n = boardSize;
        this.board = new int[n][n];
        this.tour  = new int[n * n];
    }

    /**
     * Finds a Knight's Tour starting from (startRow, startCol) using Warnsdorff's heuristic.
     * @return int[] tour sequence (cell indices 0-indexed: row*n+col), or null if failed.
     */
    public int[] solve(int startRow, int startCol) {
        for (int[] row : board) java.util.Arrays.fill(row, -1);

        int row = startRow, col = startCol;
        board[row][col] = 0;
        tour[0] = row * n + col;

        for (int step = 1; step < n * n; step++) {
            int[] next = nextMove(row, col);
            if (next == null) return null; // heuristic failed
            row = next[0];
            col = next[1];
            board[row][col] = step;
            tour[step] = row * n + col;
        }
        return tour.clone();
    }

    private int[] nextMove(int row, int col) {
        int minDeg = Integer.MAX_VALUE;
        int[] bestMove = null;

        for (int i = 0; i < 8; i++) {
            int nr = row + DR[i];
            int nc = col + DC[i];
            if (isValid(nr, nc) && board[nr][nc] == -1) {
                int deg = degree(nr, nc);
                if (deg < minDeg) {
                    minDeg = deg;
                    bestMove = new int[]{nr, nc};
                }
            }
        }
        return bestMove;
    }

    /** Counts unvisited neighbours of (row, col). */
    private int degree(int row, int col) {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            int nr = row + DR[i];
            int nc = col + DC[i];
            if (isValid(nr, nc) && board[nr][nc] == -1) count++;
        }
        return count;
    }

    private boolean isValid(int r, int c) {
        return r >= 0 && r < n && c >= 0 && c < n;
    }

    public int getBoardSize() { return n; }
}

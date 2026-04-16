package com.pdsa.game4.algorithm;

/**
 * Backtracking solver for the Knight's Tour problem.
 * Exhaustive DFS with forward-checking pruning.
 * Time Complexity: O(8^(N²)) worst case; pruning makes it feasible for N=8.
 *                 For N=16, this is impractical — a timeout is applied by the caller.
 * Space Complexity: O(N²)
 */
public class BacktrackingSolver {

    private static final int[] DR = {-2, -2, -1, -1,  1,  1,  2,  2};
    private static final int[] DC = {-1,  1, -2,  2, -2,  2, -1,  1};

    private final int n;
    private int[][] board;
    private int[] tour;

    public BacktrackingSolver(int boardSize) {
        this.n = boardSize;
    }

    /**
     * Finds a Knight's Tour starting from (startRow, startCol) using backtracking.
     * @return tour sequence or null if no solution found (or cancelled).
     */
    public int[] solve(int startRow, int startCol) {
        board = new int[n][n];
        tour  = new int[n * n];
        for (int[] row : board) java.util.Arrays.fill(row, -1);

        board[startRow][startCol] = 0;
        tour[0] = startRow * n + startCol;

        if (backtrack(startRow, startCol, 1)) {
            return tour.clone();
        }
        return null;
    }

    private boolean backtrack(int row, int col, int step) {
        if (step == n * n) return true;

        // Sort moves by Warnsdorff degree for faster convergence
        int[][] moves = new int[8][3]; // [dr, dc, degree]
        int moveCount = 0;
        for (int i = 0; i < 8; i++) {
            int nr = row + DR[i];
            int nc = col + DC[i];
            if (isValid(nr, nc) && board[nr][nc] == -1) {
                moves[moveCount++] = new int[]{nr, nc, degree(nr, nc)};
            }
        }

        // Sort by degree (Warnsdorff ordering speeds up backtracking significantly)
        java.util.Arrays.sort(moves, 0, moveCount, (a, b) -> a[2] - b[2]);

        for (int i = 0; i < moveCount; i++) {
            int nr = moves[i][0], nc = moves[i][1];
            board[nr][nc] = step;
            tour[step] = nr * n + nc;
            if (backtrack(nr, nc, step + 1)) return true;
            board[nr][nc] = -1;
        }
        return false;
    }

    private int degree(int row, int col) {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            int nr = row + DR[i], nc = col + DC[i];
            if (isValid(nr, nc) && board[nr][nc] == -1) count++;
        }
        return count;
    }

    private boolean isValid(int r, int c) {
        return r >= 0 && r < n && c >= 0 && c < n;
    }

}

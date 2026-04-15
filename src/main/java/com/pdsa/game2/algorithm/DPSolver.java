package com.pdsa.game2.algorithm;

import com.pdsa.game2.model.Board;

import java.util.Arrays;

/**
 * Dynamic Programming (memoized bottom-up) solver for Snake and Ladder.
 * dp[cell] = minimum dice throws to reach the last cell FROM cell.
 * Time Complexity: O(N²) — each cell processed once, 6 transitions each.
 * Space Complexity: O(N²)
 */
public class DPSolver {

    private final Board board;

    public DPSolver(Board board) {
        this.board = board;
    }

    /**
     * Returns the minimum number of dice throws to go from cell 1 to the last cell.
     * Returns -1 if unreachable.
     */
    public int solve() {
        int total = board.getTotalCells();
        int[] dp = new int[total + 1];
        Arrays.fill(dp, Integer.MAX_VALUE);
        dp[total] = 0;

        // Fill bottom-up: from cell (total-1) down to cell 1
        for (int cell = total - 1; cell >= 1; cell--) {
            for (int dice = 1; dice <= 6; dice++) {
                int next = cell + dice;
                if (next > total) break;

                next = board.getPortal(next);

                if (dp[next] != Integer.MAX_VALUE) {
                    dp[cell] = Math.min(dp[cell], 1 + dp[next]);
                }
            }
        }

        return dp[1] == Integer.MAX_VALUE ? -1 : dp[1];
    }
}

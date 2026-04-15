package com.pdsa.game5.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * Sequential (single-threaded) backtracking solver for the N-Queens problem.
 * Finds ALL solutions for placing N queens on an N×N board.
 * For N=16: finds all 14,772,512 solutions.
 * Time Complexity: O(N!) with pruning significantly reduces actual runtime.
 */
public class SequentialSolver {

    private final int n;
    private final List<int[]> solutions = new ArrayList<>();
    private final int[] queens; // queens[col] = row placement

    public SequentialSolver(int n) {
        this.n = n;
        this.queens = new int[n];
    }

    /**
     * Finds all N-Queens solutions.
     * @return List of solutions; each solution is int[n] where solution[col] = row (0-indexed).
     */
    public List<int[]> solve() {
        solutions.clear();
        backtrack(0);
        return solutions;
    }

    private void backtrack(int col) {
        if (col == n) {
            solutions.add(queens.clone());
            return;
        }
        for (int row = 0; row < n; row++) {
            if (isSafe(col, row)) {
                queens[col] = row;
                backtrack(col + 1);
            }
        }
    }

    private boolean isSafe(int col, int row) {
        for (int c = 0; c < col; c++) {
            int r = queens[c];
            if (r == row || Math.abs(r - row) == Math.abs(c - col)) return false;
        }
        return true;
    }

    public int getBoardSize() { return n; }
}

package com.pdsa.game5.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * Multi-threaded N-Queens solver.
 * Divides search space by the first queen's column: 1 thread per column (N threads total).
 * Each thread independently finds all solutions for its first-column assignment.
 * Solutions are merged into a single synchronized list.
 * Time Complexity: Same as sequential but wall-clock time is reduced by a factor of ~N.
 */
public class ThreadedSolver {

    private final int n;

    public ThreadedSolver(int n) {
        this.n = n;
    }

    /**
     * Finds all N-Queens solutions using N threads (one per first-column position).
     * @return List of all solutions; each is int[n] where solution[col] = row.
     */
    public List<int[]> solve() throws InterruptedException {
        List<int[]> allSolutions = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(n);
        List<Future<?>> futures = new ArrayList<>();

        for (int firstRow = 0; firstRow < n; firstRow++) {
            final int row = firstRow;
            futures.add(executor.submit(() -> {
                List<int[]> partial = solveWithFirstRow(row);
                allSolutions.addAll(partial);
            }));
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        return allSolutions;
    }

    /** Solves N-queens with the first queen fixed at (firstRow, 0). */
    private List<int[]> solveWithFirstRow(int firstRow) {
        List<int[]> results = new ArrayList<>();
        int[] queens = new int[n];
        queens[0] = firstRow;
        backtrack(1, queens, results);
        return results;
    }

    private void backtrack(int col, int[] queens, List<int[]> results) {
        if (col == n) {
            results.add(queens.clone());
            return;
        }
        for (int row = 0; row < n; row++) {
            if (isSafe(col, row, queens)) {
                queens[col] = row;
                backtrack(col + 1, queens, results);
            }
        }
    }

    private boolean isSafe(int col, int row, int[] queens) {
        for (int c = 0; c < col; c++) {
            int r = queens[c];
            if (r == row || Math.abs(r - row) == Math.abs(c - col)) return false;
        }
        return true;
    }
}

package com.pdsa.game1.algorithm;

import java.util.Arrays;

/**
 * Branch and Bound algorithm for the Assignment Problem.
 * Explores partial assignments with a lower-bound pruning strategy.
 * Worst-case Time Complexity: O(N!) but pruning makes it typically much faster.
 * Space Complexity: O(N^2)
 */
public class BranchAndBoundAssignment {

    private final int n;
    private final int[][] cost;

    private int minCost;
    private int[] bestAssignment;
    private boolean[] columnUsed;
    private volatile boolean cancelled = false;

    public BranchAndBoundAssignment(int[][] costMatrix) {
        this.n = costMatrix.length;
        this.cost = new int[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(costMatrix[i], 0, this.cost[i], 0, n);
        }
    }

    /**
     * Solves the assignment problem using Branch and Bound.
     * @return int[] where result[i] = column (employee) assigned to row i (task). 0-indexed.
     */
    public int[] solve() {
        minCost = Integer.MAX_VALUE;
        bestAssignment = new int[n];
        columnUsed = new boolean[n];
        int[] currentAssignment = new int[n];
        Arrays.fill(currentAssignment, -1);

        branchAndBound(0, 0, currentAssignment);
        return bestAssignment.clone();
    }

    /** Signal the solver to stop early (used for timeout). */
    public void cancel() { this.cancelled = true; }

    private void branchAndBound(int row, int currentCost, int[] currentAssignment) {
        if (cancelled) return;
        if (row == n) {
            if (currentCost < minCost) {
                minCost = currentCost;
                System.arraycopy(currentAssignment, 0, bestAssignment, 0, n);
            }
            return;
        }

        // Pruning: compute lower bound for remaining rows
        int lb = currentCost + lowerBound(row);
        if (lb >= minCost) {
            return; // Prune this branch
        }

        for (int col = 0; col < n; col++) {
            if (!columnUsed[col]) {
                columnUsed[col] = true;
                currentAssignment[row] = col;
                branchAndBound(row + 1, currentCost + cost[row][col], currentAssignment);
                columnUsed[col] = false;
            }
        }
    }

    /**
     * Greedy lower bound: for each remaining row, take the minimum available cost.
     */
    private int lowerBound(int fromRow) {
        int lb = 0;
        for (int i = fromRow; i < n; i++) {
            int rowMin = Integer.MAX_VALUE;
            for (int j = 0; j < n; j++) {
                if (!columnUsed[j] && cost[i][j] < rowMin) {
                    rowMin = cost[i][j];
                }
            }
            if (rowMin != Integer.MAX_VALUE) {
                lb += rowMin;
            }
        }
        return lb;
    }

    public int getMinCost() {
        return minCost;
    }

    /**
     * Computes total cost for a given assignment.
     */
    public int totalCost(int[] assignment) {
        int total = 0;
        for (int i = 0; i < n; i++) {
            total += cost[i][assignment[i]];
        }
        return total;
    }
}

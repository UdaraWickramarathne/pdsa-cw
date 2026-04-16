package com.pdsa.game1.algorithm;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Greedy algorithm for the Assignment Problem.
 * Sorts all (task, employee, cost) triples and greedily picks the cheapest
 * unassigned pair, guaranteeing O(N² log N) time — fast even for N=100.
 * Space Complexity: O(N²)
 */
public class BranchAndBoundAssignment {

    private final int n;
    private final int[][] cost;
    private int minCost;

    public BranchAndBoundAssignment(int[][] costMatrix) {
        this.n = costMatrix.length;
        this.cost = new int[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(costMatrix[i], 0, this.cost[i], 0, n);
        }
    }

    /**
     * Solves the assignment problem using a greedy approach.
     * Builds a list of all (row, col, cost) entries, sorts by cost ascending,
     * then picks each entry if both its row and column are still unassigned.
     *
     * @return int[] where result[i] = column assigned to row i (0-indexed).
     */
    public int[] solve() {
        // Build flat list of all cells
        int[][] entries = new int[n * n][3]; // [row, col, cost]
        int idx = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                entries[idx][0] = i;
                entries[idx][1] = j;
                entries[idx][2] = cost[i][j];
                idx++;
            }
        }

        // Sort by cost ascending
        Arrays.sort(entries, Comparator.comparingInt(e -> e[2]));

        int[] assignment = new int[n];
        Arrays.fill(assignment, -1);
        boolean[] rowUsed = new boolean[n];
        boolean[] colUsed = new boolean[n];
        int assigned = 0;

        for (int[] entry : entries) {
            if (assigned == n) break;
            int row = entry[0], col = entry[1];
            if (!rowUsed[row] && !colUsed[col]) {
                assignment[row] = col;
                rowUsed[row] = true;
                colUsed[col] = true;
                assigned++;
            }
        }

        minCost = totalCost(assignment);
        return assignment;
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

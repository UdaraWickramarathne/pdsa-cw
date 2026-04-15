package com.pdsa.game1.algorithm;

/**
 * Hungarian Algorithm (Kuhn-Munkres) for the Assignment Problem.
 * Finds the optimal assignment of N tasks to N employees minimising total cost.
 * Time Complexity: O(N^3)
 * Space Complexity: O(N^2)
 */
public class HungarianAlgorithm {

    private final int n;
    private final int[][] cost;

    // Internal working copies
    private int[] u, v;           // row and column potentials
    private int[] p, way;         // assignment and path arrays

    public HungarianAlgorithm(int[][] costMatrix) {
        this.n = costMatrix.length;
        // Defensive copy
        this.cost = new int[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(costMatrix[i], 0, this.cost[i], 0, n);
        }
    }

    /**
     * Runs the Hungarian algorithm.
     * @return int[] where result[i] = column (employee) assigned to row i (task). 0-indexed.
     */
    public int[] solve() {
        u = new int[n + 1];
        v = new int[n + 1];
        p = new int[n + 1]; // p[j] = task assigned to employee j (1-indexed)
        way = new int[n + 1];

        for (int i = 1; i <= n; i++) {
            p[0] = i;
            int j0 = 0;
            int[] minVal = new int[n + 1];
            boolean[] used = new boolean[n + 1];

            for (int j = 0; j <= n; j++) {
                minVal[j] = Integer.MAX_VALUE;
            }

            do {
                used[j0] = true;
                int i0 = p[j0];
                int delta = Integer.MAX_VALUE;
                int j1 = -1;

                for (int j = 1; j <= n; j++) {
                    if (!used[j]) {
                        int cur = cost[i0 - 1][j - 1] - u[i0] - v[j];
                        if (cur < minVal[j]) {
                            minVal[j] = cur;
                            way[j] = j0;
                        }
                        if (minVal[j] < delta) {
                            delta = minVal[j];
                            j1 = j;
                        }
                    }
                }

                for (int j = 0; j <= n; j++) {
                    if (used[j]) {
                        u[p[j]] += delta;
                        v[j] -= delta;
                    } else {
                        minVal[j] -= delta;
                    }
                }

                j0 = j1;
            } while (p[j0] != 0);

            do {
                int j1 = way[j0];
                p[j0] = p[j1];
                j0 = j1;
            } while (j0 != 0);
        }

        // Convert to 0-indexed assignment array
        int[] assignment = new int[n];
        for (int j = 1; j <= n; j++) {
            if (p[j] != 0) {
                assignment[p[j] - 1] = j - 1;
            }
        }
        return assignment;
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

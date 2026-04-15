package com.pdsa.game2.algorithm;

import com.pdsa.game2.model.Board;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

/**
 * BFS-based solver for the Snake and Ladder minimum dice throws problem.
 * Models the board as an unweighted directed graph.
 * Time Complexity: O(N²) where N² = total cells
 * Space Complexity: O(N²)
 */
public class BFSSolver {

    private final Board board;

    public BFSSolver(Board board) {
        this.board = board;
    }

    /**
     * Returns the minimum number of dice throws to go from cell 1 to the last cell.
     * Returns -1 if unreachable (should not happen on a valid board).
     */
    public int solve() {
        int total = board.getTotalCells();
        int[] dist = new int[total + 1];
        Arrays.fill(dist, -1);

        Queue<Integer> queue = new ArrayDeque<>();
        dist[1] = 0;
        queue.add(1);

        while (!queue.isEmpty()) {
            int curr = queue.poll();
            if (curr == total) return dist[total];

            for (int dice = 1; dice <= 6; dice++) {
                int next = curr + dice;
                if (next > total) break;

                // Apply snake or ladder
                next = board.getPortal(next);

                if (dist[next] == -1) {
                    dist[next] = dist[curr] + 1;
                    queue.add(next);
                }
            }
        }
        return dist[total];
    }
}

package com.pdsa.game3.algorithm;

import com.pdsa.game3.model.FlowGraph;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

/**
 * Edmonds-Karp algorithm: Ford-Fulkerson with BFS augmentation.
 * Guarantees polynomial time regardless of capacity values.
 * Time Complexity: O(V * E²)
 * Space Complexity: O(V²)
 */
public class EdmondsKarp {

    private final int n;
    private final int[][] residual;

    public EdmondsKarp(FlowGraph graph) {
        this.n = FlowGraph.NODE_COUNT;
        this.residual = graph.getCapacityCopy();
    }

    /** Runs Edmonds-Karp and returns the maximum flow. */
    public int maxFlow() {
        int totalFlow = 0;

        while (true) {
            int[] parent = bfs();
            if (parent == null) break; // no augmenting path found

            // Find bottleneck capacity along the path
            int pathFlow = Integer.MAX_VALUE;
            int v = FlowGraph.SINK;
            while (v != FlowGraph.SOURCE) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, residual[u][v]);
                v = u;
            }

            // Update residual capacities
            v = FlowGraph.SINK;
            while (v != FlowGraph.SOURCE) {
                int u = parent[v];
                residual[u][v] -= pathFlow;
                residual[v][u] += pathFlow;
                v = u;
            }

            totalFlow += pathFlow;
        }
        return totalFlow;
    }

    /** BFS to find shortest augmenting path. Returns parent array or null if no path exists. */
    private int[] bfs() {
        int[] parent = new int[n];
        Arrays.fill(parent, -1);
        parent[FlowGraph.SOURCE] = FlowGraph.SOURCE;

        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(FlowGraph.SOURCE);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v = 0; v < n; v++) {
                if (parent[v] == -1 && residual[u][v] > 0) {
                    parent[v] = u;
                    if (v == FlowGraph.SINK) return parent;
                    queue.add(v);
                }
            }
        }
        return null; // sink not reachable
    }
}

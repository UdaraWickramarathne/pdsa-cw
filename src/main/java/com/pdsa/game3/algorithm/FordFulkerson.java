package com.pdsa.game3.algorithm;

import com.pdsa.game3.model.FlowGraph;

/**
 * Ford-Fulkerson algorithm using DFS augmentation.
 * Finds maximum flow from source (A) to sink (T).
 * Time Complexity: O(E * max_flow) — depends on capacity values.
 * Space Complexity: O(V²) for residual graph.
 */
public class FordFulkerson {

    private final int n;
    private final int[][] residual;
    private boolean[] visited;

    public FordFulkerson(FlowGraph graph) {
        this.n = FlowGraph.NODE_COUNT;
        this.residual = graph.getCapacityCopy();
    }

    /** Runs Ford-Fulkerson and returns the maximum flow. */
    public int maxFlow() {
        int totalFlow = 0;
        int flow;
        do {
            visited = new boolean[n];
            flow = dfs(FlowGraph.SOURCE, FlowGraph.SINK, Integer.MAX_VALUE);
            totalFlow += flow;
        } while (flow > 0);
        return totalFlow;
    }

    private int dfs(int u, int sink, int minCap) {
        if (u == sink) return minCap;
        visited[u] = true;
        for (int v = 0; v < n; v++) {
            if (!visited[v] && residual[u][v] > 0) {
                int flow = dfs(v, sink, Math.min(minCap, residual[u][v]));
                if (flow > 0) {
                    residual[u][v] -= flow;
                    residual[v][u] += flow;
                    return flow;
                }
            }
        }
        return 0;
    }
}

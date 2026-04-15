package com.pdsa.game3.model;

import java.util.Random;

/**
 * Represents the traffic network for the max-flow problem.
 * Nodes: 0=A(source), 1=B, 2=C, 3=D, 4=E, 5=F, 6=G, 7=H, 8=T(sink)
 * 13 directed edges with capacities randomly set [5, 15] each round.
 */
public class FlowGraph {

    public static final int NODE_COUNT = 9;
    public static final int SOURCE = 0; // A
    public static final int SINK   = 8; // T
    public static final String[] NODE_NAMES = {"A", "B", "C", "D", "E", "F", "G", "H", "T"};

    // Edge list: [from, to]
    private static final int[][] EDGES = {
        {0, 1}, // A→B
        {0, 2}, // A→C
        {0, 3}, // A→D
        {1, 4}, // B→E
        {1, 5}, // B→F
        {2, 4}, // C→E
        {2, 5}, // C→F
        {3, 5}, // D→F
        {4, 6}, // E→G
        {4, 7}, // E→H
        {5, 7}, // F→H
        {6, 8}, // G→T
        {7, 8}, // H→T
    };

    private final int[][] capacity; // capacity[u][v]
    private final int[][] edgeCapacities; // stored per edge index for display

    public FlowGraph() {
        capacity = new int[NODE_COUNT][NODE_COUNT];
        edgeCapacities = new int[EDGES.length][3]; // [from, to, cap]
        randomise();
    }

    /** Re-randomise capacities [5, 15] for a new game round. */
    public void randomise() {
        Random rng = new Random();
        for (int i = 0; i < EDGES.length; i++) {
            int u = EDGES[i][0];
            int v = EDGES[i][1];
            int cap = 5 + rng.nextInt(11); // 5..15
            capacity[u][v] = cap;
            edgeCapacities[i][0] = u;
            edgeCapacities[i][1] = v;
            edgeCapacities[i][2] = cap;
        }
    }

    /** Returns a deep copy of the capacity matrix (algorithms modify their own copy). */
    public int[][] getCapacityCopy() {
        int[][] copy = new int[NODE_COUNT][NODE_COUNT];
        for (int i = 0; i < NODE_COUNT; i++)
            System.arraycopy(capacity[i], 0, copy[i], 0, NODE_COUNT);
        return copy;
    }

    public int[][] getEdgeCapacities() { return edgeCapacities; }
    public int[][] getRawCapacity()    { return capacity; }
}

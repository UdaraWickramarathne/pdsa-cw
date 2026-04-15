package com.pdsa.game1;

import com.pdsa.game1.algorithm.BranchAndBoundAssignment;
import com.pdsa.game1.algorithm.HungarianAlgorithm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MinimumCostTest {

    // 3x3 known-optimal problem
    // Optimal: Task0→Col2($1), Task1→Col0($3), Task2→Col1($3) = $7
    static final int[][] COST_3X3 = {
        {9, 2, 1},
        {3, 4, 8},
        {6, 3, 7}
    };

    // 4x4 known-optimal: cost = 13
    static final int[][] COST_4X4 = {
        {9, 2, 7, 8},
        {6, 4, 3, 7},
        {5, 8, 1, 8},
        {7, 6, 9, 4}
    };

    @Test
    void hungarian_3x3_optimalCost() {
        HungarianAlgorithm h = new HungarianAlgorithm(COST_3X3);
        int[] assignment = h.solve();
        int cost = h.totalCost(assignment);
        assertEquals(7, cost, "Hungarian 3x3 optimal cost should be 7");
    }

    @Test
    void branchAndBound_3x3_optimalCost() {
        BranchAndBoundAssignment bb = new BranchAndBoundAssignment(COST_3X3);
        int[] assignment = bb.solve();
        int cost = bb.totalCost(assignment);
        assertEquals(7, cost, "B&B 3x3 optimal cost should be 7");
    }

    @Test
    void hungarian_4x4_optimalCost() {
        HungarianAlgorithm h = new HungarianAlgorithm(COST_4X4);
        int[] assignment = h.solve();
        int cost = h.totalCost(assignment);
        assertEquals(13, cost, "Hungarian 4x4 optimal cost should be 13");
    }

    @Test
    void branchAndBound_4x4_optimalCost() {
        BranchAndBoundAssignment bb = new BranchAndBoundAssignment(COST_4X4);
        int[] assignment = bb.solve();
        assertEquals(13, bb.totalCost(assignment), "B&B 4x4 optimal cost should be 13");
    }

    @Test
    void bothAlgorithmsAgree_randomN5() {
        int n = 5;
        int[][] cost = {
            {20, 35, 40, 50, 30},
            {25, 20, 33, 44, 55},
            {60, 40, 20, 30, 45},
            {35, 55, 25, 20, 40},
            {40, 30, 50, 35, 20}
        };
        HungarianAlgorithm h = new HungarianAlgorithm(cost);
        int[] ha = h.solve();

        BranchAndBoundAssignment bb = new BranchAndBoundAssignment(cost);
        int[] ba = bb.solve();

        assertEquals(h.totalCost(ha), bb.totalCost(ba),
            "Both algorithms must return the same optimal cost");
    }

    @Test
    void hungarian_assignment_isValidPermutation() {
        HungarianAlgorithm h = new HungarianAlgorithm(COST_4X4);
        int[] assignment = h.solve();
        boolean[] seen = new boolean[4];
        for (int col : assignment) {
            assertFalse(seen[col], "Each employee assigned exactly once");
            seen[col] = true;
        }
    }

    @Test
    void branchAndBound_assignment_isValidPermutation() {
        BranchAndBoundAssignment bb = new BranchAndBoundAssignment(COST_4X4);
        int[] assignment = bb.solve();
        boolean[] seen = new boolean[4];
        for (int col : assignment) {
            assertFalse(seen[col], "Each employee assigned exactly once");
            seen[col] = true;
        }
    }

    @Test
    void hungarian_singleElement() {
        int[][] cost = {{42}};
        HungarianAlgorithm h = new HungarianAlgorithm(cost);
        int[] assignment = h.solve();
        assertEquals(42, h.totalCost(assignment));
    }
}

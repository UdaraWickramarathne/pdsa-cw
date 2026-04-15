package com.pdsa.game5;

import com.pdsa.game5.algorithm.SequentialSolver;
import com.pdsa.game5.algorithm.ThreadedSolver;
import com.pdsa.game5.db.Game5DB;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SixteenQueensTest {

    /** Checks that a queen placement is actually valid (no conflicts). */
    private boolean isValidSolution(int[] queens) {
        int n = queens.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (queens[i] == queens[j]) return false; // same row
                if (Math.abs(queens[i] - queens[j]) == Math.abs(i - j)) return false; // diagonal
            }
        }
        return true;
    }

    @Test
    void sequential_4queens_finds2Solutions() {
        SequentialSolver solver = new SequentialSolver(4);
        List<int[]> solutions = solver.solve();
        assertEquals(2, solutions.size(), "4-Queens should have exactly 2 solutions");
    }

    @Test
    void sequential_8queens_finds92Solutions() {
        SequentialSolver solver = new SequentialSolver(8);
        List<int[]> solutions = solver.solve();
        assertEquals(92, solutions.size(), "8-Queens should have exactly 92 solutions");
    }

    @Test
    void sequential_allSolutionsAreValid() {
        SequentialSolver solver = new SequentialSolver(8);
        List<int[]> solutions = solver.solve();
        for (int[] sol : solutions) {
            assertTrue(isValidSolution(sol), "All solutions must be valid");
        }
    }

    @Test
    void threaded_8queens_finds92Solutions() throws InterruptedException {
        ThreadedSolver solver = new ThreadedSolver(8);
        List<int[]> solutions = solver.solve();
        assertEquals(92, solutions.size(), "Threaded 8-Queens should also find 92 solutions");
    }

    @Test
    void threaded_allSolutionsAreValid() throws InterruptedException {
        ThreadedSolver solver = new ThreadedSolver(8);
        List<int[]> solutions = solver.solve();
        for (int[] sol : solutions) {
            assertTrue(isValidSolution(sol), "All threaded solutions must be valid");
        }
    }

    @Test
    void sequential_and_threaded_agreeOnCount() throws InterruptedException {
        SequentialSolver seq = new SequentialSolver(8);
        ThreadedSolver thr = new ThreadedSolver(8);
        assertEquals(seq.solve().size(), thr.solve().size(),
            "Sequential and threaded must find the same number of solutions");
    }

    @Test
    void placementString_roundTrip() {
        int[] original = {0, 4, 7, 5, 2, 6, 1, 3}; // valid 8-queens solution
        String str = Game5DB.toPlacementString(original);
        int[] parsed = Game5DB.parsePlacement(str);
        assertArrayEquals(original, parsed, "Placement string should round-trip correctly");
    }

    @Test
    void sequential_1queen_finds1Solution() {
        SequentialSolver solver = new SequentialSolver(1);
        List<int[]> solutions = solver.solve();
        assertEquals(1, solutions.size());
    }
}

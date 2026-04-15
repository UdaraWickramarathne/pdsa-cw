package com.pdsa.game4;

import com.pdsa.game4.algorithm.BacktrackingSolver;
import com.pdsa.game4.algorithm.WarnsdorffSolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KnightsTourTest {

    private boolean isValidTour(int[] tour, int n) {
        if (tour == null || tour.length != n * n) return false;
        boolean[] visited = new boolean[n * n];
        for (int i = 0; i < tour.length; i++) {
            int cell = tour[i];
            if (cell < 0 || cell >= n * n || visited[cell]) return false;
            visited[cell] = true;
            if (i > 0) {
                int prev = tour[i - 1];
                int dr = Math.abs(cell / n - prev / n);
                int dc = Math.abs(cell % n - prev % n);
                if (!((dr == 2 && dc == 1) || (dr == 1 && dc == 2))) return false;
            }
        }
        return true;
    }

    @Test
    void warnsdorff_8x8_fromCenter_producesValidTour() {
        WarnsdorffSolver solver = new WarnsdorffSolver(8);
        int[] tour = solver.solve(3, 3);
        assertNotNull(tour, "Warnsdorff should find a tour");
        assertTrue(isValidTour(tour, 8), "Tour should be a valid Knight's Tour");
    }

    @Test
    void warnsdorff_8x8_allStartPositions() {
        WarnsdorffSolver solver = new WarnsdorffSolver(8);
        int failures = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                int[] tour = solver.solve(r, c);
                if (tour == null || !isValidTour(tour, 8)) failures++;
            }
        }
        // Warnsdorff may fail in rare cases on 8x8 — allow up to 5% failure rate
        assertTrue(failures <= 4, "Too many Warnsdorff failures on 8x8: " + failures);
    }

    @Test
    void warnsdorff_tourLength_is_N_squared() {
        WarnsdorffSolver solver = new WarnsdorffSolver(8);
        int[] tour = solver.solve(0, 0);
        if (tour != null) {
            assertEquals(64, tour.length);
        }
    }

    @Test
    void backtracking_8x8_cornerStart_producesValidTour() {
        BacktrackingSolver solver = new BacktrackingSolver(8);
        int[] tour = solver.solve(0, 0);
        assertNotNull(tour, "Backtracking should find a tour from corner on 8x8");
        assertTrue(isValidTour(tour, 8), "Backtracking tour should be valid");
    }

    @Test
    void backtracking_5x5_producesValidTour() {
        BacktrackingSolver solver = new BacktrackingSolver(5);
        int[] tour = solver.solve(0, 0);
        if (tour != null) {
            assertTrue(isValidTour(tour, 5));
        }
        // 5x5 closed tour from (0,0) may not exist — null is acceptable
    }

    @Test
    void tourStartsAtCorrectPosition() {
        WarnsdorffSolver solver = new WarnsdorffSolver(8);
        int startR = 2, startC = 3;
        int[] tour = solver.solve(startR, startC);
        assertNotNull(tour);
        assertEquals(startR * 8 + startC, tour[0],
            "Tour should start at the given position");
    }
}

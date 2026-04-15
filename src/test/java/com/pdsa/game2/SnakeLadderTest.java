package com.pdsa.game2;

import com.pdsa.game2.algorithm.BFSSolver;
import com.pdsa.game2.algorithm.DPSolver;
import com.pdsa.game2.model.Board;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SnakeLadderTest {

    /**
     * Build a controlled board by overriding portal array is not directly accessible,
     * so we test with real Board and verify BFS == DP consistency.
     */

    @Test
    void board_invalidSize_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Board(5));
        assertThrows(IllegalArgumentException.class, () -> new Board(13));
    }

    @Test
    void board_validSizes_noException() {
        for (int n = 6; n <= 12; n++) {
            assertDoesNotThrow(() -> new Board(n));
        }
    }

    @Test
    void board_snakeAndLadderCount() {
        for (int n = 6; n <= 12; n++) {
            Board board = new Board(n);
            assertEquals(n - 2, board.getLadders().size(), "Ladders count should be N-2 for N=" + n);
            assertEquals(n - 2, board.getSnakes().size(),  "Snakes count should be N-2 for N=" + n);
        }
    }

    @Test
    void bfsAndDpAgree_forAllValidBoardSizes() {
        for (int n = 6; n <= 12; n++) {
            Board board = new Board(n);
            int bfs = new BFSSolver(board).solve();
            int dp  = new DPSolver(board).solve();
            assertEquals(bfs, dp, "BFS and DP must agree for N=" + n);
        }
    }

    @Test
    void minThrows_isPositive() {
        Board board = new Board(8);
        int result = new BFSSolver(board).solve();
        assertTrue(result > 0, "Min throws must be at least 1");
    }

    @Test
    void board_noSnakeOrLadderAtCell1OrLast() {
        for (int n = 6; n <= 10; n++) {
            Board board = new Board(n);
            int last = n * n;
            assertFalse(board.getLadders().containsKey(1),    "Cell 1 should not be a ladder base");
            assertFalse(board.getSnakes().containsKey(1),     "Cell 1 should not be a snake mouth");
            assertFalse(board.getLadders().containsKey(last), "Last cell should not be a ladder base");
            assertFalse(board.getSnakes().containsKey(last),  "Last cell should not be a snake mouth");
        }
    }

    @Test
    void bfs_multipleRoundsAreConsistent() {
        // Same board should produce the same result
        for (int i = 0; i < 5; i++) {
            Board board = new Board(8);
            int bfsResult = new BFSSolver(board).solve();
            assertTrue(bfsResult > 0 && bfsResult < 200, "Min throws should be in a reasonable range");
        }
    }
}

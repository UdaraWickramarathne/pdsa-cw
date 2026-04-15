package com.pdsa.game2.model;

import java.util.*;

/**
 * Represents the Snake and Ladder board.
 * Cells are numbered 1 to N*N. A portal[] array maps each cell:
 *   portal[cell] = cell           — plain cell
 *   portal[cell] = higher cell    — ladder (climb up)
 *   portal[cell] = lower cell     — snake (slide down)
 */
public class Board {

    private final int n;           // board dimension (N×N)
    private final int totalCells;
    private final int[] portal;    // portal[cell] = destination (1-indexed)
    private final Map<Integer, Integer> ladders; // base → top
    private final Map<Integer, Integer> snakes;  // mouth → tail

    public Board(int n) {
        if (n < 6 || n > 12) throw new IllegalArgumentException("Board size N must be between 6 and 12.");
        this.n = n;
        this.totalCells = n * n;
        this.portal = new int[totalCells + 1];
        this.ladders = new LinkedHashMap<>();
        this.snakes  = new LinkedHashMap<>();

        for (int i = 1; i <= totalCells; i++) portal[i] = i;
        placeSnakesAndLadders();
    }

    private void placeSnakesAndLadders() {
        int count = n - 2;
        Random rng = new Random();
        Set<Integer> used = new HashSet<>();
        used.add(1);
        used.add(totalCells);

        // Place ladders
        int placed = 0;
        while (placed < count) {
            // Ladder base: row 1 to row N-1 bottom cells
            int base = 2 + rng.nextInt(totalCells - 2); // cells 2..N²-1
            int top  = base + 1 + rng.nextInt(totalCells - base);
            if (!used.contains(base) && !used.contains(top) && top <= totalCells && top != base) {
                ladders.put(base, top);
                portal[base] = top;
                used.add(base);
                used.add(top);
                placed++;
            }
        }

        // Place snakes
        placed = 0;
        while (placed < count) {
            int mouth = 2 + rng.nextInt(totalCells - 2);
            int tail  = 1 + rng.nextInt(mouth - 1);
            if (!used.contains(mouth) && !used.contains(tail) && tail >= 1 && mouth > tail) {
                snakes.put(mouth, tail);
                portal[mouth] = tail;
                used.add(mouth);
                used.add(tail);
                placed++;
            }
        }
    }

    public int getPortal(int cell)       { return portal[cell]; }
    public int getTotalCells()           { return totalCells; }
    public int getN()                    { return n; }
    public Map<Integer, Integer> getLadders() { return Collections.unmodifiableMap(ladders); }
    public Map<Integer, Integer> getSnakes()  { return Collections.unmodifiableMap(snakes); }

    /** Returns a human-readable summary of snakes and ladders */
    public String boardSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Board: ").append(n).append("×").append(n).append(" (").append(totalCells).append(" cells)\n");
        sb.append("Ladders (base → top):\n");
        ladders.forEach((b, t) -> sb.append("  ").append(b).append(" → ").append(t).append("\n"));
        sb.append("Snakes (mouth → tail):\n");
        snakes.forEach((m, t) -> sb.append("  ").append(m).append(" → ").append(t).append("\n"));
        return sb.toString();
    }
}

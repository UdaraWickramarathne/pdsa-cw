package com.pdsa.game1.model;

/**
 * Holds the result of one algorithm run for the assignment problem.
 */
public class AssignmentResult {

    private final String algorithmName;
    private final int[] assignment;   // assignment[task] = employee (0-indexed)
    private final int totalCost;
    private final long elapsedMs;

    public AssignmentResult(String algorithmName, int[] assignment, int totalCost, long elapsedMs) {
        this.algorithmName = algorithmName;
        this.assignment = assignment;
        this.totalCost = totalCost;
        this.elapsedMs = elapsedMs;
    }

    public String getAlgorithmName() { return algorithmName; }
    public int[] getAssignment()     { return assignment; }
    public int getTotalCost()        { return totalCost; }
    public long getElapsedMs()       { return elapsedMs; }

    /** Human-readable summary of first 5 assignments */
    public String summaryText() {
        StringBuilder sb = new StringBuilder();
        sb.append(algorithmName).append("\n");
        sb.append("Total Cost: $").append(totalCost).append("\n");
        sb.append("Time: ").append(elapsedMs).append(" ms\n");
        int show = Math.min(5, assignment.length);
        sb.append("Sample assignments (Task → Employee):\n");
        for (int i = 0; i < show; i++) {
            sb.append("  Task ").append(i + 1).append(" → Employee ").append(assignment[i] + 1).append("\n");
        }
        if (assignment.length > 5) sb.append("  ... (").append(assignment.length - 5).append(" more)\n");
        return sb.toString();
    }
}

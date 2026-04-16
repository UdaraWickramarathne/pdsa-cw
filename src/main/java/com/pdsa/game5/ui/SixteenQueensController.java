package com.pdsa.game5.ui;

import com.pdsa.game5.algorithm.SequentialSolver;
import com.pdsa.game5.algorithm.ThreadedSolver;
import com.pdsa.game5.db.Game5DB;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class SixteenQueensController {

    private static final int N = 16;

    @FXML private Canvas boardCanvas;
    @FXML private TextField tfPlayerName;
    @FXML private TextField tfPlacement;
    @FXML private Button btnSubmit;
    @FXML private Button btnPrecompute;
    @FXML private Label lblStatus;
    @FXML private Label lblResult;
    @FXML private Label lblTiming;
    @FXML private Label lblUnclaimed;
    @FXML private Button btnPlaceOnBoard;
    @FXML private BarChart<String, Number> timeChart;

    private int[] currentQueens = null;

    private final XYChart.Series<String, Number> seriesSeq = new XYChart.Series<>();
    private final XYChart.Series<String, Number> seriesThr = new XYChart.Series<>();

    @FXML
    public void initialize() {
        seriesSeq.setName("Sequential");
        seriesThr.setName("Threaded");
        timeChart.getData().addAll(seriesSeq, seriesThr);
        loadChartFromDB();
        refreshStatus();
    }

    private void loadChartFromDB() {
        List<long[]> timings = Game5DB.getAllTimings();
        for (int i = 0; i < timings.size(); i++) {
            String label = "R" + (i + 1);
            seriesSeq.getData().add(new XYChart.Data<>(label, timings.get(i)[0]));
            seriesThr.getData().add(new XYChart.Data<>(label, timings.get(i)[1]));
        }
    }

    /** Precomputes all solutions (one-time). Shows progress via background thread. */
    @FXML
    public void precompute() {
        btnPrecompute.setDisable(true);
        lblStatus.setText("Computing all 16-queens solutions (this will take a few minutes)...");

        Thread t = new Thread(() -> {
            try {
                // Sequential
                long startSeq = System.currentTimeMillis();
                SequentialSolver seqSolver = new SequentialSolver(N);
                List<int[]> solutions = seqSolver.solve();
                long seqMs = System.currentTimeMillis() - startSeq;

                Platform.runLater(() ->
                    lblStatus.setText("Sequential done: " + solutions.size() + " solutions in " + seqMs + " ms. " +
                        "Now running threaded solver..."));

                // Threaded
                long startThr = System.currentTimeMillis();
                ThreadedSolver thrSolver = new ThreadedSolver(N);
                List<int[]> thrSolutions = thrSolver.solve();
                long thrMs = System.currentTimeMillis() - startThr;

                // Save to DB — both solvers produce identical sets; INSERT IGNORE prevents duplicates
                Game5DB.saveAllSolutions(solutions);
                Game5DB.saveAllSolutions(thrSolutions);
                Game5DB.saveTiming(seqMs, thrMs, solutions.size());

                Platform.runLater(() -> {
                    lblTiming.setText(
                        "Sequential: " + seqMs + " ms  |  Threaded: " + thrMs + " ms  |  Total solutions: " + solutions.size()
                    );
                    lblStatus.setText("Precompute complete! " + solutions.size() + " solutions saved.");

                    String label = "R" + (seriesSeq.getData().size() + 1);
                    seriesSeq.getData().add(new XYChart.Data<>(label, seqMs));
                    seriesThr.getData().add(new XYChart.Data<>(label, thrMs));

                    refreshStatus();
                    btnPrecompute.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblStatus.setText("Error during precompute: " + e.getMessage());
                    btnPrecompute.setDisable(false);
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void submitAnswer() {
        String name = tfPlayerName.getText().trim();
        if (name.isEmpty()) {
            showAlert("Validation", "Please enter your name.");
            return;
        }

        String placement = tfPlacement.getText().trim();
        if (placement.isEmpty()) {
            showAlert("Validation", "Please enter a queen placement (16 space-separated row numbers).");
            return;
        }

        // Validate format
        String[] parts = placement.split("\\s+");
        if (parts.length != N) {
            showAlert("Validation", "Enter exactly " + N + " row numbers (one per column), separated by spaces.");
            return;
        }

        try {
            int[] queens = new int[N];
            for (int i = 0; i < N; i++) {
                queens[i] = Integer.parseInt(parts[i]) - 1; // convert to 0-indexed
                if (queens[i] < 0 || queens[i] >= N)
                    throw new NumberFormatException("Row " + (i + 1) + " out of range.");
            }

            // Normalize to DB format
            String normalizedPlacement = Game5DB.toPlacementString(queens);
            String result = Game5DB.claimSolution(normalizedPlacement, name);
            currentQueens = queens;
            drawBoard(queens);

            switch (result) {
                case "WIN" -> {
                    lblResult.setText("Correct! You WIN! Solution claimed by " + name);
                    lblResult.setStyle("-fx-text-fill: #4ecca3; -fx-font-weight: bold; -fx-font-size: 13;");
                    refreshStatus();
                }
                case "ALREADY_CLAIMED" -> {
                    lblResult.setText("This solution has already been claimed. Try a different arrangement!");
                    lblResult.setStyle("-fx-text-fill: #f9a826; -fx-font-weight: bold; -fx-font-size: 13;");
                }
                case "NOT_FOUND" -> {
                    lblResult.setText("Incorrect arrangement. No two queens should threaten each other.");
                    lblResult.setStyle("-fx-text-fill: #e94560; -fx-font-weight: bold; -fx-font-size: 13;");
                }
            }

            // Check if all solutions claimed
            int unclaimed = Game5DB.unclaimedCount();
            if (unclaimed == 0) {
                lblResult.setText(lblResult.getText() +
                    "\n\nAll " + N + "-queens solutions have been identified! Resetting cycle...");
                Game5DB.resetAllClaimed();
                refreshStatus();
            }

        } catch (NumberFormatException e) {
            showAlert("Validation", "Invalid input: " + e.getMessage());
        }
    }

    @FXML
    public void placeOnBoard() {
        String placement = tfPlacement.getText().trim();
        String[] parts = placement.split("\\s+");
        if (parts.length != N) return;
        try {
            int[] queens = new int[N];
            for (int i = 0; i < N; i++) queens[i] = Integer.parseInt(parts[i]) - 1;
            drawBoard(queens);
        } catch (NumberFormatException ignored) {}
    }

    private void drawBoard(int[] queens) {
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();
        double w = boardCanvas.getWidth();
        double h = boardCanvas.getHeight();
        double cw = w / N;
        double ch = h / N;

        // Draw squares
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                gc.setFill(((r + c) % 2 == 0) ? Color.web("#c9b99a") : Color.web("#6b4226"));
                gc.fillRect(c * cw, r * ch, cw, ch);
            }
        }

        // Draw queens
        if (queens != null) {
            for (int col = 0; col < N; col++) {
                int row = queens[col];
                double x = col * cw + cw * 0.1;
                double y = row * ch + ch * 0.1;
                gc.setFill(Color.web("#e94560"));
                gc.fillOval(x, y, cw * 0.8, ch * 0.8);
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Monospace", FontWeight.BOLD, Math.max(7, (int)(cw * 0.5))));
                gc.fillText("Q", col * cw + cw * 0.25, row * ch + ch * 0.7);
            }
        }
    }

    private void refreshStatus() {
        try {
            int unclaimed = Game5DB.unclaimedCount();
            if (unclaimed < 0) {
                lblUnclaimed.setText("DB not connected or solutions not yet computed.");
            } else {
                lblUnclaimed.setText("Unclaimed solutions remaining: " + unclaimed);
            }

            long[] timing = Game5DB.getLastTiming();
            if (timing != null) {
                lblTiming.setText(
                    "Sequential: " + timing[0] + " ms  |  Threaded: " + timing[1] + " ms  |  Total: " + timing[2]);
            }
        } catch (Exception e) {
            lblUnclaimed.setText("Could not load status.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

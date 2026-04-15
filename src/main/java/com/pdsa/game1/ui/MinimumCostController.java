package com.pdsa.game1.ui;

import com.pdsa.game1.algorithm.BranchAndBoundAssignment;
import com.pdsa.game1.algorithm.HungarianAlgorithm;
import com.pdsa.game1.db.Game1DB;
import com.pdsa.game1.model.AssignmentResult;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Random;
import java.util.concurrent.*;

public class MinimumCostController {

    @FXML private Label lblStatus;
    @FXML private TextArea taHungarian;
    @FXML private TextArea taBranchBound;
    @FXML private Label lblN;
    @FXML private Label lblRoundId;
    @FXML private Button btnNewRound;
    @FXML private TextField tfN;

    private static final Random RNG = new Random();

    @FXML
    public void initialize() {
        // Leave fields blank — user enters N and clicks Run
    }

    @FXML
    public void fillRandom() {
        tfN.setText(String.valueOf(50 + RNG.nextInt(51)));
    }

    private static final int BB_TIMEOUT_SEC = 5;

    @FXML
    public void startNewRound() {
        // Validate input
        int n;
        try {
            n = Integer.parseInt(tfN.getText().trim());
            if (n < 50 || n > 100) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a number between 50 and 100.");
            return;
        }

        lblStatus.setText("Running algorithms...");
        btnNewRound.setDisable(true);
        lblN.setText("N = " + n);
        taHungarian.setText("Computing...");
        taBranchBound.setText("Computing (timeout: " + BB_TIMEOUT_SEC + "s)...");

        // Generate random cost matrix: values $20 – $200
        int[][] costMatrix = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                costMatrix[i][j] = 20 + RNG.nextInt(181);

        Thread bg = new Thread(() -> {
            // Run Hungarian Algorithm
            long startH = System.currentTimeMillis();
            HungarianAlgorithm hungarian = new HungarianAlgorithm(costMatrix);
            int[] hAssignment = hungarian.solve();
            long hungarianMs = System.currentTimeMillis() - startH;
            int hCost = hungarian.totalCost(hAssignment);
            AssignmentResult hResult = new AssignmentResult("Hungarian Algorithm", hAssignment, hCost, hungarianMs);

            // Run Branch and Bound on the actual N×N matrix with a timeout
            BranchAndBoundAssignment bbSolver = new BranchAndBoundAssignment(costMatrix);
            ExecutorService exec = Executors.newSingleThreadExecutor();
            Future<int[]> future = exec.submit(bbSolver::solve);
            exec.shutdown();

            long startBB = System.currentTimeMillis();
            int[] bbAssignment = null;
            long bbMs;
            boolean bbTimedOut;
            try {
                bbAssignment = future.get(BB_TIMEOUT_SEC, TimeUnit.SECONDS);
                bbMs = System.currentTimeMillis() - startBB;
                bbTimedOut = false;
            } catch (TimeoutException e) {
                bbSolver.cancel();
                future.cancel(true);
                bbMs = BB_TIMEOUT_SEC * 1000L;
                bbTimedOut = true;
            } catch (Exception e) {
                bbMs = -1;
                bbTimedOut = true;
            }

            final long finalBbMs = bbMs;
            final boolean finalBbTimedOut = bbTimedOut;
            final int[] finalBbAssignment = bbAssignment;

            Platform.runLater(() -> {
                int roundId = Game1DB.saveRound(n, hCost, hungarianMs, finalBbMs);
                lblRoundId.setText("Round ID: " + roundId);
                taHungarian.setText(hResult.summaryText());

                if (finalBbTimedOut) {
                    taBranchBound.setText(
                        "Branch & Bound\n" +
                        "TIMEOUT (>" + BB_TIMEOUT_SEC + "s) — B&B is O(N!) and impractical for N=" + n + "\n" +
                        "Time recorded: " + finalBbMs + " ms\n\n" +
                        "Note: Hungarian O(N³) is the practical algorithm for large N.\n" +
                        "Hungarian optimal cost: $" + hCost
                    );
                } else {
                    int bbCost = bbSolver.getMinCost();
                    AssignmentResult bbResult = new AssignmentResult(
                        "Branch & Bound", finalBbAssignment, bbCost, finalBbMs);
                    taBranchBound.setText(bbResult.summaryText());
                }

                lblStatus.setText("Round complete. Optimal cost: $" + hCost);
                btnNewRound.setDisable(false);
            });
        });
        bg.setDaemon(true);
        bg.start();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

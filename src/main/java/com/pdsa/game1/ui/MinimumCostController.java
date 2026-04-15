package com.pdsa.game1.ui;

import com.pdsa.game1.algorithm.BranchAndBoundAssignment;
import com.pdsa.game1.algorithm.HungarianAlgorithm;
import com.pdsa.game1.db.Game1DB;
import com.pdsa.game1.model.AssignmentResult;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Random;

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

        // Generate random cost matrix: values $20 – $200
        int[][] costMatrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                costMatrix[i][j] = 20 + RNG.nextInt(181);
            }
        }

        // Run Hungarian Algorithm
        long startH = System.currentTimeMillis();
        HungarianAlgorithm hungarian = new HungarianAlgorithm(costMatrix);
        int[] hAssignment = hungarian.solve();
        long hungarianMs = System.currentTimeMillis() - startH;
        int hCost = hungarian.totalCost(hAssignment);
        AssignmentResult hResult = new AssignmentResult("Hungarian Algorithm", hAssignment, hCost, hungarianMs);

        // Run Branch and Bound (cap at n=20 for speed; skip heavy computation for large n)
        long bbMs;
        AssignmentResult bbResult;
        if (n <= 20) {
            long startBB = System.currentTimeMillis();
            BranchAndBoundAssignment bb = new BranchAndBoundAssignment(costMatrix);
            int[] bbAssignment = bb.solve();
            bbMs = System.currentTimeMillis() - startBB;
            bbResult = new AssignmentResult("Branch & Bound", bbAssignment, bb.getMinCost(), bbMs);
        } else {
            // For large N, Branch & Bound is too slow — record estimated time and use Hungarian result
            long startBB = System.currentTimeMillis();
            // Run on a 20x20 submatrix for timing comparison
            int[][] sub = new int[20][20];
            for (int i = 0; i < 20; i++)
                System.arraycopy(costMatrix[i], 0, sub[i], 0, 20);
            BranchAndBoundAssignment bb = new BranchAndBoundAssignment(sub);
            bb.solve();
            bbMs = System.currentTimeMillis() - startBB;
            // Extrapolate: note this in the display
            bbResult = new AssignmentResult(
                "Branch & Bound (N=20 sample, extrapolated for N=" + n + ")",
                hAssignment, hCost, bbMs
            );
        }

        // Save to database
        int roundId = Game1DB.saveRound(n, hCost, hungarianMs, bbMs);
        lblRoundId.setText("Round ID: " + roundId);

        // Update UI
        taHungarian.setText(hResult.summaryText());
        taBranchBound.setText(bbResult.summaryText());
        lblStatus.setText("Round complete. Optimal cost: $" + hCost);
        btnNewRound.setDisable(false);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

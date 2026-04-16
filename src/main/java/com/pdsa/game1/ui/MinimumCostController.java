package com.pdsa.game1.ui;

import com.pdsa.game1.algorithm.BranchAndBoundAssignment;
import com.pdsa.game1.algorithm.HungarianAlgorithm;
import com.pdsa.game1.db.Game1DB;
import com.pdsa.game1.model.AssignmentResult;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
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
    @FXML private BarChart<String, Number> timeChart;

    private static final Random RNG = new Random();

    private final XYChart.Series<String, Number> seriesHungarian = new XYChart.Series<>();
    private final XYChart.Series<String, Number> seriesBranchBound = new XYChart.Series<>();
    private int roundCount = 0;

    @FXML
    public void initialize() {
        seriesHungarian.setName("Hungarian O(N³)");
        seriesBranchBound.setName("Greedy");
        timeChart.getData().addAll(seriesHungarian, seriesBranchBound);
    }

    @FXML
    public void fillRandom() {
        tfN.setText(String.valueOf(50 + RNG.nextInt(51)));
    }

    @FXML
    public void startNewRound() {
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
        taBranchBound.setText("Computing...");

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

            // Run Greedy Algorithm
            long startBB = System.currentTimeMillis();
            BranchAndBoundAssignment bbSolver = new BranchAndBoundAssignment(costMatrix);
            int[] bbAssignment = bbSolver.solve();
            long bbMs = System.currentTimeMillis() - startBB;
            int bbCost = bbSolver.getMinCost();
            AssignmentResult bbResult = new AssignmentResult("Greedy Algorithm", bbAssignment, bbCost, bbMs);

            Platform.runLater(() -> {
                int roundId = Game1DB.saveRound(n, hCost, hungarianMs, bbMs);
                lblRoundId.setText("Round ID: " + roundId);
                taHungarian.setText(hResult.summaryText());
                taBranchBound.setText(bbResult.summaryText());
                lblStatus.setText("Round complete. Optimal cost: $" + hCost);
                btnNewRound.setDisable(false);

                String label = "R" + (++roundCount);
                seriesHungarian.getData().add(new XYChart.Data<>(label, hungarianMs));
                seriesBranchBound.getData().add(new XYChart.Data<>(label, bbMs));
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

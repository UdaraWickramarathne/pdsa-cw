package com.pdsa.game1.ui;

import com.pdsa.game1.algorithm.BranchAndBoundAssignment;
import com.pdsa.game1.algorithm.HungarianAlgorithm;
import com.pdsa.game1.db.Game1DB;
import com.pdsa.game1.model.AssignmentResult;
import com.pdsa.util.Banner;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.Random;

public class MinimumCostController {

    @FXML private Label lblStatus;
    @FXML private Label lblN;
    @FXML private Label lblRoundId;
    @FXML private Label lblHungarianMs;
    @FXML private Label lblBranchBoundMs;
    @FXML private TextArea taHungarian;
    @FXML private TextArea taBranchBound;
    @FXML private Button btnNewRound;
    @FXML private TextField tfN;
    @FXML private BarChart<String, Number> timeChart;
    @FXML private ProgressIndicator piHungarian;
    @FXML private ProgressIndicator piBranchBound;
    @FXML private HBox bannerBox;
    @FXML private Label bannerLabel;
    @FXML private VBox recordsDrawer;
    @FXML private TabPane recordsTabPane;
    @FXML private ToggleButton btnHistory;

    private static final Random RNG = new Random();
    private final XYChart.Series<String, Number> seriesHungarian = new XYChart.Series<>();
    private final XYChart.Series<String, Number> seriesBranchBound = new XYChart.Series<>();
    private int roundCount = 0;

    @FXML
    public void initialize() {
        seriesHungarian.setName("Hungarian O(N³)");
        seriesBranchBound.setName("Branch & Bound");
        timeChart.getData().addAll(seriesHungarian, seriesBranchBound);

        recordsDrawer.setTranslateX(300);
        recordsDrawer.setVisible(false);

        // Wire keyboard shortcuts once scene is ready
        btnNewRound.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) {
                scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.N), this::startNewRound);
                scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.ESCAPE), () -> scene.getWindow().hide());
            }
        });
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
            Banner.show(bannerBox, bannerLabel, Banner.Kind.WARN,
                "Please enter a number between 50 and 100.");
            return;
        }

        lblStatus.setText("Running…");
        lblStatus.getStyleClass().setAll("label-cyan");
        btnNewRound.setDisable(true);
        lblN.setText("N = " + n);

        piHungarian.setVisible(true);  piHungarian.setManaged(true);
        piBranchBound.setVisible(true); piBranchBound.setManaged(true);
        lblHungarianMs.setText("— ms");
        lblBranchBoundMs.setText("— ms");
        taHungarian.setText("Computing…");
        taBranchBound.setText("Computing…");

        int[][] costMatrix = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                costMatrix[i][j] = 20 + RNG.nextInt(181);

        Thread bg = new Thread(() -> {
            long startH = System.currentTimeMillis();
            HungarianAlgorithm hungarian = new HungarianAlgorithm(costMatrix);
            int[] hAssignment = hungarian.solve();
            long hungarianMs = System.currentTimeMillis() - startH;
            int hCost = hungarian.totalCost(hAssignment);
            AssignmentResult hResult = new AssignmentResult("Hungarian Algorithm", hAssignment, hCost, hungarianMs);

            long startBB = System.currentTimeMillis();
            BranchAndBoundAssignment bbSolver = new BranchAndBoundAssignment(costMatrix);
            int[] bbAssignment = bbSolver.solve();
            long bbMs = System.currentTimeMillis() - startBB;
            int bbCost = bbSolver.getMinCost();
            AssignmentResult bbResult = new AssignmentResult("Branch & Bound", bbAssignment, bbCost, bbMs);

            Platform.runLater(() -> {
                int roundId = Game1DB.saveRound(n, hCost, hungarianMs, bbMs);
                lblRoundId.setText("ROUND " + String.format("%03d", roundId));
                taHungarian.setText(hResult.summaryText());
                taBranchBound.setText(bbResult.summaryText());
                lblHungarianMs.setText(hungarianMs + " ms");
                lblBranchBoundMs.setText(bbMs + " ms");

                // Highlight winner card
                if (hungarianMs <= bbMs) {
                    lblHungarianMs.getStyleClass().add("label-green");
                } else {
                    lblBranchBoundMs.getStyleClass().add("label-green");
                }

                lblStatus.setText("Done — optimal cost: $" + hCost);
                btnNewRound.setDisable(false);
                piHungarian.setVisible(false);  piHungarian.setManaged(false);
                piBranchBound.setVisible(false); piBranchBound.setManaged(false);

                String label = "R" + (++roundCount);
                seriesHungarian.getData().add(new XYChart.Data<>(label, hungarianMs));
                seriesBranchBound.getData().add(new XYChart.Data<>(label, bbMs));
            });
        });
        bg.setDaemon(true);
        bg.start();
    }

    @FXML
    public void toggleHistory() {
        if (btnHistory.isSelected()) {
            loadRecordsData();
            recordsDrawer.setVisible(true);
            TranslateTransition tt = new TranslateTransition(Duration.millis(280), recordsDrawer);
            tt.setToX(0);
            tt.play();
        } else {
            TranslateTransition tt = new TranslateTransition(Duration.millis(280), recordsDrawer);
            tt.setToX(300);
            tt.setOnFinished(e -> recordsDrawer.setVisible(false));
            tt.play();
        }
    }

    @FXML
    public void closeDrawer() {
        btnHistory.setSelected(false);
        toggleHistory();
    }

    private void loadRecordsData() {
        recordsTabPane.getTabs().clear();

        // Rounds tab
        TableView<javafx.collections.ObservableList<String>> roundsTable = buildTable(
            "Round#", "N", "Min Cost", "Hungarian (ms)", "Branch & Bound (ms)", "Date");
        populate(roundsTable, Game1DB.getRecentRounds(50));
        Tab roundsTab = new Tab("Rounds", roundsTable);
        recordsTabPane.getTabs().add(roundsTab);
    }

    private TableView<javafx.collections.ObservableList<String>> buildTable(String... headers) {
        TableView<javafx.collections.ObservableList<String>> table = new TableView<>();
        for (int i = 0; i < headers.length; i++) {
            final int col = i;
            TableColumn<javafx.collections.ObservableList<String>, String> tc = new TableColumn<>(headers[i]);
            tc.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().get(col)));
            tc.setPrefWidth(90);
            table.getColumns().add(tc);
        }
        return table;
    }

    private void populate(TableView<javafx.collections.ObservableList<String>> table, List<String[]> rows) {
        for (String[] row : rows)
            table.getItems().add(FXCollections.observableArrayList(row));
        if (rows.isEmpty())
            table.setPlaceholder(new Label("No records yet — play a round first."));
    }
}

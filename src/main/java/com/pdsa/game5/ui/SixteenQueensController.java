package com.pdsa.game5.ui;

import com.pdsa.game5.algorithm.SequentialSolver;
import com.pdsa.game5.algorithm.ThreadedSolver;
import com.pdsa.game5.db.Game5DB;
import com.pdsa.util.Banner;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;

public class SixteenQueensController {

    private static final int N = 16;
    /** Pixel gutter reserved on the left and top for row/column labels. */
    private static final int GUTTER = 18;

    @FXML private Canvas boardCanvas;
    @FXML private TextField tfPlayerName;
    @FXML private Button btnSubmit;
    @FXML private Button btnPrecompute;
    @FXML private Button btnViewSample;
    @FXML private Label lblStatus;
    @FXML private Label lblResult;
    @FXML private Label lblSeqMs;
    @FXML private Label lblThrMs;
    @FXML private Label lblSolutions;
    @FXML private Label lblClaimedBadge;
    @FXML private Label lblUnclaimedBadge;
    @FXML private Label stepPill1;
    @FXML private Label stepPill2;
    @FXML private Label stepPill3;
    @FXML private ProgressBar pbPrecompute;
    @FXML private VBox solutionOverlay;
    @FXML private BarChart<String, Number> timeChart;
    @FXML private HBox bannerBox;
    @FXML private Label bannerLabel;
    @FXML private VBox recordsDrawer;
    @FXML private TabPane recordsTabPane;
    @FXML private ToggleButton btnHistory;

    private final int[] userQueens = new int[N];
    private boolean sampleOverlayActive = false;

    private final XYChart.Series<String, Number> seriesSeq = new XYChart.Series<>();
    private final XYChart.Series<String, Number> seriesThr = new XYChart.Series<>();

    @FXML
    public void initialize() {
        seriesSeq.setName("Sequential");
        seriesThr.setName("Threaded");
        timeChart.getData().addAll(seriesSeq, seriesThr);
        loadChartFromDB();
        refreshStatus();

        Arrays.fill(userQueens, -1);
        boardCanvas.setOnMouseClicked(this::handleBoardClick);
        drawBoard(userQueens, false);

        recordsDrawer.setTranslateX(300);
        recordsDrawer.setVisible(false);

        // Stepper: start at step 1
        setStep(1);

        // Wire keyboard shortcuts once scene is available
        boardCanvas.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) {
                scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.ENTER), () -> { if (!btnSubmit.isDisabled()) submitAnswer(); });
                scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.ESCAPE), () -> scene.getWindow().hide());
            }
        });
    }

    private void setStep(int step) {
        stepPill1.getStyleClass().setAll("step-pill", step == 1 ? "active" : step > 1 ? "done" : "");
        stepPill2.getStyleClass().setAll("step-pill", step == 2 ? "active" : step > 2 ? "done" : "");
        stepPill3.getStyleClass().setAll("step-pill", step == 3 ? "active" : "");
    }

    private void loadChartFromDB() {
        List<long[]> timings = Game5DB.getAllTimings();
        for (int i = 0; i < timings.size(); i++) {
            String label = "R" + (i + 1);
            seriesSeq.getData().add(new XYChart.Data<>(label, timings.get(i)[0]));
            seriesThr.getData().add(new XYChart.Data<>(label, timings.get(i)[1]));
        }
    }

    @FXML
    public void handleBoardClick(MouseEvent e) {
        if (sampleOverlayActive) return;
        double cw = (boardCanvas.getWidth()  - GUTTER) / N;
        double ch = (boardCanvas.getHeight() - GUTTER) / N;
        int col = (int)((e.getX() - GUTTER) / cw);
        int row = (int)((e.getY() - GUTTER) / ch);
        if (col < 0 || col >= N || row < 0 || row >= N) return;

        if (userQueens[col] == row) {
            userQueens[col] = -1;
        } else {
            userQueens[col] = row;
        }
        drawBoard(userQueens, false);

        // Advance to step 2 once any queen is placed
        long placed = 0;
        for (int q : userQueens) if (q >= 0) placed++;
        if (placed > 0) setStep(2);
    }

    @FXML
    public void clearUserBoard() {
        Arrays.fill(userQueens, -1);
        lblResult.setText("");
        lblResult.getStyleClass().setAll();
        drawBoard(userQueens, false);
        setStep(Game5DB.solutionsExist() ? 2 : 1);
    }

    @FXML
    public void precompute() {
        btnPrecompute.setDisable(true);
        pbPrecompute.setVisible(true);
        pbPrecompute.setManaged(true);
        lblStatus.setText("Running Sequential solver…");
        setStep(1);

        Thread t = new Thread(() -> {
            try {
                // Sequential
                long startSeq = System.currentTimeMillis();
                SequentialSolver seqSolver = new SequentialSolver(N);
                List<int[]> solutions = seqSolver.solve();
                long seqMs = System.currentTimeMillis() - startSeq;

                Platform.runLater(() ->
                    lblStatus.setText("Sequential done: " + solutions.size() + " solutions in " + seqMs + " ms. Running threaded…"));

                // Threaded
                long startThr = System.currentTimeMillis();
                ThreadedSolver thrSolver = new ThreadedSolver(N);
                List<int[]> thrSolutions = thrSolver.solve();
                long thrMs = System.currentTimeMillis() - startThr;

                Game5DB.saveAllSolutions(solutions);
                Game5DB.saveAllSolutions(thrSolutions);
                Game5DB.saveTiming(seqMs, thrMs, solutions.size());

                Platform.runLater(() -> {
                    lblSeqMs.setText(seqMs + " ms");
                    lblThrMs.setText(thrMs + " ms");
                    lblSolutions.setText(String.valueOf(solutions.size()));
                    lblStatus.setText("Precompute complete — " + solutions.size() + " solutions saved.");

                    pbPrecompute.setVisible(false);
                    pbPrecompute.setManaged(false);

                    String label = "R" + (seriesSeq.getData().size() + 1);
                    seriesSeq.getData().add(new XYChart.Data<>(label, seqMs));
                    seriesThr.getData().add(new XYChart.Data<>(label, thrMs));

                    refreshStatus();
                    clearUserBoard();
                    btnPrecompute.setDisable(false);
                    setStep(2);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblStatus.setText("Error: " + e.getMessage());
                    pbPrecompute.setVisible(false);
                    pbPrecompute.setManaged(false);
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
            Banner.show(bannerBox, bannerLabel, Banner.Kind.WARN, "Please enter your name.");
            return;
        }

        for (int col = 0; col < N; col++) {
            if (userQueens[col] < 0) {
                Banner.show(bannerBox, bannerLabel, Banner.Kind.WARN,
                    "Column " + (col + 1) + " is empty — place all 16 queens first.");
                return;
            }
        }

        setStep(3);
        String normalizedPlacement = Game5DB.toPlacementString(userQueens);
        String result = Game5DB.claimSolution(normalizedPlacement, name);

        lblResult.getStyleClass().setAll();
        switch (result) {
            case "WIN" -> {
                lblResult.setText("★ Correct! YOU WIN — solution claimed by " + name + "!");
                lblResult.getStyleClass().add("result-win");
                refreshStatus();
            }
            case "ALREADY_CLAIMED" -> {
                lblResult.setText("◆ Already claimed. Try a different arrangement!");
                lblResult.getStyleClass().add("result-draw");
            }
            case "NOT_FOUND" -> {
                lblResult.setText("✗ Invalid — queens must not threaten each other.");
                lblResult.getStyleClass().add("result-lose");
            }
        }

        int unclaimed = Game5DB.unclaimedCount();
        if (unclaimed == 0) {
            Banner.show(bannerBox, bannerLabel, Banner.Kind.INFO,
                "All solutions identified! Resetting cycle…");
            Game5DB.resetAllClaimed();
            refreshStatus();
        }
    }

    @FXML
    public void showSampleSolution() {
        if (!Game5DB.solutionsExist()) {
            Banner.show(bannerBox, bannerLabel, Banner.Kind.WARN, "Run Precompute first to generate solutions.");
            return;
        }
        int[] sample = Game5DB.fetchOneSolution();
        if (sample == null) {
            Banner.show(bannerBox, bannerLabel, Banner.Kind.WARN, "No solutions available yet.");
            return;
        }

        sampleOverlayActive = true;
        solutionOverlay.setVisible(true);
        solutionOverlay.setManaged(true);
        drawBoard(sample, true);
    }

    @FXML
    public void dismissSampleSolution() {
        sampleOverlayActive = false;
        solutionOverlay.setVisible(false);
        solutionOverlay.setManaged(false);
        clearUserBoard();
    }

    /**
     * Returns a boolean array where {@code true} means the queen in that column
     * conflicts with at least one other queen (same row or same diagonal).
     * Column conflicts cannot occur because each column holds at most one queen.
     */
    private boolean[] findConflicts(int[] queens) {
        boolean[] conflict = new boolean[N];
        for (int i = 0; i < N; i++) {
            if (queens[i] < 0) continue;
            for (int j = i + 1; j < N; j++) {
                if (queens[j] < 0) continue;
                boolean sameRow = queens[i] == queens[j];
                boolean sameDiag = Math.abs(i - j) == Math.abs(queens[i] - queens[j]);
                if (sameRow || sameDiag) {
                    conflict[i] = true;
                    conflict[j] = true;
                }
            }
        }
        return conflict;
    }

    private void drawBoard(int[] queens, boolean isSample) {
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();
        double w  = boardCanvas.getWidth();
        double h  = boardCanvas.getHeight();
        // Board area starts at (GUTTER, GUTTER); labels live in the gutter strip
        double bw = w - GUTTER;
        double bh = h - GUTTER;
        double cw = bw / N;
        double ch = bh / N;

        // Clear canvas background
        gc.setFill(Color.web("#0D0020"));
        gc.fillRect(0, 0, w, h);

        // Dark checkerboard (offset by gutter)
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                Color fill = ((r + c) % 2 == 0) ? Color.web("#1F0D4A") : Color.web("#160032");
                gc.setFill(fill);
                gc.fillRect(GUTTER + c * cw, GUTTER + r * ch, cw, ch);
            }
        }

        // Grid lines
        gc.setStroke(Color.web("#2A1060"));
        gc.setLineWidth(0.5);
        for (int r = 0; r <= N; r++) gc.strokeLine(GUTTER, GUTTER + r * ch, w, GUTTER + r * ch);
        for (int c = 0; c <= N; c++) gc.strokeLine(GUTTER + c * cw, GUTTER, GUTTER + c * cw, h);

        // Column numbers — centred above each column in the top gutter
        gc.setFont(Font.font("VT323", 11));
        gc.setFill(Color.web("#6A5A9C"));
        for (int i = 0; i < N; i++) {
            String label = String.valueOf(i + 1);
            double x = GUTTER + i * cw + cw * 0.5 - (label.length() > 1 ? 5 : 3);
            gc.fillText(label, x, GUTTER - 4);
        }

        // Row numbers — centred to the left of each row in the left gutter
        for (int i = 0; i < N; i++) {
            String label = String.valueOf(i + 1);
            double y = GUTTER + i * ch + ch * 0.68;
            gc.fillText(label, label.length() > 1 ? 1 : 5, y);
        }

        // Queens
        if (queens != null) {
            boolean[] conflicts = isSample ? new boolean[N] : findConflicts(queens);

            // Highlight the row and column of each conflicting queen
            if (!isSample) {
                gc.setFill(Color.web("#FF000022"));
                for (int col = 0; col < N; col++) {
                    if (!conflicts[col]) continue;
                    int row = queens[col];
                    if (row < 0) continue;
                    gc.fillRect(GUTTER,           GUTTER + row * ch, bw, ch);
                    gc.fillRect(GUTTER + col * cw, GUTTER,           cw, bh);
                }
            }

            for (int col = 0; col < N; col++) {
                int row = queens[col];
                if (row < 0) continue;

                double cx = GUTTER + col * cw + cw / 2;
                double cy = GUTTER + row * ch + ch / 2;

                String glowHex;
                Color queenColor;
                if (isSample) {
                    queenColor = Color.web("#FFE500");
                    glowHex    = "#FFE500";
                } else if (conflicts[col]) {
                    queenColor = Color.web("#FF3B3B");
                    glowHex    = "#FF0000";
                } else {
                    queenColor = Color.web("#FF2E88");
                    glowHex    = "#FF2E88";
                }

                gc.setEffect(new DropShadow(8, 0, 0, Color.web(glowHex)));
                gc.setFill(queenColor);
                gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, Math.max(7, (int)(cw * 0.52))));
                gc.fillText("♛", cx - cw * 0.3, cy + ch * 0.25);
                gc.setEffect(null);
            }
        }
    }

    private void refreshStatus() {
        try {
            int unclaimed = Game5DB.unclaimedCount();
            int total = 14772; // known total 16-queens solutions
            if (unclaimed < 0) {
                lblUnclaimedBadge.setText("LEFT: —");
                lblClaimedBadge.setText("CLAIMED: 0");
            } else {
                int claimed = total - unclaimed;
                lblUnclaimedBadge.setText("LEFT: " + unclaimed);
                lblClaimedBadge.setText("CLAIMED: " + claimed);
            }

            long[] timing = Game5DB.getLastTiming();
            if (timing != null) {
                lblSeqMs.setText(timing[0] + " ms");
                lblThrMs.setText(timing[1] + " ms");
                lblSolutions.setText(String.valueOf(timing[2]));
            }
        } catch (Exception e) {
            lblUnclaimedBadge.setText("LEFT: —");
        }
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

        TableView<javafx.collections.ObservableList<String>> tt =
            buildTable("ID", "Sequential (ms)", "Threaded (ms)", "Solutions", "Date");
        populate(tt, Game5DB.getAllTimingsFull());
        recordsTabPane.getTabs().add(new Tab("Timing Runs", tt));

        TableView<javafx.collections.ObservableList<String>> wt =
            buildTable("ID", "Solution#", "Player", "Date");
        populate(wt, Game5DB.getRecentWinners(50));
        recordsTabPane.getTabs().add(new Tab("Winners", wt));
    }

    private TableView<javafx.collections.ObservableList<String>> buildTable(String... headers) {
        TableView<javafx.collections.ObservableList<String>> table = new TableView<>();
        for (int i = 0; i < headers.length; i++) {
            final int col = i;
            TableColumn<javafx.collections.ObservableList<String>, String> tc = new TableColumn<>(headers[i]);
            tc.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().get(col)));
            tc.setPrefWidth(110);
            table.getColumns().add(tc);
        }
        return table;
    }

    private void populate(TableView<javafx.collections.ObservableList<String>> table, List<String[]> rows) {
        for (String[] row : rows)
            table.getItems().add(FXCollections.observableArrayList(row));
        if (rows.isEmpty())
            table.setPlaceholder(new Label("No records yet — run the solver first."));
    }
}

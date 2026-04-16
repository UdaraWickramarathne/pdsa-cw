package com.pdsa.game4.ui;

import com.pdsa.game4.algorithm.BacktrackingSolver;
import com.pdsa.game4.algorithm.WarnsdorffSolver;
import com.pdsa.game4.db.Game4DB;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KnightsTourController {

    @FXML private Canvas boardCanvas;
    @FXML private ToggleButton rb8x8;
    @FXML private ToggleButton rb16x16;
    @FXML private Label lblStart;
    @FXML private Label lblWarnsdorff;
    @FXML private Label lblBacktracking;
    @FXML private Label lblStatus;
    @FXML private Label lblResult;
    @FXML private Label lblRoundBadge;
    @FXML private TextField tfPlayerName;
    @FXML private Button btnNewRound;
    @FXML private Button btnCheckTour;
    @FXML private Button btnShowSolution;
    @FXML private Button btnUndo;
    @FXML private BarChart<String, Number> timeChart;
    @FXML private HBox bannerBox;
    @FXML private Label bannerLabel;
    @FXML private VBox recordsDrawer;
    @FXML private TabPane recordsTabPane;
    @FXML private ToggleButton btnHistory;

    private int boardSize;
    private int startRow, startCol;
    private int[] correctTour;
    private int currentRoundId = -1;
    private List<Integer> playerClicks = new ArrayList<>();
    private List<Integer> suggestedMoves = new ArrayList<>();

    private final Random rng = new Random();

    private final XYChart.Series<String, Number> seriesWarnsdorff = new XYChart.Series<>();
    private final XYChart.Series<String, Number> seriesBacktracking = new XYChart.Series<>();
    private int roundCount = 0;

    @FXML
    public void initialize() {
        // Default board size
        rb8x8.setSelected(true);
        boardSize = 8;

        seriesWarnsdorff.setName("Warnsdorff");
        seriesBacktracking.setName("Backtracking");
        timeChart.getData().addAll(seriesWarnsdorff, seriesBacktracking);

        recordsDrawer.setTranslateX(300);
        recordsDrawer.setVisible(false);

        // Wire keyboard shortcuts once scene is available
        boardCanvas.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) {
                scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.N), this::startNewRound);
                scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.Z, javafx.scene.input.KeyCombination.CONTROL_DOWN),
                    this::undoLastClick);
                scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.ESCAPE), () -> scene.getWindow().hide());
            }
        });
    }

    @FXML
    public void onSizeToggle() {
        boardSize = rb16x16.isSelected() ? 16 : 8;
        // Ensure mutual exclusion
        if (rb16x16.isSelected()) rb8x8.setSelected(false);
        else rb16x16.setSelected(false);
        rb8x8.setSelected(!rb16x16.isSelected());
    }

    @FXML
    public void startNewRound() {
        Banner.hide(bannerBox);
        btnNewRound.setDisable(true);
        btnCheckTour.setDisable(true);
        btnShowSolution.setDisable(true);
        btnUndo.setDisable(true);
        lblStatus.setText("Computing tours…");
        playerClicks.clear();
        suggestedMoves.clear();
        lblResult.setText("");
        lblResult.getStyleClass().setAll();

        startRow = rng.nextInt(boardSize);
        startCol = rng.nextInt(boardSize);
        lblStart.setText("Start: (" + (startRow + 1) + ", " + (startCol + 1) + ")");
        lblWarnsdorff.setText("Warnsdorff: computing…");
        lblBacktracking.setText("Backtracking: computing…");

        final int capturedRow = startRow;
        final int capturedCol = startCol;
        final int capturedSize = boardSize;

        Thread bg = new Thread(() -> {
            long startW = System.currentTimeMillis();
            WarnsdorffSolver wSolver = new WarnsdorffSolver(capturedSize);
            int[] wTour = wSolver.solve(capturedRow, capturedCol);
            long wMs = System.currentTimeMillis() - startW;

            long startBT = System.currentTimeMillis();
            BacktrackingSolver btSolver = new BacktrackingSolver(capturedSize);
            int[] btTour = btSolver.solve(capturedRow, capturedCol);
            long btMs = System.currentTimeMillis() - startBT;

            Platform.runLater(() -> {
                correctTour = wTour;

                lblWarnsdorff.setText("Warnsdorff: " + wMs + " ms" + (wTour != null ? " ✓" : " (no tour)"));
                lblBacktracking.setText("Backtracking: " + btMs + " ms" + (btTour != null ? " ✓" : " (no tour)"));

                int roundId = Game4DB.saveRound(capturedSize, capturedRow, capturedCol, wMs, btMs);
                currentRoundId = roundId;
                lblRoundBadge.setText("ROUND " + String.format("%03d", Math.max(roundId, 1)));

                lblStatus.setText("Click cells to build your tour, then CHECK.");
                btnCheckTour.setDisable(false);
                btnShowSolution.setDisable(false);
                btnNewRound.setDisable(false);
                drawBoard(null);

                String label = "R" + (++roundCount);
                seriesWarnsdorff.getData().add(new XYChart.Data<>(label, wMs));
                seriesBacktracking.getData().add(new XYChart.Data<>(label, btMs));
            });
        });
        bg.setDaemon(true);
        bg.start();
    }

    @FXML
    public void checkTour() {
        String name = tfPlayerName.getText().trim();
        if (name.isEmpty()) {
            Banner.show(bannerBox, bannerLabel, Banner.Kind.WARN, "Please enter your name.");
            return;
        }
        if (playerClicks.isEmpty()) {
            Banner.show(bannerBox, bannerLabel, Banner.Kind.WARN, "Click cells on the board to build your tour.");
            return;
        }

        int requiredStart = startRow * boardSize + startCol;
        if (playerClicks.get(0) != requiredStart) {
            Banner.show(bannerBox, bannerLabel, Banner.Kind.ERROR,
                "Tour must start at (" + (startRow + 1) + ", " + (startCol + 1) + "). Start a new round to reset.");
            return;
        }

        int total = boardSize * boardSize;

        if (playerClicks.size() < total) {
            if (playerClicks.size() >= total / 2 && isValidPartialKnightPath(playerClicks, boardSize)) {
                lblResult.setText("DRAW — Valid partial tour! Covered " +
                    playerClicks.size() + " / " + total + " cells.");
                lblResult.getStyleClass().setAll("result-draw");
            } else {
                Banner.show(bannerBox, bannerLabel, Banner.Kind.WARN,
                    "Incomplete tour — need " + total + " cells, you have " + playerClicks.size() + ".");
            }
            return;
        }

        if (isValidKnightTour(playerClicks, boardSize)) {
            lblResult.setText("★ Correct Knight's Tour! YOU WIN!");
            lblResult.getStyleClass().setAll("result-win");
            String seq = playerClicks.stream().map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
            if (currentRoundId > 0) Game4DB.saveWinner(currentRoundId, name, seq);
        } else {
            lblResult.setText("✗ Invalid tour. Check your moves or Show Solution.");
            lblResult.getStyleClass().setAll("result-lose");
        }
    }

    @FXML
    public void undoLastClick() {
        if (playerClicks.isEmpty()) return;
        playerClicks.remove(playerClicks.size() - 1);
        if (!playerClicks.isEmpty()) {
            suggestedMoves = computeValidMoves(playerClicks.get(playerClicks.size() - 1));
        } else {
            suggestedMoves.clear();
        }
        btnUndo.setDisable(playerClicks.isEmpty());
        drawBoard(playerClicks.isEmpty() ? null : playerClicks);
    }

    @FXML
    public void showSolution() {
        if (correctTour != null) {
            playerClicks.clear();
            suggestedMoves.clear();
            for (int cell : correctTour) playerClicks.add(cell);
            drawBoard(playerClicks);
            lblResult.setText("Solution shown (Warnsdorff's tour).");
            lblResult.getStyleClass().setAll("result-draw");
        }
    }

    @FXML
    public void handleCanvasClick(javafx.scene.input.MouseEvent e) {
        if (currentRoundId < 0) return;
        double cellW = boardCanvas.getWidth() / boardSize;
        double cellH = boardCanvas.getHeight() / boardSize;
        int col = (int)(e.getX() / cellW);
        int row = (int)(e.getY() / cellH);
        if (row < 0 || row >= boardSize || col < 0 || col >= boardSize) return;
        int cell = row * boardSize + col;

        if (playerClicks.isEmpty()) {
            int requiredStart = startRow * boardSize + startCol;
            if (cell != requiredStart) {
                Banner.show(bannerBox, bannerLabel, Banner.Kind.INFO,
                    "Start at the ♞ marker: (" + (startRow + 1) + ", " + (startCol + 1) + ")");
                return;
            }
            playerClicks.add(cell);
            suggestedMoves = computeValidMoves(cell);
        } else {
            if (!suggestedMoves.contains(cell)) return;
            playerClicks.add(cell);
            suggestedMoves = computeValidMoves(cell);
            if (suggestedMoves.isEmpty() && playerClicks.size() < boardSize * boardSize) {
                Banner.show(bannerBox, bannerLabel, Banner.Kind.WARN,
                    "No valid moves from here — start a new round to try again.");
            }
        }

        btnUndo.setDisable(false);
        drawBoard(playerClicks);
    }

    private List<Integer> computeValidMoves(int currentCell) {
        int[] DR = {-2, -2, -1, -1,  1,  1,  2,  2};
        int[] DC = {-1,  1, -2,  2, -2,  2, -1,  1};
        int row = currentCell / boardSize;
        int col = currentCell % boardSize;
        List<Integer> moves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            int nr = row + DR[i];
            int nc = col + DC[i];
            if (nr >= 0 && nr < boardSize && nc >= 0 && nc < boardSize) {
                int next = nr * boardSize + nc;
                if (!playerClicks.contains(next)) moves.add(next);
            }
        }
        return moves;
    }

    private void drawBoard(List<Integer> highlighted) {
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();
        double w = boardCanvas.getWidth();
        double h = boardCanvas.getHeight();
        double cw = w / boardSize;
        double ch = h / boardSize;

        // Dark checkerboard using neon theme tokens
        gc.clearRect(0, 0, w, h);
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                Color fill = ((r + c) % 2 == 0) ? Color.web("#1F0D4A") : Color.web("#160032");
                gc.setFill(fill);
                gc.fillRect(c * cw, r * ch, cw, ch);
            }
        }

        // Grid lines (subtle)
        gc.setStroke(Color.web("#2A1060"));
        gc.setLineWidth(0.5);
        for (int r = 0; r <= boardSize; r++) gc.strokeLine(0, r * ch, w, r * ch);
        for (int c = 0; c <= boardSize; c++) gc.strokeLine(c * cw, 0, c * cw, h);

        // Highlighted path
        if (highlighted != null && !highlighted.isEmpty()) {
            // Draw connecting lines first
            gc.setStroke(Color.web("#FFE500"));
            gc.setLineWidth(2.0);
            for (int i = 1; i < highlighted.size(); i++) {
                int c1 = highlighted.get(i - 1), c2 = highlighted.get(i);
                gc.strokeLine(
                    (c1 % boardSize) * cw + cw / 2, (c1 / boardSize) * ch + ch / 2,
                    (c2 % boardSize) * cw + cw / 2, (c2 / boardSize) * ch + ch / 2
                );
            }
            // Draw step cells
            for (int step = 0; step < highlighted.size(); step++) {
                int cell = highlighted.get(step);
                int r = cell / boardSize;
                int c = cell % boardSize;
                double x = c * cw + 2;
                double y = r * ch + 2;
                gc.setFill(Color.web("#00F5FF33"));
                gc.fillRect(x, y, cw - 4, ch - 4);
                gc.setFill(Color.web("#00F5FF"));
                gc.setFont(Font.font("VT323", FontWeight.BOLD, Math.max(9, (int)(cw * 0.42))));
                gc.fillText(String.valueOf(step + 1), x + cw * 0.18, y + ch * 0.72);
            }
        }

        // Valid next move highlights (neon yellow)
        for (int cell : suggestedMoves) {
            int r = cell / boardSize;
            int c = cell % boardSize;
            gc.setFill(Color.web("#FFE50030"));
            gc.fillRect(c * cw + 1, r * ch + 1, cw - 2, ch - 2);
            gc.setFill(Color.web("#FFE500BB"));
            double dotR = Math.min(cw, ch) * 0.22;
            gc.fillOval(c * cw + cw / 2 - dotR, r * ch + ch / 2 - dotR, dotR * 2, dotR * 2);
        }

        // Start marker — neon magenta knight glyph
        double sx = startCol * cw + cw / 2;
        double sy = startRow * ch + ch / 2;
        gc.setEffect(new DropShadow(8, 0, 0, Color.web("#FF2E88")));
        gc.setFill(Color.web("#FF2E88"));
        gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, Math.max(8, (int)(cw * 0.55))));
        gc.fillText("♞", sx - cw * 0.28, sy + ch * 0.25);
        gc.setEffect(null);
    }

    private boolean isValidKnightTour(List<Integer> cells, int n) {
        if (cells.size() != n * n) return false;
        boolean[] visited = new boolean[n * n];
        for (int i = 0; i < cells.size(); i++) {
            int cell = cells.get(i);
            if (cell < 0 || cell >= n * n || visited[cell]) return false;
            visited[cell] = true;
            if (i > 0) {
                int prev = cells.get(i - 1);
                int dr = Math.abs(cell / n - prev / n);
                int dc = Math.abs(cell % n - prev % n);
                if (!((dr == 2 && dc == 1) || (dr == 1 && dc == 2))) return false;
            }
        }
        return true;
    }

    private boolean isValidPartialKnightPath(List<Integer> cells, int n) {
        boolean[] visited = new boolean[n * n];
        for (int i = 0; i < cells.size(); i++) {
            int cell = cells.get(i);
            if (cell < 0 || cell >= n * n || visited[cell]) return false;
            visited[cell] = true;
            if (i > 0) {
                int prev = cells.get(i - 1);
                int dr = Math.abs(cell / n - prev / n);
                int dc = Math.abs(cell % n - prev % n);
                if (!((dr == 2 && dc == 1) || (dr == 1 && dc == 2))) return false;
            }
        }
        return true;
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

        TableView<javafx.collections.ObservableList<String>> rt =
            buildTable("Round#", "Board", "Start Row", "Start Col", "Warnsdorff (ms)", "Backtracking (ms)", "Date");
        populate(rt, Game4DB.getRecentRounds(50));
        recordsTabPane.getTabs().add(new Tab("Rounds", rt));

        TableView<javafx.collections.ObservableList<String>> wt =
            buildTable("ID", "Round#", "Player", "Date");
        populate(wt, Game4DB.getRecentWinners(50));
        recordsTabPane.getTabs().add(new Tab("Winners", wt));
    }

    private TableView<javafx.collections.ObservableList<String>> buildTable(String... headers) {
        TableView<javafx.collections.ObservableList<String>> table = new TableView<>();
        for (int i = 0; i < headers.length; i++) {
            final int col = i;
            TableColumn<javafx.collections.ObservableList<String>, String> tc = new TableColumn<>(headers[i]);
            tc.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().get(col)));
            tc.setPrefWidth(95);
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

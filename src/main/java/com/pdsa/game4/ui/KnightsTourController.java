package com.pdsa.game4.ui;

import com.pdsa.game4.algorithm.BacktrackingSolver;
import com.pdsa.game4.algorithm.WarnsdorffSolver;
import com.pdsa.game4.db.Game4DB;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KnightsTourController {

    @FXML private Canvas boardCanvas;
    @FXML private RadioButton rb8x8;
    @FXML private RadioButton rb16x16;
    @FXML private Label lblStart;
    @FXML private Label lblWarnsdorff;
    @FXML private Label lblBacktracking;
    @FXML private Label lblStatus;
    @FXML private Label lblResult;
    @FXML private TextField tfPlayerName;
    @FXML private Button btnNewRound;
    @FXML private Button btnCheckTour;
    @FXML private Button btnShowSolution;
    @FXML private BarChart<String, Number> timeChart;

    private int boardSize;
    private int startRow, startCol;
    private int[] correctTour;
    private int currentRoundId = -1;
    private List<Integer> playerClicks = new ArrayList<>();

    private final ToggleGroup sizeGroup = new ToggleGroup();
    private final Random rng = new Random();

    private final XYChart.Series<String, Number> seriesWarnsdorff = new XYChart.Series<>();
    private final XYChart.Series<String, Number> seriesBacktracking = new XYChart.Series<>();
    private int roundCount = 0;

    @FXML
    public void initialize() {
        rb8x8.setToggleGroup(sizeGroup);
        rb16x16.setToggleGroup(sizeGroup);
        rb8x8.setSelected(true);
        boardSize = 8;

        sizeGroup.selectedToggleProperty().addListener((obs, old, nv) -> {
            boardSize = rb16x16.isSelected() ? 16 : 8;
        });

        seriesWarnsdorff.setName("Warnsdorff");
        seriesBacktracking.setName("Backtracking");
        timeChart.getData().addAll(seriesWarnsdorff, seriesBacktracking);
    }

    @FXML
    public void startNewRound() {
        btnNewRound.setDisable(true);
        btnCheckTour.setDisable(true);
        btnShowSolution.setDisable(true);
        lblStatus.setText("Computing tours...");
        playerClicks.clear();
        lblResult.setText("");

        startRow = rng.nextInt(boardSize);
        startCol = rng.nextInt(boardSize);
        lblStart.setText("Start: (" + (startRow + 1) + ", " + (startCol + 1) + ")");
        lblWarnsdorff.setText("Warnsdorff: computing...");
        lblBacktracking.setText("Backtracking: computing...");

        final int capturedRow = startRow;
        final int capturedCol = startCol;
        final int capturedSize = boardSize;

        Thread bg = new Thread(() -> {
            // Warnsdorff (fast heuristic)
            long startW = System.currentTimeMillis();
            WarnsdorffSolver wSolver = new WarnsdorffSolver(capturedSize);
            int[] wTour = wSolver.solve(capturedRow, capturedCol);
            long wMs = System.currentTimeMillis() - startW;

            // Backtracking — actual time recorded, no timeout
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

                lblStatus.setText("Click cells in the tour order, then press 'Check Tour'");
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
            showAlert("Validation", "Please enter your name.");
            return;
        }
        if (playerClicks.isEmpty()) {
            showAlert("Incomplete", "Please click cells to build your tour.");
            return;
        }

        int requiredStart = startRow * boardSize + startCol;
        if (playerClicks.get(0) != requiredStart) {
            showAlert("Wrong Start",
                "Your tour must start at the marked cell (" + (startRow + 1) + ", " + (startCol + 1) + "). " +
                "Please clear your path (start New Round) and begin from the red circle.");
            return;
        }

        int total = boardSize * boardSize;

        if (playerClicks.size() < total) {
            if (playerClicks.size() >= total / 2 && isValidPartialKnightPath(playerClicks, boardSize)) {
                lblResult.setText("DRAW — Valid partial tour! You covered " +
                    playerClicks.size() + " of " + total + " cells correctly from the start.");
                lblResult.setStyle("-fx-text-fill: #f9a826; -fx-font-weight: bold; -fx-font-size: 14;");
            } else {
                showAlert("Incomplete", "You need to click all " + total + " cells. " +
                    "You've clicked " + playerClicks.size() + " so far.");
            }
            return;
        }

        if (isValidKnightTour(playerClicks, boardSize)) {
            lblResult.setText("Correct Knight's Tour! You WIN!");
            lblResult.setStyle("-fx-text-fill: #4ecca3; -fx-font-weight: bold; -fx-font-size: 14;");
            String seq = playerClicks.stream().map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
            if (currentRoundId > 0) Game4DB.saveWinner(currentRoundId, name, seq);
        } else {
            lblResult.setText("Invalid tour. Try again or click 'Show Solution'.");
            lblResult.setStyle("-fx-text-fill: #e94560; -fx-font-weight: bold; -fx-font-size: 14;");
        }
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
    public void showSolution() {
        if (correctTour != null) {
            playerClicks.clear();
            for (int cell : correctTour) playerClicks.add(cell);
            drawBoard(playerClicks);
            lblResult.setText("Solution shown (Warnsdorff's tour).");
            lblResult.setStyle("-fx-text-fill: #f9a826; -fx-font-weight: bold;");
        }
    }

    @FXML
    public void handleCanvasClick(javafx.scene.input.MouseEvent e) {
        double cellW = boardCanvas.getWidth() / boardSize;
        double cellH = boardCanvas.getHeight() / boardSize;
        int col = (int)(e.getX() / cellW);
        int row = (int)(e.getY() / cellH);
        if (row >= 0 && row < boardSize && col >= 0 && col < boardSize) {
            int cell = row * boardSize + col;
            if (!playerClicks.contains(cell)) {
                playerClicks.add(cell);
                drawBoard(playerClicks);
            }
        }
    }

    private void drawBoard(List<Integer> highlighted) {
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();
        double w = boardCanvas.getWidth();
        double h = boardCanvas.getHeight();
        double cw = w / boardSize;
        double ch = h / boardSize;

        gc.clearRect(0, 0, w, h);
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                Color fill = ((r + c) % 2 == 0) ? Color.web("#c9b99a") : Color.web("#6b4226");
                gc.setFill(fill);
                gc.fillRect(c * cw, r * ch, cw, ch);
            }
        }

        double sx = startCol * cw + cw / 2;
        double sy = startRow * ch + ch / 2;
        gc.setFill(Color.web("#e94560"));
        gc.fillOval(sx - cw * 0.3, sy - ch * 0.3, cw * 0.6, ch * 0.6);

        if (highlighted != null) {
            for (int step = 0; step < highlighted.size(); step++) {
                int cell = highlighted.get(step);
                int r = cell / boardSize;
                int c = cell % boardSize;
                double x = c * cw + 4;
                double y = r * ch + 4;
                gc.setFill(Color.web("#4ecca399"));
                gc.fillRect(x, y, cw - 8, ch - 8);
                gc.setFill(Color.web("#1a1a2e"));
                gc.setFont(Font.font("Monospace", FontWeight.BOLD, Math.max(8, (int)(cw * 0.4))));
                gc.fillText(String.valueOf(step + 1), x + 4, y + ch * 0.6);
            }
            gc.setStroke(Color.web("#f9a826"));
            gc.setLineWidth(1.5);
            for (int i = 1; i < highlighted.size(); i++) {
                int c1 = highlighted.get(i - 1), c2 = highlighted.get(i);
                gc.strokeLine(
                    (c1 % boardSize) * cw + cw / 2, (c1 / boardSize) * ch + ch / 2,
                    (c2 % boardSize) * cw + cw / 2, (c2 / boardSize) * ch + ch / 2
                );
            }
        }
    }

    private boolean isValidKnightTour(List<Integer> cells, int n) {
        if (cells.size() != n * n) return false;
        boolean[] visited = new boolean[n * n];
        for (int i = 0; i < cells.size(); i++) {
            int cell = cells.get(i);
            if (cell < 0 || cell >= n * n) return false;
            if (visited[cell]) return false;
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

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

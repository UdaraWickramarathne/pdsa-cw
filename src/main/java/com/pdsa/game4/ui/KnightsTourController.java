package com.pdsa.game4.ui;

import com.pdsa.game4.algorithm.BacktrackingSolver;
import com.pdsa.game4.algorithm.WarnsdorffSolver;
import com.pdsa.game4.db.Game4DB;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class KnightsTourController {

    private static final int BACKTRACK_TIMEOUT_SEC = 30;

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

    private int boardSize;
    private int startRow, startCol;
    private int[] correctTour;        // from Warnsdorff
    private int currentRoundId = -1;
    private List<Integer> playerClicks = new ArrayList<>(); // cell indices in click order

    private final ToggleGroup sizeGroup = new ToggleGroup();
    private final Random rng = new Random();

    @FXML
    public void initialize() {
        rb8x8.setToggleGroup(sizeGroup);
        rb16x16.setToggleGroup(sizeGroup);
        rb8x8.setSelected(true);
        boardSize = 8;

        sizeGroup.selectedToggleProperty().addListener((obs, old, nv) -> {
            boardSize = rb16x16.isSelected() ? 16 : 8;
        });
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

        // Run Warnsdorff synchronously (fast)
        long startW = System.currentTimeMillis();
        WarnsdorffSolver wSolver = new WarnsdorffSolver(boardSize);
        int[] wTour = wSolver.solve(startRow, startCol);
        long wMs = System.currentTimeMillis() - startW;
        lblWarnsdorff.setText("Warnsdorff: " + wMs + " ms" + (wTour != null ? " ✓" : " (failed)"));

        correctTour = wTour;

        // Run Backtracking in background thread with timeout
        ExecutorService exec = Executors.newSingleThreadExecutor();
        BacktrackingSolver btSolver = new BacktrackingSolver(boardSize);
        Future<int[]> future = exec.submit(() -> btSolver.solve(startRow, startCol));
        exec.shutdown();

        Thread bgThread = new Thread(() -> {
            long startBT = System.currentTimeMillis();
            int[] btTour = null;
            long btMs = -1;
            try {
                btTour = future.get(BACKTRACK_TIMEOUT_SEC, TimeUnit.SECONDS);
                btMs = System.currentTimeMillis() - startBT;
            } catch (TimeoutException e) {
                btSolver.cancel();
                future.cancel(true);
                btMs = BACKTRACK_TIMEOUT_SEC * 1000L;
            } catch (Exception e) {
                btMs = -1;
            }

            final long finalBtMs = btMs;
            final boolean timedOut = btTour == null;

            Platform.runLater(() -> {
                String btLabel = timedOut
                    ? "Backtracking: TIMEOUT (>" + BACKTRACK_TIMEOUT_SEC + "s) — too slow for N=" + boardSize
                    : "Backtracking: " + finalBtMs + " ms ✓";
                lblBacktracking.setText(btLabel);

                int roundId = Game4DB.saveRound(boardSize, startRow, startCol, wMs, finalBtMs);
                currentRoundId = roundId;

                lblStatus.setText("Click cells in the tour order, then press 'Check Tour'");
                btnCheckTour.setDisable(false);
                btnShowSolution.setDisable(false);
                btnNewRound.setDisable(false);
                drawBoard(null);
            });
        });
        bgThread.setDaemon(true);
        bgThread.start();
    }

    @FXML
    public void checkTour() {
        String name = tfPlayerName.getText().trim();
        if (name.isEmpty()) {
            showAlert("Validation", "Please enter your name.");
            return;
        }
        if (playerClicks.size() < boardSize * boardSize) {
            showAlert("Incomplete", "You need to click all " + (boardSize * boardSize) + " cells. " +
                "You've clicked " + playerClicks.size() + " so far.");
            return;
        }

        // Validate: correct knight moves + visits every cell exactly once
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

        // Mark start position
        double sx = startCol * cw + cw / 2;
        double sy = startRow * ch + ch / 2;
        gc.setFill(Color.web("#e94560"));
        gc.fillOval(sx - cw * 0.3, sy - ch * 0.3, cw * 0.6, ch * 0.6);

        // Draw player's path
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
            // Draw lines between consecutive moves
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

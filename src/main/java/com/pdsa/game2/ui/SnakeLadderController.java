package com.pdsa.game2.ui;

import com.pdsa.game2.algorithm.BFSSolver;
import com.pdsa.game2.algorithm.DPSolver;
import com.pdsa.game2.db.Game2DB;
import com.pdsa.game2.model.Board;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Random;

public class SnakeLadderController {

    @FXML private Spinner<Integer> spinnerN;
    @FXML private Canvas boardCanvas;
    @FXML private TextArea lblBoardInfo;
    @FXML private Label lblBfsTime;
    @FXML private Label lblDpTime;
    @FXML private Label lblCorrectAnswer;
    @FXML private RadioButton rbChoice1;
    @FXML private RadioButton rbChoice2;
    @FXML private RadioButton rbChoice3;
    @FXML private TextField tfPlayerName;
    @FXML private Button btnSubmit;
    @FXML private Label lblResult;
    @FXML private Button btnNewRound;
    @FXML private BarChart<String, Number> timeChart;

    private Board currentBoard;
    private int correctAnswer;
    private int currentRoundId = -1;
    private final Random rng = new Random();

    private final ToggleGroup choiceGroup = new ToggleGroup();
    private final XYChart.Series<String, Number> seriesBfs = new XYChart.Series<>();
    private final XYChart.Series<String, Number> seriesDp  = new XYChart.Series<>();
    private int roundCount = 0;

    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> vf = new SpinnerValueFactory.IntegerSpinnerValueFactory(6, 12, 8);
        spinnerN.setValueFactory(vf);
        rbChoice1.setToggleGroup(choiceGroup);
        rbChoice2.setToggleGroup(choiceGroup);
        rbChoice3.setToggleGroup(choiceGroup);

        seriesBfs.setName("BFS");
        seriesDp.setName("Dynamic Programming");
        timeChart.getData().addAll(seriesBfs, seriesDp);
    }

    @FXML
    public void startNewRound() {
        int n;
        try {
            n = spinnerN.getValue();
            if (n < 6 || n > 12) throw new IllegalArgumentException();
        } catch (Exception e) {
            showAlert("Validation Error", "Board size N must be between 6 and 12.");
            return;
        }

        currentBoard = new Board(n);
        lblBoardInfo.setText(currentBoard.boardSummary());

        // Run BFS
        long startBfs = System.currentTimeMillis();
        BFSSolver bfs = new BFSSolver(currentBoard);
        int bfsAnswer = bfs.solve();
        long bfsMs = System.currentTimeMillis() - startBfs;

        // Run DP
        long startDp = System.currentTimeMillis();
        DPSolver dp = new DPSolver(currentBoard);
        int dpAnswer = dp.solve();
        long dpMs = System.currentTimeMillis() - startDp;

        correctAnswer = bfsAnswer; // both should agree

        lblBfsTime.setText("BFS: " + bfsMs + " ms  (answer: " + bfsAnswer + ")");
        lblDpTime.setText("DP:  " + dpMs  + " ms  (answer: " + dpAnswer  + ")");
        lblCorrectAnswer.setText("(hidden until submission)");

        // Save round
        currentRoundId = Game2DB.saveRound(n, correctAnswer, bfsMs, dpMs);

        // Update chart
        String label = "R" + (++roundCount);
        seriesBfs.getData().add(new XYChart.Data<>(label, bfsMs));
        seriesDp.getData().add(new XYChart.Data<>(label, dpMs));

        // Generate 3 choices: correct + 2 distractors
        int d1 = correctAnswer + 1 + rng.nextInt(3);
        int d2 = Math.max(1, correctAnswer - 1 - rng.nextInt(3));
        while (d2 == correctAnswer || d2 == d1) d2 = Math.max(1, correctAnswer - 1 - rng.nextInt(4));

        int[] choices = shuffle(new int[]{correctAnswer, d1, d2});
        rbChoice1.setText(String.valueOf(choices[0]));
        rbChoice2.setText(String.valueOf(choices[1]));
        rbChoice3.setText(String.valueOf(choices[2]));
        choiceGroup.selectToggle(null);

        lblResult.setText("");
        btnSubmit.setDisable(false);
        drawBoard();
    }

    @FXML
    public void submitAnswer() {
        RadioButton selected = (RadioButton) choiceGroup.getSelectedToggle();
        if (selected == null) {
            showAlert("Validation Error", "Please select one of the three choices.");
            return;
        }
        String name = tfPlayerName.getText().trim();
        if (name.isEmpty()) {
            showAlert("Validation Error", "Please enter your name.");
            return;
        }

        int playerAnswer;
        try {
            playerAnswer = Integer.parseInt(selected.getText());
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid choice.");
            return;
        }

        lblCorrectAnswer.setText("Correct answer: " + correctAnswer);

        if (playerAnswer == correctAnswer) {
            lblResult.setText("You WIN! Correct answer: " + correctAnswer);
            lblResult.setStyle("-fx-text-fill: #4ecca3; -fx-font-weight: bold;");
            if (currentRoundId > 0) Game2DB.saveWinner(currentRoundId, name, playerAnswer);
        } else if (Math.abs(playerAnswer - correctAnswer) <= 2) {
            lblResult.setText("DRAW — Very close! Correct answer was " + correctAnswer);
            lblResult.setStyle("-fx-text-fill: #f9a826; -fx-font-weight: bold;");
        } else {
            lblResult.setText("You LOSE. Correct answer was " + correctAnswer);
            lblResult.setStyle("-fx-text-fill: #e94560; -fx-font-weight: bold;");
        }
        btnSubmit.setDisable(true);
    }

    private void drawBoard() {
        if (currentBoard == null) return;
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();
        int n = currentBoard.getN();
        double w = boardCanvas.getWidth();
        double h = boardCanvas.getHeight();
        double cellW = w / n;
        double cellH = h / n;

        gc.setFill(Color.web("#16213e"));
        gc.fillRect(0, 0, w, h);

        // Draw cells
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                int cellNum = (n - 1 - row) * n + (row % 2 == 0 ? col + 1 : n - col);
                double x = col * cellW;
                double y = row * cellH;

                gc.setStroke(Color.web("#333355"));
                gc.setLineWidth(1);
                gc.strokeRect(x, y, cellW, cellH);

                gc.setFill(Color.web("#1a1a2e"));
                gc.fillRect(x + 1, y + 1, cellW - 2, cellH - 2);

                gc.setFill(Color.web("#a8a8b3"));
                gc.setFont(Font.font(9));
                gc.fillText(String.valueOf(cellNum), x + 3, y + 12);
            }
        }

        // Draw ladders (green)
        currentBoard.getLadders().forEach((base, top) -> {
            double[] bPos = cellCenter(base, n, cellW, cellH);
            double[] tPos = cellCenter(top, n, cellW, cellH);
            gc.setStroke(Color.web("#4ecca3"));
            gc.setLineWidth(2.5);
            gc.strokeLine(bPos[0], bPos[1], tPos[0], tPos[1]);
        });

        // Draw snakes (red)
        currentBoard.getSnakes().forEach((mouth, tail) -> {
            double[] mPos = cellCenter(mouth, n, cellW, cellH);
            double[] tPos = cellCenter(tail, n, cellW, cellH);
            gc.setStroke(Color.web("#e94560"));
            gc.setLineWidth(2.5);
            gc.strokeLine(mPos[0], mPos[1], tPos[0], tPos[1]);
        });
    }

    private double[] cellCenter(int cell, int n, double cellW, double cellH) {
        int idx = cell - 1;
        int row = idx / n;
        int col = idx % n;
        // Board renders bottom-to-top; even rows left-to-right, odd rows right-to-left
        int displayRow = n - 1 - row;
        int displayCol = (row % 2 == 0) ? col : n - 1 - col;
        return new double[]{displayCol * cellW + cellW / 2, displayRow * cellH + cellH / 2};
    }

    private int[] shuffle(int[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
        }
        return arr;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

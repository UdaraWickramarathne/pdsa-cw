package com.pdsa.game2.ui;

import com.pdsa.game2.algorithm.BFSSolver;
import com.pdsa.game2.algorithm.DPSolver;
import com.pdsa.game2.db.Game2DB;
import com.pdsa.game2.model.Board;
import com.pdsa.util.Banner;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.List;
import java.util.Random;

public class SnakeLadderController {

    @FXML private Spinner<Integer> spinnerN;
    @FXML private Canvas boardCanvas;
    @FXML private TextArea lblBoardInfo;
    @FXML private Label lblBfsTime;
    @FXML private Label lblDpTime;
    @FXML private Label lblCorrectAnswer;
    @FXML private Button btnChoice1;
    @FXML private Button btnChoice2;
    @FXML private Button btnChoice3;
    @FXML private TextField tfPlayerName;
    @FXML private Button btnSubmit;
    @FXML private Label lblResult;
    @FXML private Button btnNewRound;
    @FXML private Label lblRoundBadge;
    @FXML private BarChart<String, Number> timeChart;
    @FXML private ProgressIndicator piBoard;
    @FXML private HBox bannerBox;
    @FXML private Label bannerLabel;
    @FXML private VBox recordsDrawer;
    @FXML private TabPane recordsTabPane;
    @FXML private ToggleButton btnHistory;

    private Board currentBoard;
    private int correctAnswer;
    private int currentRoundId = -1;
    private int selectedChoice = -1;
    private final Random rng = new Random();
    private int roundCount = 0;

    private final XYChart.Series<String, Number> seriesBfs = new XYChart.Series<>();
    private final XYChart.Series<String, Number> seriesDp  = new XYChart.Series<>();

    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> vf = new SpinnerValueFactory.IntegerSpinnerValueFactory(6, 12, 8);
        spinnerN.setValueFactory(vf);

        seriesBfs.setName("BFS");
        seriesDp.setName("Dynamic Programming");
        timeChart.getData().addAll(seriesBfs, seriesDp);

        recordsDrawer.setTranslateX(300);
        recordsDrawer.setVisible(false);

        btnSubmit.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) {
                scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.ENTER), () -> { if (!btnSubmit.isDisabled()) submitAnswer(); });
                scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.N), this::startNewRound);
                scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.ESCAPE), () -> scene.getWindow().hide());
            }
        });
    }

    @FXML
    public void startNewRound() {
        int n;
        try {
            n = spinnerN.getValue();
            if (n < 6 || n > 12) throw new IllegalArgumentException();
        } catch (Exception e) {
            Banner.show(bannerBox, bannerLabel, Banner.Kind.WARN, "Board size N must be between 6 and 12.");
            return;
        }

        currentBoard = new Board(n);
        lblBoardInfo.setText(currentBoard.boardSummary());

        long startBfs = System.currentTimeMillis();
        BFSSolver bfs = new BFSSolver(currentBoard);
        int bfsAnswer = bfs.solve();
        long bfsMs = System.currentTimeMillis() - startBfs;

        long startDp = System.currentTimeMillis();
        DPSolver dp = new DPSolver(currentBoard);
        int dpAnswer = dp.solve();
        long dpMs = System.currentTimeMillis() - startDp;

        correctAnswer = bfsAnswer;
        currentRoundId = Game2DB.saveRound(n, correctAnswer, bfsMs, dpMs);
        lblRoundBadge.setText("ROUND " + String.format("%03d", currentRoundId));

        lblBfsTime.setText("BFS: " + bfsMs + " ms  (answer: " + bfsAnswer + ")");
        lblDpTime.setText("DP:  " + dpMs  + " ms  (answer: " + dpAnswer + ")");
        lblCorrectAnswer.setText("Answer hidden — guess first!");

        String label = "R" + (++roundCount);
        seriesBfs.getData().add(new XYChart.Data<>(label, bfsMs));
        seriesDp.getData().add(new XYChart.Data<>(label, dpMs));

        int d1 = correctAnswer + 1 + rng.nextInt(3);
        int d2 = Math.max(1, correctAnswer - 1 - rng.nextInt(3));
        while (d2 == correctAnswer || d2 == d1) d2 = Math.max(1, correctAnswer - 1 - rng.nextInt(4));

        int[] choices = shuffle(new int[]{correctAnswer, d1, d2});
        btnChoice1.setText(String.valueOf(choices[0]));
        btnChoice2.setText(String.valueOf(choices[1]));
        btnChoice3.setText(String.valueOf(choices[2]));

        clearChoiceSelection();
        lblResult.setText("");
        lblResult.getStyleClass().setAll();
        btnSubmit.setDisable(true);
        drawBoard();
    }

    @FXML public void selectChoice1() { selectChoice(btnChoice1); }
    @FXML public void selectChoice2() { selectChoice(btnChoice2); }
    @FXML public void selectChoice3() { selectChoice(btnChoice3); }

    private void selectChoice(Button btn) {
        clearChoiceSelection();
        btn.getStyleClass().add("selected");
        selectedChoice = Integer.parseInt(btn.getText());
        btnSubmit.setDisable(tfPlayerName.getText().trim().isEmpty());
    }

    private void clearChoiceSelection() {
        btnChoice1.getStyleClass().remove("selected");
        btnChoice2.getStyleClass().remove("selected");
        btnChoice3.getStyleClass().remove("selected");
        selectedChoice = -1;
    }

    @FXML
    public void submitAnswer() {
        if (selectedChoice < 0) {
            Banner.show(bannerBox, bannerLabel, Banner.Kind.WARN, "Please select one of the three choices.");
            return;
        }
        String name = tfPlayerName.getText().trim();
        if (name.isEmpty()) {
            Banner.show(bannerBox, bannerLabel, Banner.Kind.WARN, "Please enter your name.");
            return;
        }

        lblCorrectAnswer.setText("Correct answer: " + correctAnswer);
        lblResult.getStyleClass().setAll();

        if (selectedChoice == correctAnswer) {
            lblResult.setText("★ YOU WIN!  Correct: " + correctAnswer);
            lblResult.getStyleClass().add("result-win");
            if (currentRoundId > 0) Game2DB.saveWinner(currentRoundId, name, selectedChoice);
        } else if (Math.abs(selectedChoice - correctAnswer) <= 2) {
            lblResult.setText("◆ DRAW — Very close! Answer: " + correctAnswer);
            lblResult.getStyleClass().add("result-draw");
        } else {
            lblResult.setText("✗ LOSE.  Answer was " + correctAnswer);
            lblResult.getStyleClass().add("result-lose");
        }
        btnSubmit.setDisable(true);
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

    @FXML public void closeDrawer() { btnHistory.setSelected(false); toggleHistory(); }

    private void loadRecordsData() {
        recordsTabPane.getTabs().clear();

        TableView<javafx.collections.ObservableList<String>> roundsTable =
            buildTable("Round#", "N", "BFS (ms)", "DP (ms)", "Answer", "Date");
        populate(roundsTable, Game2DB.getRecentRounds(50));
        recordsTabPane.getTabs().add(new Tab("Rounds", roundsTable));

        TableView<javafx.collections.ObservableList<String>> winnersTable =
            buildTable("#", "Round#", "Player", "Answer", "Date");
        populate(winnersTable, Game2DB.getRecentWinners(50));
        recordsTabPane.getTabs().add(new Tab("Winners", winnersTable));
    }

    private TableView<javafx.collections.ObservableList<String>> buildTable(String... headers) {
        TableView<javafx.collections.ObservableList<String>> table = new TableView<>();
        for (int i = 0; i < headers.length; i++) {
            final int col = i;
            TableColumn<javafx.collections.ObservableList<String>, String> tc = new TableColumn<>(headers[i]);
            tc.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().get(col)));
            tc.setPrefWidth(80);
            table.getColumns().add(tc);
        }
        return table;
    }

    private void populate(TableView<javafx.collections.ObservableList<String>> table, List<String[]> rows) {
        for (String[] row : rows)
            table.getItems().add(FXCollections.observableArrayList(row));
        if (rows.isEmpty())
            table.setPlaceholder(new Label("No records yet."));
    }

    private void drawBoard() {
        if (currentBoard == null) return;
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();
        int n = currentBoard.getN();
        double w = boardCanvas.getWidth();
        double h = boardCanvas.getHeight();
        double cellW = w / n;
        double cellH = h / n;

        gc.setFill(Color.web("#0F001E"));
        gc.fillRect(0, 0, w, h);

        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                int cellNum = (n - 1 - row) * n + (row % 2 == 0 ? col + 1 : n - col);
                double x = col * cellW;
                double y = row * cellH;

                // Checkerboard: two surface shades
                gc.setFill(Color.web((row + col) % 2 == 0 ? "#160032" : "#1F0D4A"));
                gc.fillRect(x, y, cellW, cellH);

                gc.setStroke(Color.web("#2A1060"));
                gc.setLineWidth(0.5);
                gc.strokeRect(x, y, cellW, cellH);

                gc.setFill(Color.web("#6A5A9C"));
                gc.setFont(Font.font("Monospace", 8));
                gc.fillText(String.valueOf(cellNum), x + 3, y + 11);
            }
        }

        // Ladders (cyan)
        currentBoard.getLadders().forEach((base, top) -> {
            double[] bPos = cellCenter(base, n, cellW, cellH);
            double[] tPos = cellCenter(top, n, cellW, cellH);
            gc.setStroke(Color.web("#00F5FF"));
            gc.setLineWidth(2.5);
            gc.strokeLine(bPos[0], bPos[1], tPos[0], tPos[1]);
        });

        // Snakes (magenta)
        currentBoard.getSnakes().forEach((mouth, tail) -> {
            double[] mPos = cellCenter(mouth, n, cellW, cellH);
            double[] tPos = cellCenter(tail, n, cellW, cellH);
            gc.setStroke(Color.web("#FF2E88"));
            gc.setLineWidth(2.5);
            gc.strokeLine(mPos[0], mPos[1], tPos[0], tPos[1]);
        });
    }

    private double[] cellCenter(int cell, int n, double cellW, double cellH) {
        int idx = cell - 1;
        int row = idx / n;
        int col = idx % n;
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
}

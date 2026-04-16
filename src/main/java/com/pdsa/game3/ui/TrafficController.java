package com.pdsa.game3.ui;

import com.pdsa.game3.algorithm.EdmondsKarp;
import com.pdsa.game3.algorithm.FordFulkerson;
import com.pdsa.game3.db.Game3DB;
import com.pdsa.game3.model.FlowGraph;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.List;

public class TrafficController {

    private static final double[][] NODE_POS = {
        {60,  190}, // A (source)
        {200, 90},  // B
        {200, 190}, // C
        {200, 290}, // D
        {340, 90},  // E
        {340, 190}, // F
        {480, 90},  // G
        {480, 190}, // H
        {580, 190}, // T (sink)
    };

    @FXML private Canvas graphCanvas;
    @FXML private Label lblFF;
    @FXML private Label lblEK;
    @FXML private TextField tfPlayerName;
    @FXML private TextField tfAnswer;
    @FXML private Button btnSubmit;
    @FXML private Label lblResult;
    @FXML private Label lblRoundInfo;
    @FXML private Label lblRoundBadge;
    @FXML private BarChart<String, Number> timeChart;
    @FXML private StackPane startOverlay;
    @FXML private HBox bannerBox;
    @FXML private Label bannerLabel;
    @FXML private VBox recordsDrawer;
    @FXML private TabPane recordsTabPane;
    @FXML private ToggleButton btnHistory;

    private FlowGraph graph;
    private int correctMaxFlow;
    private int currentRoundId = -1;
    private boolean roundStarted = false;

    private final XYChart.Series<String, Number> seriesFF = new XYChart.Series<>();
    private final XYChart.Series<String, Number> seriesEK = new XYChart.Series<>();
    private int roundCount = 0;

    @FXML
    public void initialize() {
        seriesFF.setName("Ford-Fulkerson");
        seriesEK.setName("Edmonds-Karp");
        timeChart.getData().addAll(seriesFF, seriesEK);

        recordsDrawer.setTranslateX(300);
        recordsDrawer.setVisible(false);

        // Show the start overlay; hide after first round starts
        startOverlay.setVisible(true);
        startOverlay.setManaged(true);

        tfAnswer.sceneProperty().addListener((obs, old, scene) -> {
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
        // Hide start overlay after first use
        if (!roundStarted) {
            startOverlay.setVisible(false);
            startOverlay.setManaged(false);
            roundStarted = true;
        }

        graph = new FlowGraph();

        long startFF = System.currentTimeMillis();
        FordFulkerson ff = new FordFulkerson(graph);
        int ffFlow = ff.maxFlow();
        long ffMs = System.currentTimeMillis() - startFF;

        long startEK = System.currentTimeMillis();
        EdmondsKarp ek = new EdmondsKarp(graph);
        int ekFlow = ek.maxFlow();
        long ekMs = System.currentTimeMillis() - startEK;

        correctMaxFlow = ffFlow;
        currentRoundId = Game3DB.saveRound(correctMaxFlow, ffMs, ekMs);
        lblRoundBadge.setText("ROUND " + String.format("%03d", currentRoundId));

        lblFF.setText("Ford-Fulkerson: " + ffMs + " ms  (max flow = " + ffFlow + ")");
        lblEK.setText("Edmonds-Karp:   " + ekMs + " ms  (max flow = " + ekFlow + ")");
        lblRoundInfo.setText("Round #" + currentRoundId + "  ·  Correct max flow: ??? (submit to reveal)");
        lblResult.setText("");
        lblResult.getStyleClass().setAll();
        tfAnswer.clear();
        btnSubmit.setDisable(false);
        drawGraph(false);

        String label = "R" + (++roundCount);
        seriesFF.getData().add(new XYChart.Data<>(label, ffMs));
        seriesEK.getData().add(new XYChart.Data<>(label, ekMs));
    }

    @FXML
    public void submitAnswer() {
        String name = tfPlayerName.getText().trim();
        if (name.isEmpty()) {
            Banner.show(bannerBox, bannerLabel, Banner.Kind.WARN, "Please enter your name.");
            return;
        }
        int playerAnswer;
        try {
            playerAnswer = Integer.parseInt(tfAnswer.getText().trim());
            if (playerAnswer <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Banner.show(bannerBox, bannerLabel, Banner.Kind.WARN, "Please enter a positive integer as your answer.");
            return;
        }

        lblRoundInfo.setText("Round #" + currentRoundId + "  ·  Correct max flow: " + correctMaxFlow);
        lblResult.getStyleClass().setAll();
        drawGraph(true); // highlight path in yellow

        if (playerAnswer == correctMaxFlow) {
            lblResult.setText("★ YOU WIN!  Correct max flow: " + correctMaxFlow);
            lblResult.getStyleClass().add("result-win");
            if (currentRoundId > 0) Game3DB.saveWinner(currentRoundId, name, playerAnswer);
        } else if (Math.abs(playerAnswer - correctMaxFlow) <= 2) {
            lblResult.setText("◆ DRAW — Very close! Answer was " + correctMaxFlow);
            lblResult.getStyleClass().add("result-draw");
        } else {
            lblResult.setText("✗ LOSE.  Correct answer was " + correctMaxFlow);
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
        TableView<javafx.collections.ObservableList<String>> rt =
            buildTable("Round#", "Max Flow", "Ford-Fulkerson (ms)", "Edmonds-Karp (ms)", "Date");
        populate(rt, Game3DB.getRecentRounds(50));
        recordsTabPane.getTabs().add(new Tab("Rounds", rt));

        TableView<javafx.collections.ObservableList<String>> wt =
            buildTable("ID", "Round#", "Player", "Answer", "Date");
        populate(wt, Game3DB.getRecentWinners(50));
        recordsTabPane.getTabs().add(new Tab("Winners", wt));
    }

    private TableView<javafx.collections.ObservableList<String>> buildTable(String... headers) {
        TableView<javafx.collections.ObservableList<String>> table = new TableView<>();
        for (int i = 0; i < headers.length; i++) {
            final int col = i;
            TableColumn<javafx.collections.ObservableList<String>, String> tc = new TableColumn<>(headers[i]);
            tc.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().get(col)));
            tc.setPrefWidth(100);
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

    private void drawGraph(boolean highlightPath) {
        GraphicsContext gc = graphCanvas.getGraphicsContext2D();
        double w = graphCanvas.getWidth();
        double h = graphCanvas.getHeight();

        gc.setFill(Color.web("#0F001E"));
        gc.fillRect(0, 0, w, h);

        if (graph == null) return;

        int[][] edges = graph.getEdgeCapacities();
        String[] names = FlowGraph.NODE_NAMES;

        // Draw edges
        for (int[] edge : edges) {
            int u = edge[0], v = edge[1], cap = edge[2];
            double x1 = NODE_POS[u][0], y1 = NODE_POS[u][1];
            double x2 = NODE_POS[v][0], y2 = NODE_POS[v][1];

            gc.setStroke(Color.web("#2A1060"));
            gc.setLineWidth(2);
            gc.strokeLine(x1, y1, x2, y2);
            drawArrow(gc, x1, y1, x2, y2);

            double mx = (x1 + x2) / 2;
            double my = (y1 + y2) / 2;
            gc.setFill(Color.web("#FFE500"));
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
            gc.fillText(String.valueOf(cap), mx + 4, my - 4);
        }

        // Draw nodes
        for (int i = 0; i < NODE_POS.length; i++) {
            double x = NODE_POS[i][0];
            double y = NODE_POS[i][1];
            double r = 22;

            Color nodeColor = (i == FlowGraph.SOURCE) ? Color.web("#39FF14")
                           : (i == FlowGraph.SINK)   ? Color.web("#FF2E88")
                           : Color.web("#1F0D4A");
            gc.setFill(nodeColor);
            gc.fillOval(x - r, y - r, r * 2, r * 2);

            String borderHex = (i == FlowGraph.SOURCE) ? "#39FF14"
                             : (i == FlowGraph.SINK)   ? "#FF2E88"
                             : "#6A5A9C";
            gc.setStroke(Color.web(borderHex));
            gc.setLineWidth(1.5);
            gc.strokeOval(x - r, y - r, r * 2, r * 2);

            // Glow for source/sink
            if (i == FlowGraph.SOURCE || i == FlowGraph.SINK) {
                gc.setEffect(new javafx.scene.effect.DropShadow(10, 0, 0,
                    i == FlowGraph.SOURCE ? Color.web("#39FF14") : Color.web("#FF2E88")));
                gc.strokeOval(x - r, y - r, r * 2, r * 2);
                gc.setEffect(null);
            }

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
            gc.fillText(names[i], x - 6, y + 5);
        }
    }

    private void drawArrow(GraphicsContext gc, double x1, double y1, double x2, double y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double len = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        double arrowX = x1 + (len - 24) * Math.cos(angle);
        double arrowY = y1 + (len - 24) * Math.sin(angle);
        double arrowSize = 8;

        gc.setStroke(Color.web("#6A5A9C"));
        gc.setLineWidth(1.5);
        gc.strokeLine(arrowX, arrowY,
            arrowX - arrowSize * Math.cos(angle - 0.4),
            arrowY - arrowSize * Math.sin(angle - 0.4));
        gc.strokeLine(arrowX, arrowY,
            arrowX - arrowSize * Math.cos(angle + 0.4),
            arrowY - arrowSize * Math.sin(angle + 0.4));
    }
}

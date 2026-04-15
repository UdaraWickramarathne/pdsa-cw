package com.pdsa.game3.ui;

import com.pdsa.game3.algorithm.EdmondsKarp;
import com.pdsa.game3.algorithm.FordFulkerson;
import com.pdsa.game3.db.Game3DB;
import com.pdsa.game3.model.FlowGraph;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class TrafficController {

    // Node positions on 600x380 canvas
    private static final double[][] NODE_POS = {
        {60,  190}, // A (source)
        {200, 90},  // B
        {200, 190}, // C
        {200, 290}, // D
        {340, 90},  // E
        {340, 190}, // F (through D→F)
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

    private FlowGraph graph;
    private int correctMaxFlow;
    private int currentRoundId = -1;

    @FXML
    public void initialize() {
        startNewRound();
    }

    @FXML
    public void startNewRound() {
        graph = new FlowGraph();

        // Ford-Fulkerson
        long startFF = System.currentTimeMillis();
        FordFulkerson ff = new FordFulkerson(graph);
        int ffFlow = ff.maxFlow();
        long ffMs = System.currentTimeMillis() - startFF;

        // Edmonds-Karp
        long startEK = System.currentTimeMillis();
        EdmondsKarp ek = new EdmondsKarp(graph);
        int ekFlow = ek.maxFlow();
        long ekMs = System.currentTimeMillis() - startEK;

        correctMaxFlow = ffFlow;
        currentRoundId = Game3DB.saveRound(correctMaxFlow, ffMs, ekMs);

        lblFF.setText("Ford-Fulkerson: " + ffMs + " ms  (max flow = " + ffFlow + ")");
        lblEK.setText("Edmonds-Karp:   " + ekMs + " ms  (max flow = " + ekFlow + ")");
        lblRoundInfo.setText("Round ID: " + currentRoundId + "   |   Correct max flow: ???  (submit to reveal)");
        lblResult.setText("");
        tfAnswer.clear();
        btnSubmit.setDisable(false);
        drawGraph();
    }

    @FXML
    public void submitAnswer() {
        String name = tfPlayerName.getText().trim();
        if (name.isEmpty()) {
            showAlert("Validation Error", "Please enter your name.");
            return;
        }

        int playerAnswer;
        try {
            playerAnswer = Integer.parseInt(tfAnswer.getText().trim());
            if (playerAnswer <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a positive integer as your answer.");
            return;
        }

        lblRoundInfo.setText("Round ID: " + currentRoundId + "   |   Correct max flow: " + correctMaxFlow);

        if (playerAnswer == correctMaxFlow) {
            lblResult.setText("You WIN! Correct max flow: " + correctMaxFlow);
            lblResult.setStyle("-fx-text-fill: #4ecca3; -fx-font-weight: bold; -fx-font-size: 14;");
            if (currentRoundId > 0) Game3DB.saveWinner(currentRoundId, name, playerAnswer);
        } else if (Math.abs(playerAnswer - correctMaxFlow) <= 2) {
            lblResult.setText("DRAW — Very close! Correct answer was " + correctMaxFlow);
            lblResult.setStyle("-fx-text-fill: #f9a826; -fx-font-weight: bold; -fx-font-size: 14;");
        } else {
            lblResult.setText("You LOSE. Correct answer was " + correctMaxFlow);
            lblResult.setStyle("-fx-text-fill: #e94560; -fx-font-weight: bold; -fx-font-size: 14;");
        }
        btnSubmit.setDisable(true);
    }

    private void drawGraph() {
        GraphicsContext gc = graphCanvas.getGraphicsContext2D();
        double w = graphCanvas.getWidth();
        double h = graphCanvas.getHeight();

        gc.setFill(Color.web("#16213e"));
        gc.fillRect(0, 0, w, h);

        int[][] edges = graph.getEdgeCapacities();
        String[] names = FlowGraph.NODE_NAMES;

        // Draw edges
        for (int[] edge : edges) {
            int u = edge[0], v = edge[1], cap = edge[2];
            double x1 = NODE_POS[u][0], y1 = NODE_POS[u][1];
            double x2 = NODE_POS[v][0], y2 = NODE_POS[v][1];

            gc.setStroke(Color.web("#555577"));
            gc.setLineWidth(2);
            gc.strokeLine(x1, y1, x2, y2);

            // Arrowhead
            drawArrow(gc, x1, y1, x2, y2);

            // Capacity label at midpoint
            double mx = (x1 + x2) / 2;
            double my = (y1 + y2) / 2;
            gc.setFill(Color.web("#f9a826"));
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
            gc.fillText(String.valueOf(cap), mx + 4, my - 4);
        }

        // Draw nodes
        for (int i = 0; i < NODE_POS.length; i++) {
            double x = NODE_POS[i][0];
            double y = NODE_POS[i][1];
            double r = 22;

            Color nodeColor = (i == FlowGraph.SOURCE) ? Color.web("#4ecca3")
                           : (i == FlowGraph.SINK)   ? Color.web("#e94560")
                           : Color.web("#16213e");
            gc.setFill(nodeColor);
            gc.fillOval(x - r, y - r, r * 2, r * 2);
            gc.setStroke(Color.web("#a8a8b3"));
            gc.setLineWidth(1.5);
            gc.strokeOval(x - r, y - r, r * 2, r * 2);

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

        gc.setStroke(Color.web("#a8a8b3"));
        gc.setLineWidth(1.5);
        gc.strokeLine(arrowX, arrowY,
            arrowX - arrowSize * Math.cos(angle - 0.4),
            arrowY - arrowSize * Math.sin(angle - 0.4));
        gc.strokeLine(arrowX, arrowY,
            arrowX - arrowSize * Math.cos(angle + 0.4),
            arrowY - arrowSize * Math.sin(angle + 0.4));
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

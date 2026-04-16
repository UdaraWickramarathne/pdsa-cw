package com.pdsa.game2.ui;

import com.pdsa.game2.db.Game2DB;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class Game2RecordsView {

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Game 2 — Snake & Ladder Records");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        TableView<ObservableList<String>> roundsTable = buildTable(
            "Round#", "Board Size", "Min Throws", "BFS (ms)", "DP (ms)", "Date"
        );
        populate(roundsTable, Game2DB.getRecentRounds(100));

        TableView<ObservableList<String>> winnersTable = buildTable(
            "ID", "Round#", "Player", "Answer", "Date"
        );
        populate(winnersTable, Game2DB.getRecentWinners(100));

        tabPane.getTabs().addAll(
            new Tab("Rounds (last 100)", roundsTable),
            new Tab("Winners (last 100)", winnersTable)
        );

        VBox root = new VBox(tabPane);
        root.setStyle("-fx-background-color: #1a1a2e;");
        VBox.setVgrow(tabPane, javafx.scene.layout.Priority.ALWAYS);
        stage.setScene(new Scene(root, 680, 460));
        stage.show();
    }

    private TableView<ObservableList<String>> buildTable(String... headers) {
        TableView<ObservableList<String>> table = new TableView<>();
        table.setStyle("-fx-background-color: #16213e;");
        for (int i = 0; i < headers.length; i++) {
            final int col = i;
            TableColumn<ObservableList<String>, String> tc = new TableColumn<>(headers[i]);
            tc.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().get(col)));
            tc.setPrefWidth(110);
            table.getColumns().add(tc);
        }
        return table;
    }

    private void populate(TableView<ObservableList<String>> table, List<String[]> rows) {
        for (String[] row : rows) {
            table.getItems().add(FXCollections.observableArrayList(row));
        }
        if (rows.isEmpty()) {
            table.setPlaceholder(new Label("No records yet — play a round first."));
        }
    }
}

package com.pdsa.game5.ui;

import com.pdsa.game5.db.Game5DB;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class Game5RecordsView {

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Game 5 — Sixteen Queens Records");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        TableView<ObservableList<String>> timingTable = buildTable(
            "ID", "Sequential (ms)", "Threaded (ms)", "Total Solutions", "Date"
        );
        populate(timingTable, Game5DB.getAllTimingsFull());

        TableView<ObservableList<String>> winnersTable = buildTable(
            "ID", "Solution#", "Player", "Date"
        );
        populate(winnersTable, Game5DB.getRecentWinners(100));

        tabPane.getTabs().addAll(
            new Tab("Timing Runs", timingTable),
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
            tc.setPrefWidth(130);
            table.getColumns().add(tc);
        }
        return table;
    }

    private void populate(TableView<ObservableList<String>> table, List<String[]> rows) {
        for (String[] row : rows) {
            table.getItems().add(FXCollections.observableArrayList(row));
        }
        if (rows.isEmpty()) {
            table.setPlaceholder(new Label("No records yet — run the solver first."));
        }
    }
}

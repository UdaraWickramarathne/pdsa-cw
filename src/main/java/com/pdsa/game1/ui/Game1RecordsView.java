package com.pdsa.game1.ui;

import com.pdsa.game1.db.Game1DB;
import com.pdsa.util.Theme;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class Game1RecordsView {

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Game 1 — Minimum Cost Records");

        TableView<ObservableList<String>> table = buildTable(
            "Round#", "N", "Min Cost", "Hungarian (ms)", "Branch & Bound (ms)", "Date"
        );
        populate(table, Game1DB.getRecentRounds(100));

        Label info = new Label("Last 100 rounds  ·  Minimum Cost (Assignment Problem)");
        info.getStyleClass().add("ui-caption");
        info.setStyle("-fx-padding: 8 12;");

        VBox root = new VBox(4, info, table);
        root.getStyleClass().add("game-root");
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

        Scene scene = new Scene(root, 800, 480);
        Theme.apply(stage, scene);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    private TableView<ObservableList<String>> buildTable(String... headers) {
        TableView<ObservableList<String>> table = new TableView<>();
        for (int i = 0; i < headers.length; i++) {
            final int col = i;
            TableColumn<ObservableList<String>, String> tc = new TableColumn<>(headers[i]);
            tc.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().get(col)));
            tc.setPrefWidth(120);
            table.getColumns().add(tc);
        }
        return table;
    }

    private void populate(TableView<ObservableList<String>> table, List<String[]> rows) {
        for (String[] row : rows)
            table.getItems().add(FXCollections.observableArrayList(row));
        if (rows.isEmpty())
            table.setPlaceholder(new Label("No records yet — play a round first."));
    }
}

package com.pdsa.game5;

import com.pdsa.util.Theme;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SixteenQueensApp {

    public void show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/pdsa/game5/SixteenQueens.fxml")
            );
            Stage stage = new Stage();
            stage.setTitle("Game 5 — Sixteen Queens Puzzle");
            Scene scene = new Scene(loader.load(), 1120, 1020);
            Theme.apply(stage, scene);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(1020);
            stage.setMinHeight(920);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Could not load Sixteen Queens game: " + e.getMessage(), e);
        }
    }
}

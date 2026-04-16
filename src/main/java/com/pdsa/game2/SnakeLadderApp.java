package com.pdsa.game2;

import com.pdsa.util.Theme;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SnakeLadderApp {

    public void show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/pdsa/game2/SnakeLadder.fxml")
            );
            Stage stage = new Stage();
            stage.setTitle("Game 2 — Snake and Ladder");
            Scene scene = new Scene(loader.load(), 1100, 960);
            Theme.apply(stage, scene);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(1000);
            stage.setMinHeight(860);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Could not load Snake & Ladder game: " + e.getMessage(), e);
        }
    }
}

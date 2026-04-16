package com.pdsa.game4;

import com.pdsa.util.Theme;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class KnightsTourApp {

    public void show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/pdsa/game4/KnightsTour.fxml")
            );
            Stage stage = new Stage();
            stage.setTitle("Game 4 — Knight's Tour");
            Scene scene = new Scene(loader.load(), 1120, 1020);
            Theme.apply(stage, scene);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(1020);
            stage.setMinHeight(920);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Could not load Knight's Tour game: " + e.getMessage(), e);
        }
    }
}

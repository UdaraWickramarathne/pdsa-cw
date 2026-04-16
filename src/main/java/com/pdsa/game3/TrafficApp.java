package com.pdsa.game3;

import com.pdsa.util.Theme;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TrafficApp {

    public void show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/pdsa/game3/Traffic.fxml")
            );
            Stage stage = new Stage();
            stage.setTitle("Game 3 — Traffic Simulation (Max Flow)");
            Scene scene = new Scene(loader.load(), 1060, 980);
            Theme.apply(stage, scene);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(960);
            stage.setMinHeight(880);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Could not load Traffic Simulation game: " + e.getMessage(), e);
        }
    }
}

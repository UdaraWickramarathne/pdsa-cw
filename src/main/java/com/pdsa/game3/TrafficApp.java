package com.pdsa.game3;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for Game 3 — Traffic Simulation.
 * Called from the main menu. Opens in its own Stage.
 */
public class TrafficApp {

    public void show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/pdsa/game3/Traffic.fxml")
            );
            Stage stage = new Stage();
            stage.setTitle("Game 3 — Traffic Simulation (Max Flow)");
            stage.setScene(new Scene(loader.load(), 800, 820));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Could not load Traffic Simulation game: " + e.getMessage(), e);
        }
    }
}

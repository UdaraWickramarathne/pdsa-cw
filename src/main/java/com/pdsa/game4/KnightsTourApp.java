package com.pdsa.game4;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for Game 4 — Knight's Tour.
 * Called from the main menu. Opens in its own Stage.
 */
public class KnightsTourApp {

    public void show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/pdsa/game4/KnightsTour.fxml")
            );
            Stage stage = new Stage();
            stage.setTitle("Game 4 — Knight's Tour");
            stage.setScene(new Scene(loader.load(), 900, 620));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Could not load Knight's Tour game: " + e.getMessage(), e);
        }
    }
}

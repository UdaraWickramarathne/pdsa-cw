package com.pdsa.game2;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for Game 2 — Snake and Ladder.
 * Called from the main menu. Opens in its own Stage.
 */
public class SnakeLadderApp {

    public void show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/pdsa/game2/SnakeLadder.fxml")
            );
            Stage stage = new Stage();
            stage.setTitle("Game 2 — Snake and Ladder");
            stage.setScene(new Scene(loader.load(), 900, 620));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Could not load Snake & Ladder game: " + e.getMessage(), e);
        }
    }
}

package com.pdsa.game5;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for Game 5 — Sixteen Queens.
 * Called from the main menu. Opens in its own Stage.
 */
public class SixteenQueensApp {

    public void show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/pdsa/game5/SixteenQueens.fxml")
            );
            Stage stage = new Stage();
            stage.setTitle("Game 5 — Sixteen Queens Puzzle");
            stage.setScene(new Scene(loader.load(), 900, 640));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Could not load Sixteen Queens game: " + e.getMessage(), e);
        }
    }
}

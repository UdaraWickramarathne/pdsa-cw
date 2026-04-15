package com.pdsa.game1;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for Game 1 — Minimum Cost.
 * Called from the main menu. Opens in its own Stage.
 */
public class MinimumCostApp {

    public void show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/pdsa/game1/MinimumCost.fxml")
            );
            Stage stage = new Stage();
            stage.setTitle("Game 1 — Minimum Cost Assignment");
            stage.setScene(new Scene(loader.load(), 800, 560));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Could not load Minimum Cost game: " + e.getMessage(), e);
        }
    }
}

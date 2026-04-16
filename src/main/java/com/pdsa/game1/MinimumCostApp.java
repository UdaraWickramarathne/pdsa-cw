package com.pdsa.game1;

import com.pdsa.util.Theme;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MinimumCostApp {

    public void show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/pdsa/game1/MinimumCost.fxml")
            );
            Stage stage = new Stage();
            stage.setTitle("Game 1 — Minimum Cost Assignment");
            Scene scene = new Scene(loader.load(), 860, 820);
            Theme.apply(stage, scene);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(800);
            stage.setMinHeight(700);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Could not load Minimum Cost game: " + e.getMessage(), e);
        }
    }
}

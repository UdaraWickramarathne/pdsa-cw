package com.pdsa;

import com.pdsa.game1.MinimumCostApp;
import com.pdsa.game2.SnakeLadderApp;
import com.pdsa.game3.TrafficApp;
import com.pdsa.game4.KnightsTourApp;
import com.pdsa.game5.SixteenQueensApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class MainMenuController {

    @FXML
    public void openMinimumCost() {
        launchGame(() -> new MinimumCostApp().show());
    }

    @FXML
    public void openSnakeLadder() {
        launchGame(() -> new SnakeLadderApp().show());
    }

    @FXML
    public void openTrafficSimulation() {
        launchGame(() -> new TrafficApp().show());
    }

    @FXML
    public void openKnightsTour() {
        launchGame(() -> new KnightsTourApp().show());
    }

    @FXML
    public void openSixteenQueens() {
        launchGame(() -> new SixteenQueensApp().show());
    }

    private void launchGame(Runnable gameRunner) {
        try {
            gameRunner.run();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Launch Error");
            alert.setHeaderText("Failed to open game");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}

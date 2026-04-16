package com.pdsa;

import com.pdsa.game1.MinimumCostApp;
import com.pdsa.game1.ui.Game1RecordsView;
import com.pdsa.game2.SnakeLadderApp;
import com.pdsa.game2.ui.Game2RecordsView;
import com.pdsa.game3.TrafficApp;
import com.pdsa.game3.ui.Game3RecordsView;
import com.pdsa.game4.KnightsTourApp;
import com.pdsa.game4.ui.Game4RecordsView;
import com.pdsa.game5.SixteenQueensApp;
import com.pdsa.game5.ui.Game5RecordsView;
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

    @FXML
    public void openGame1Records() {
        launchGame(() -> new Game1RecordsView().show());
    }

    @FXML
    public void openGame2Records() {
        launchGame(() -> new Game2RecordsView().show());
    }

    @FXML
    public void openGame3Records() {
        launchGame(() -> new Game3RecordsView().show());
    }

    @FXML
    public void openGame4Records() {
        launchGame(() -> new Game4RecordsView().show());
    }

    @FXML
    public void openGame5Records() {
        launchGame(() -> new Game5RecordsView().show());
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

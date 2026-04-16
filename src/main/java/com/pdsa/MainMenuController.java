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
import com.pdsa.util.Banner;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MainMenuController {

    @FXML private VBox root;
    @FXML private HBox bannerBox;
    @FXML private Label bannerLabel;

    @FXML
    public void initialize() {
        // Wire keyboard shortcuts once the scene is available
        root.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) setupKeyboard(scene);
        });
    }

    private void setupKeyboard(javafx.scene.Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            boolean shift = e.isShiftDown();
            KeyCode code  = e.getCode();

            if (code == KeyCode.ESCAPE) {
                scene.getWindow().hide();
                e.consume();
                return;
            }

            if (!shift) {
                switch (code) {
                    case DIGIT1, NUMPAD1 -> { launchGame(() -> new MinimumCostApp().show()); e.consume(); }
                    case DIGIT2, NUMPAD2 -> { launchGame(() -> new SnakeLadderApp().show()); e.consume(); }
                    case DIGIT3, NUMPAD3 -> { launchGame(() -> new TrafficApp().show()); e.consume(); }
                    case DIGIT4, NUMPAD4 -> { launchGame(() -> new KnightsTourApp().show()); e.consume(); }
                    case DIGIT5, NUMPAD5 -> { launchGame(() -> new SixteenQueensApp().show()); e.consume(); }
                    default -> {}
                }
            } else {
                switch (code) {
                    case DIGIT1, NUMPAD1 -> { launchGame(() -> new Game1RecordsView().show()); e.consume(); }
                    case DIGIT2, NUMPAD2 -> { launchGame(() -> new Game2RecordsView().show()); e.consume(); }
                    case DIGIT3, NUMPAD3 -> { launchGame(() -> new Game3RecordsView().show()); e.consume(); }
                    case DIGIT4, NUMPAD4 -> { launchGame(() -> new Game4RecordsView().show()); e.consume(); }
                    case DIGIT5, NUMPAD5 -> { launchGame(() -> new Game5RecordsView().show()); e.consume(); }
                    default -> {}
                }
            }
        });
    }

    @FXML public void openMinimumCost()      { launchGame(() -> new MinimumCostApp().show()); }
    @FXML public void openSnakeLadder()      { launchGame(() -> new SnakeLadderApp().show()); }
    @FXML public void openTrafficSimulation(){ launchGame(() -> new TrafficApp().show()); }
    @FXML public void openKnightsTour()      { launchGame(() -> new KnightsTourApp().show()); }
    @FXML public void openSixteenQueens()    { launchGame(() -> new SixteenQueensApp().show()); }
    @FXML public void openGame1Records()     { launchGame(() -> new Game1RecordsView().show()); }
    @FXML public void openGame2Records()     { launchGame(() -> new Game2RecordsView().show()); }
    @FXML public void openGame3Records()     { launchGame(() -> new Game3RecordsView().show()); }
    @FXML public void openGame4Records()     { launchGame(() -> new Game4RecordsView().show()); }
    @FXML public void openGame5Records()     { launchGame(() -> new Game5RecordsView().show()); }

    private void launchGame(Runnable gameRunner) {
        try {
            gameRunner.run();
        } catch (Exception e) {
            Banner.show(bannerBox, bannerLabel, Banner.Kind.ERROR,
                "Failed to open game: " + e.getMessage());
        }
    }
}

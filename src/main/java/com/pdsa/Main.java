package com.pdsa;

import com.pdsa.util.Theme;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Theme.loadFonts();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pdsa/MainMenu.fxml"));
        Scene scene = new Scene(loader.load(), 960, 640);
        Theme.apply(primaryStage, scene);
        primaryStage.setTitle("PDSA Arcade");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(880);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

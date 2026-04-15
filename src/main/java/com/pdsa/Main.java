package com.pdsa;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pdsa/MainMenu.fxml"));
        Scene scene = new Scene(loader.load(), 700, 500);
        scene.getStylesheets().add(getClass().getResource("/com/pdsa/style.css").toExternalForm());
        primaryStage.setTitle("PDSA Coursework — Game Menu");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

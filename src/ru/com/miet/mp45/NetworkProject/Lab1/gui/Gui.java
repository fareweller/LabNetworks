package ru.com.miet.mp45.NetworkProject.Lab1.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Gui extends Application {
    public static void Main(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("gui.fxml"));
        primaryStage.setTitle("Satan's  Chat Of Fun");
        primaryStage.setScene(new Scene(root, 640, 400));
        primaryStage.show();
    }
}

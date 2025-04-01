package com.lab;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class TypingApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/game.fxml")));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

        primaryStage.setTitle("Typing Speed Test");
        primaryStage.setScene(scene);

        primaryStage.setMaximized(true); // ✅ เปิดเต็มจอตอนเริ่ม

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

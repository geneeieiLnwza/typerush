package com.lab;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class TypeRush extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("pimyada & phimlaphat");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(5));

        Label welcomeLabel = new Label("Welcome to TypingBuddy");
        welcomeLabel.setFont(Font.font(16));
        BorderPane.setMargin(welcomeLabel, new Insets(5));
        root.setTop(welcomeLabel);

        VBox centerPane = new VBox(10);
        centerPane.setPadding(new Insets(5));

        Label titleLabel = new Label("A World of Tales");
        titleLabel.setFont(Font.font(16));

        TextArea showTextArea = new TextArea();
        showTextArea.setEditable(false);
        showTextArea.setWrapText(true);
        showTextArea.setFont(Font.font(16));
        showTextArea.setPrefRowCount(10);
        showTextArea.setPrefColumnCount(40);

        ScrollPane scrollPane = new ScrollPane(showTextArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPadding(new Insets(5));

        centerPane.getChildren().addAll(titleLabel, scrollPane);
        root.setCenter(centerPane);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

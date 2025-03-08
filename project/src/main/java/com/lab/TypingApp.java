package com.lab;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.Random;

public class TypingApp extends Application {
    private static final String[] SAMPLE_TEXTS = {
        "The quick brown fox jumps over the lazy dog.",
        "Java is a versatile and powerful programming language.",
        "Practice makes perfect, so keep typing!",
        "Coding is fun and rewarding when you master it.",
        "Always aim for accuracy before speed in typing."
    };
    
    private String currentText;
    private int timeLeft = 60;
    private Timeline timeline;
    private Label timerLabel;
    private TextArea typingArea;
    private Label resultLabel;
    private Label sampleText;
    private Random random = new Random();
    
    @Override
    public void start(Stage primaryStage) {
        Label titleLabel = new Label("Java Typing Speed Test");
        Label promptLabel = new Label("Type the following text:");
        sampleText = new Label();
        typingArea = new TextArea();
        typingArea.setDisable(true);
        Button startButton = new Button("Start Test");
        timerLabel = new Label("Time: " + timeLeft + "s");
        resultLabel = new Label();
        
        startButton.setOnAction(e -> startTest());
        
        VBox layout = new VBox(10, titleLabel, timerLabel, promptLabel, sampleText, typingArea, startButton, resultLabel);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        
        Scene scene = new Scene(layout, 500, 350);
        primaryStage.setTitle("Typing Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void startTest() {
        nextSentence();
        typingArea.setDisable(false);
        typingArea.setText("");
        typingArea.requestFocus();
        timeLeft = 60;
        timerLabel.setText("Time: " + timeLeft + "s");
        resultLabel.setText("");
        
        typingArea.setOnKeyReleased(e -> checkCompletion());
        
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;
            timerLabel.setText("Time: " + timeLeft + "s");
            if (timeLeft <= 0) {
                endTest();
            }
        }));
        timeline.setCycleCount(60);
        timeline.play();
    }
    
    private void nextSentence() {
        currentText = SAMPLE_TEXTS[random.nextInt(SAMPLE_TEXTS.length)];
        sampleText.setText(currentText);
        typingArea.setText("");
    }
    
    private void checkCompletion() {
        if (typingArea.getText().trim().equals(currentText)) {
            nextSentence();
        }
    }
    
    private void endTest() {
        timeline.stop();
        typingArea.setDisable(true);
        
        String typedText = typingArea.getText().trim();
        String[] words = typedText.split("\\s+");
        int correctWords = 0;
        
        String[] sampleWords = currentText.split("\\s+");
        
        StringBuilder resultText = new StringBuilder();
        for (int i = 0; i < Math.min(words.length, sampleWords.length); i++) {
            if (words[i].equals(sampleWords[i])) {
                resultText.append(words[i]).append(" ");
                correctWords++;
            } else {
                resultText.append("[")
                        .append(words[i])
                        .append("] ");
            }
        }
        
        int wpm = correctWords;
        Platform.runLater(() -> {
            resultLabel.setText("Results: " + wpm + " WPM");
            sampleText.setText(resultText.toString()); 
        });
    }
    public static void main(String[] args) {
        launch(args);
    }
}

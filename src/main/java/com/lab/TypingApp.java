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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
 
public class TypingApp extends Application {
    private static List<String> SAMPLE_TEXTS = new ArrayList<>(); 
    private String currentText;
    private int timeLeft = 60;
    private Timeline timeline;
    private Label timerLabel;
    private TextArea typingArea;
    private Label resultLabel;
    private Label sampleText;
    private Random random = new Random();
 
  
    static {
        loadSampleTexts();
    }
 
    private static void loadSampleTexts() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(TypingApp.class.getResourceAsStream("/text.csv"))))) { 
            String line;
            while ((line = reader.readLine()) != null) {
                SAMPLE_TEXTS.add(line.trim()); 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
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
        currentText = SAMPLE_TEXTS.get(random.nextInt(SAMPLE_TEXTS.size())); 
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
package com.lab;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class GameController {
    @FXML private Label sampleText;
    @FXML private Label timerLabel;
    @FXML private Label resultLabel;
    @FXML private TextArea typingArea;
    @FXML private Button startButton;

    private static final List<String> SAMPLE_TEXTS = new ArrayList<>();
    private String currentText;
    private int timeLeft = 60;
    private Timeline timeline;
    private Random random = new Random();
    private boolean testStarted = false;

    static {
        loadSampleTexts();
    }

    private static void loadSampleTexts() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(GameController.class.getResourceAsStream("/text.csv"))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                SAMPLE_TEXTS.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void startTest() {
        if (!testStarted) {
            testStarted = true;
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
            startButton.setText("Restart");
        } else {
            restartTest();
        }
    }

    private void restartTest() {
        testStarted = false;
        startTest();
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
                resultText.append("[").append(words[i]).append("] ");
            }
        }

        int wpm = (correctWords / 5);  
        Platform.runLater(() -> {
            resultLabel.setText("Results: " + wpm + " WPM");
            sampleText.setText(resultText.toString());
        });
    }
}

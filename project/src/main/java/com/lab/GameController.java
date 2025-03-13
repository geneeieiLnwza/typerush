package com.lab;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    @FXML private TextField typingField;
    @FXML private Button startButton;
    @FXML private ListView<String> textListView;
    @FXML private TextField textInputField;

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
        } catch (IOException | NullPointerException e) {
            System.out.println("Warning: text.csv not found or unreadable.");
        }
    }

    @FXML
    private void showResults() {
        resultLabel.setText("Results are displayed here!");
    }

    @FXML
    private void onAddText() {
        String newText = textInputField.getText().trim();
        if (!newText.isEmpty()) {
            SAMPLE_TEXTS.add(newText);
            textListView.getItems().add(newText);
            textInputField.clear();
        }
    }

    @FXML
    private void onEditText() {
        int selectedIndex = textListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            SAMPLE_TEXTS.set(selectedIndex, textInputField.getText().trim());
            textListView.getItems().set(selectedIndex, textInputField.getText().trim());
            textInputField.clear();
        }
    }

    @FXML
    private void onDeleteText() {
        int selectedIndex = textListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            SAMPLE_TEXTS.remove(selectedIndex);
            textListView.getItems().remove(selectedIndex);
        }
    }

    @FXML
    private void startTest() {
        if (!testStarted) {
            testStarted = true;
            nextSentence();
            typingField.setDisable(false);
            typingField.setText("");
            typingField.requestFocus();
            timeLeft = 60;
            timerLabel.setText("Time: " + timeLeft + "s");
            resultLabel.setText("");

            typingField.setOnKeyReleased(e -> checkCompletion());

            if (timeline != null) {
                timeline.stop();
            }

            timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                if (timeLeft > 0) {
                    timeLeft--;
                    Platform.runLater(() -> timerLabel.setText("Time: " + timeLeft + "s"));
                }
                if (timeLeft <= 0) {
                    endTest();
                }
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
            startButton.setText("Restart");
        } else {
            restartTest();
        }
    }

    private void restartTest() {
        testStarted = false;
        if (timeline != null) {
            timeline.stop();
        }
        startTest();
    }

    private void nextSentence() {
        if (SAMPLE_TEXTS.isEmpty()) {
            sampleText.setText("No text available.");
            return;
        }
        currentText = SAMPLE_TEXTS.get(random.nextInt(SAMPLE_TEXTS.size()));
        sampleText.setText(currentText);
        typingField.setText("");
    }

    private void checkCompletion() {
        if (typingField.getText().trim().equals(currentText)) {
            nextSentence();
        }
    }

    private void endTest() {
        if (timeline != null) {
            timeline.stop();
        }
        typingField.setDisable(true);

        String typedText = typingField.getText().trim();
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

        int wpm = correctWords / 5;
        Platform.runLater(() -> {
            resultLabel.setText("Results: " + wpm + " WPM");
            sampleText.setText(resultText.toString());
        });
    }
}

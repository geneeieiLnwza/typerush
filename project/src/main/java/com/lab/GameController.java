package com.lab;

import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.*;
import java.util.*;

public class GameController {

    @FXML
    private Label timerLabel;
    @FXML
    private TextArea typingField;
    @FXML
    private Button startButton;
    @FXML
    private Label resultLabelwpm;
    @FXML
    private Label resultLabelaccuracy;
    @FXML
    private TextFlow sampleTextFlow;

    private static final String BASE_CSV = "text.csv";
    public static final String USER_CSV = "text_user.csv";

    private String currentText;
    private int timeLeft = 60;
    private Timeline timeline;
    private boolean testStarted = false;
    private long startTime;
    private int correctWords = 0;
    private int totalTypedWords = 0;
    private int mistakeCount = 0;
    private int lastWpm = 0;
    private int lastScore = 0;

    @FXML
    private void startTest() {
        // Bounce animation
        ScaleTransition bounce = new ScaleTransition(Duration.millis(100), startButton);
        bounce.setFromX(1.0);
        bounce.setFromY(1.0);
        bounce.setToX(1.1);
        bounce.setToY(1.1);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(2);
        bounce.play();

        // Logic เดิมของการเริ่มเกม
        if (!testStarted) {
            testStarted = true;
            correctWords = 0;
            totalTypedWords = 0;
            mistakeCount = 0;
            startTime = System.currentTimeMillis();
            nextSentence();
            typingField.setDisable(false);
            typingField.setText("");
            typingField.setPrefHeight(40); // รีเซ็ตความสูงของ TextArea

            typingField.requestFocus();
            timeLeft = 60;
            timerLabel.setText("Time: " + timeLeft + "s");
            resultLabelwpm.setText("");
            resultLabelaccuracy.setText("");

            typingField.setOnKeyReleased(e -> checkCompletion());

            if (timeline != null) {
                timeline.stop();
            }

            timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                timeLeft = 60 - (int) elapsed;

                Platform.runLater(() -> {
                    timerLabel.setText("Time: " + timeLeft + "s");
                    updateResultsRealtime();
                });

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
        if (timeline != null)
            timeline.stop();
        startTest();
    }

    private void nextSentence() {
        List<String> texts = new ArrayList<>();
        loadFromCsv(BASE_CSV, texts);
        loadFromCsv(USER_CSV, texts);

        if (texts.isEmpty()) {
            sampleTextFlow.getChildren().setAll(new Text("No text available."));
            return;
        }

        currentText = texts.get(new Random().nextInt(texts.size()));

        // แปลง currentText เป็นตัว ๆ แล้วใส่ใน TextFlow
        sampleTextFlow.getChildren().clear();
        for (char c : currentText.toCharArray()) {
            Text t = new Text(String.valueOf(c));
            t.setStyle("-fx-fill: white; -fx-font-size: 18px;");
            sampleTextFlow.getChildren().add(t);
        }

        typingField.setText("");

        System.out.println("\uD83D\uDCC4 Loaded texts:");
        for (String text : texts) {
            System.out.println("\uD83D\uDC49 " + text);
        }
    }

    private void loadFromCsv(String path, List<String> list) {
        try {
            InputStream input;
            if (path.equals(BASE_CSV)) {
                input = getClass().getResourceAsStream("/" + path);
            } else {
                input = new FileInputStream(path);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line.trim());
            }
            reader.close();
        } catch (IOException | NullPointerException e) {
            System.out.println("⚠️ Error reading file: " + path);
        }
    }

    private void checkCompletion() {
        String typedText = typingField.getText();

        for (int i = 0; i < sampleTextFlow.getChildren().size(); i++) {
            Text letter = (Text) sampleTextFlow.getChildren().get(i);

            if (i < typedText.length()) {
                char typedChar = typedText.charAt(i);
                char expectedChar = currentText.charAt(i);

                if (typedChar == expectedChar) {
                    letter.setStyle("-fx-fill: #00ff00; -fx-font-size: 18px;");
                } else {
                    letter.setStyle("-fx-fill: #ff4444; -fx-font-size: 18px;");
                    shakeNode(typingField); // เพิ่มตรงนี้
                }
            } else {
                letter.setStyle("-fx-fill: white; -fx-font-size: 18px;");
            }
        }

        if (typedText.equals(currentText)) {
            String[] words = typedText.split("\\s+");
            correctWords += words.length;
            totalTypedWords += words.length;
            nextSentence();
        }
    }

    private void endTest() {
        if (timeline != null)
            timeline.stop();
        typingField.setDisable(true);

        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        if (elapsedTime == 0)
            elapsedTime = 1;

        int wpm = (int) ((correctWords * 60) / elapsedTime);
        int total = totalTypedWords == 0 ? correctWords : totalTypedWords;
        double baseScore = ((double) correctWords / total) * 100;
        double penalty = Math.min(mistakeCount * 5, 50);
        int finalScore = (int) Math.max(0, baseScore - penalty);

        lastWpm = wpm;
        lastScore = finalScore;
        resultLabelwpm.setText("Results: " + wpm + " WPM");
        resultLabelaccuracy.setText("Score: " + finalScore + "%");
    }

    private void updateResultsRealtime() {
        long elapsedMillis = System.currentTimeMillis() - startTime;
        double elapsedMinutes = elapsedMillis / 60000.0;

        String typed = typingField.getText();
        int correctChars = countCorrectCharacters(currentText, typed);
        int totalTyped = typed.length();

        int wpm = elapsedMinutes > 0 ? (int) ((correctChars / 5.0) / elapsedMinutes) : 0;
        double accuracy = totalTyped == 0 ? 100.0 : ((double) correctChars / totalTyped) * 100;

        resultLabelwpm.setText(String.format("Live: %d WPM", wpm));
        resultLabelaccuracy.setText(String.format("Accuracy: %.1f%%", accuracy));
    }

    private int countCorrectCharacters(String expected, String actual) {
        int correct = 0;
        int len = Math.min(expected.length(), actual.length());
        for (int i = 0; i < len; i++) {
            if (expected.charAt(i) == actual.charAt(i)) {
                correct++;
            }
        }
        return correct;
    }

    @FXML
    private GridPane root; // ← ต้องผูกจาก FXML ด้วย

    @FXML
    private void initialize() {
        // เมื่อกด Enter ให้เปลี่ยนประโยค
        typingField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                nextSentence();
            }
        });

        // ทำให้ TextArea ขยายความสูงอัตโนมัติตามเนื้อหา
        typingField.textProperty().addListener((obs, oldText, newText) -> {
            double height = computeTextAreaHeight(typingField);
            typingField.setPrefHeight(height);
        });

        // Responsive font: ฟอนต์ใหญ่ขึ้นตามความกว้างจอ
        root.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();

            double baseFontSize = width / 60;
            double bigFontSize = width / 80;

            resultLabelwpm.setStyle("-fx-font-size: " + bigFontSize + "px;");
            resultLabelaccuracy.setStyle("-fx-font-size: " + bigFontSize + "px;");
            timerLabel.setStyle("-fx-font-size: " + bigFontSize + "px;");
            typingField.setStyle("-fx-font-size: " + baseFontSize + "px;");
            startButton.setStyle("-fx-font-size: " + baseFontSize + "px;");
        });
        startButton.setCursor(javafx.scene.Cursor.HAND);
    }

    @FXML
    private void showResults() {
        resultLabelwpm.setText("Last Results: " + lastWpm + " WPM"); // ✅
        resultLabelaccuracy.setText("Score: " + lastScore + "%");
    }

    @FXML
    private void switchToManageTexts(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manage_texts.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Manage Sample Texts");
            currentStage.setMaximized(true);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getResourcePath(String filename) {
        return Objects.requireNonNull(getClass().getResource("/" + filename)).getPath();
    }

    private double computeTextAreaHeight(TextArea textArea) {
        Text text = new Text(textArea.getText());
        text.setFont(textArea.getFont());
        text.setWrappingWidth(textArea.getWidth() - 20); // เผื่อ padding
        return text.getLayoutBounds().getHeight() + 30; // เพิ่มเผื่อ padding
    }

    private void shakeNode(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.play();
    }

}

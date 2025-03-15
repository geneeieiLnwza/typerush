package com.lab;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.FileWriter;
import javafx.scene.Node;

public class GameController {
    @FXML
    private Label sampleText;
    @FXML
    private Label timerLabel;
    @FXML
    private Label resultLabel;
    @FXML
    private TextField typingField;
    @FXML
    private Button startButton;
    @FXML
    private ListView<String> textListView;
    @FXML
    private TextField textInputField;

    public static final List<String> SAMPLE_TEXTS = new ArrayList<>();
    private String currentText;
    private int timeLeft = 60;
    private Timeline timeline;
    private Random random = new Random();
    private boolean testStarted = false;
    private long startTime; // เวลาที่เริ่มพิมพ์จริง
    private int correctWords = 0; // คำที่พิมพ์ถูกต้องจริง ๆ

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

    // บันทึกข้อความตัวอย่างลงไฟล์ text.csv
    public static void saveSampleTexts() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("text.csv"))) {
            for (String text : SAMPLE_TEXTS) {
                writer.write(text);
                writer.newLine();
            }
            System.out.println("Saved sample texts to text.csv");
        } catch (IOException e) {
            System.out.println("Error saving text.csv: " + e.getMessage());
        }
    }

     // ตั้งค่าเริ่มต้นของการทดสอบ
    @FXML
    private void startTest() {
        if (!testStarted) {
            testStarted = true;
            correctWords = 0;
            startTime = System.currentTimeMillis(); // จับเวลาเริ่มต้น
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

            // ตัวจับเวลาถอยหลัง
            timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                timeLeft = 60 - (int) ((System.currentTimeMillis() - startTime) / 1000); // คำนวณเวลาที่เหลือจริง
                Platform.runLater(() -> timerLabel.setText("Time: " + timeLeft + "s"));
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

    // รีสตาร์ทการทดสอบ
    private void restartTest() {
        testStarted = false;
        if (timeline != null) {
            timeline.stop();
        }
        startTest();
    }

    @FXML
    private void openManageTexts(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manage_texts.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("Manage Sample Texts");
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // เลือกข้อความสุ่มและแสดงใน UI
    private void nextSentence() {
        if (SAMPLE_TEXTS.isEmpty()) {
            sampleText.setText("No text available.");
            return;
        }
        currentText = SAMPLE_TEXTS.get(random.nextInt(SAMPLE_TEXTS.size()));
        sampleText.setText(currentText);
        typingField.setText("");
    }

    // ตรวจสอบว่าผู้ใช้พิมพ์ถูกต้องหรือไม่
    private void checkCompletion() {
        String typedText = typingField.getText().trim();
        String[] typedWords = typedText.split("\\s+");
        String[] correctWordsArray = currentText.split("\\s+");

        if (typedText.equals(currentText)) {
            correctWords += correctWordsArray.length;
            nextSentence();
        }
    }

    @FXML
    private void initialize() {
        typingField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER: //ENTER ให้เปลี่ยนไปยังประโยคถัดไป
                    nextSentence();
                    break;
                default:
                    break;
            }
        });
    }

     // เปลี่ยนหน้าจอไปยังหน้า "Manage Sample Texts"
    @FXML
    private void switchToManageTexts(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manage_texts.fxml")); // ตรวจสอบ Path ให้ถูกต้อง
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root); // สร้าง Scene ใหม่
            stage.setScene(scene);
            stage.setTitle("Manage Sample Texts");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // แสดงข้อผิดพลาดหากมี
        }
    }

    private void endTest() {
        if (timeline != null) {
            timeline.stop();
        }
        typingField.setDisable(true);

        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000; // เวลาที่ใช้จริง
        if (elapsedTime == 0)
            elapsedTime = 1; // ป้องกันหารด้วยศูนย์

        int wpm = (int) ((correctWords * 60) / elapsedTime); // คำนวณ WPM อย่างแม่นยำ

        Platform.runLater(() -> {
            resultLabel.setText("Results: " + wpm + " WPM");
            sampleText.setText("Test Finished!");
        });
    }
}
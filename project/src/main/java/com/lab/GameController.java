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

import java.io.*;
import java.util.*;

public class GameController {

    @FXML private Label sampleText;       // ข้อความตัวอย่างให้ผู้ใช้พิมพ์
    @FXML private Label timerLabel;       // ตัวจับเวลา
    @FXML private Label resultLabel;      // แสดงผลคะแนน
    @FXML private TextField typingField;  // ช่องให้พิมพ์
    @FXML private Button startButton;     // ปุ่มเริ่ม/รีสตาร์ท


    private static final String BASE_CSV = "text.csv";  // ✅ เพิ่มชื่อไฟล์ base

    
    public static final String USER_CSV = "text_user.csv";

    private String currentText;       // ข้อความปัจจุบันที่กำลังพิมพ์
    private int timeLeft = 60;        // เวลาเริ่มต้น (60 วินาที)
    private Timeline timeline;        // ตัวจับเวลาแบบ real-time
    private boolean testStarted = false; // เช็คว่าเกมเริ่มหรือยัง
    private long startTime;           // เวลาที่เริ่มพิมพ์
    private int correctWords = 0;     // คำที่พิมพ์ถูกต้อง
    private int totalTypedWords = 0;  // คำที่พิมพ์ทั้งหมด
    private int mistakeCount = 0;     // จำนวนครั้งที่พิมพ์ผิด
    private final Random random = new Random(); // สำหรับสุ่มข้อความ
            // ใส่ด้านบนของคลาส
        private int lastWpm = 0;
        private int lastScore = 0;



    // ✅ เมื่อผู้ใช้กด Start (หรือ Restart) ให้เริ่มเกม
    @FXML
    private void startTest() {


        if (!testStarted) {
            testStarted = true;
            correctWords = 0;
            totalTypedWords = 0;
            mistakeCount = 0;
            startTime = System.currentTimeMillis();  // บันทึกเวลาที่เริ่ม
            nextSentence();                          // แสดงประโยคแรก
            typingField.setDisable(false);
            typingField.setText("");
            typingField.requestFocus();              // โฟกัสช่องพิมพ์
            timeLeft = 60;
            timerLabel.setText("Time: " + timeLeft + "s");
            resultLabel.setText("");

            typingField.setOnKeyReleased(e -> checkCompletion()); // ตรวจทุกครั้งที่ปล่อยปุ่ม

            if (timeline != null) {
                timeline.stop();
            }

            // ✅ ตั้งตัวจับเวลา ถอยหลังทีละ 1 วินาที
            timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                timeLeft = 60 - (int) ((System.currentTimeMillis() - startTime) / 1000);

                Platform.runLater(() -> timerLabel.setText("Time: " + timeLeft + "s"));
                if (timeLeft <= 0) {
                    endTest(); // หมดเวลา
                }
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
            startButton.setText("Restart");
        } else {
            restartTest();
        }
    }

    // ✅ ถ้ากด Start ใหม่ = รีสตาร์ทเกม
    private void restartTest() {
        testStarted = false;
        if (timeline != null) timeline.stop();
        startTest(); // เรียกใหม่
    }

    // ✅ แสดงประโยคใหม่แบบสุ่ม
    private void nextSentence() {
        List<String> texts = new ArrayList<>();
        loadFromCsv(BASE_CSV, texts);  // ✅ โหลดจาก resources
        loadFromCsv(USER_CSV, texts);  // ✅ โหลดจาก text_user.csv (แก้ไขได้)
    
        if (texts.isEmpty()) {
            sampleText.setText("No text available.");
            return;
        }
    
        currentText = texts.get(new Random().nextInt(texts.size()));
        sampleText.setText(currentText);
        typingField.setText("");
    
        System.out.println("📄 Loaded texts:");
        for (String text : texts) {
            System.out.println("👉 " + text);
        }
    }
    

    private void loadFromCsv(String path, List<String> list) {
        try {
            InputStream input;
            if (path.equals(BASE_CSV)) {
                input = getClass().getResourceAsStream("/" + path); // ✅ โหลดจาก resources
                if (input == null) {
                    System.out.println("🚫 BASE_CSV not found in resources: " + path);
                    return;
                }
            } else {
                input = new FileInputStream(path); // ✅ โหลดจาก text_user.csv
            }
    
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line.trim());
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("⚠️ Error reading file: " + path);
            e.printStackTrace();
        }
    }
    
    
    
    
    
    
    // ✅ ตรวจว่าผู้ใช้พิมพ์ครบหรือยัง
    private void checkCompletion() {
        String typedText = typingField.getText().trim();

        if (!currentText.startsWith(typedText)) {
            mistakeCount++; // ถ้าเริ่มต้นไม่ตรง ถือว่าผิด
        }

        if (typedText.equals(currentText)) {
            String[] words = typedText.split("\\s+"); // นับจำนวนคำ
            correctWords += words.length;
            totalTypedWords += words.length;
            nextSentence(); // ไปคำถัดไป
        }
    }

    // ✅ สิ้นสุดการทดสอบ → คำนวณคะแนน
    private void endTest() {
        if (timeline != null) timeline.stop();
        typingField.setDisable(true);

        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        if (elapsedTime == 0) elapsedTime = 1;

        int wpm = (int) ((correctWords * 60) / elapsedTime);
        int total = totalTypedWords == 0 ? correctWords : totalTypedWords;
        double baseScore = ((double) correctWords / total) * 100;
        double penalty = Math.min(mistakeCount * 5, 50);
        int finalScore = (int) Math.max(0, baseScore - penalty);

        lastWpm = wpm;
        lastScore = finalScore;

        Platform.runLater(() -> {
            resultLabel.setText("Results: " + wpm + " WPM | Score: " + finalScore + "%");
            sampleText.setText("Test Finished!");
        });
    }

    // ✅ เมื่อกด ENTER → ไปยังประโยคถัดไป
    @FXML
    private void initialize() {
        typingField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                nextSentence();
            }
        });
    }

    // ✅ เปิดหน้าจอ ManageTextsController
    @FXML
    private void openManageTexts(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manage_texts.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Manage Sample Texts");
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void showResults() {
    resultLabel.setText("Last Results: " + lastWpm + " WPM | Score: " + lastScore + "%");
}

@FXML
private void switchToManageTexts(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/manage_texts.fxml"));
        Parent root = loader.load();

        // ✅ ดึง stage เดิมจากปุ่มที่กด
        Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();

        currentStage.setScene(new Scene(root));             // ✅ เปลี่ยน scene ใหม่ในหน้าต่างเดิม
        currentStage.setTitle("Manage Sample Texts");       // ตั้งชื่อใหม่ (optional)
        currentStage.show();                                // แสดงผล
    } catch (IOException e) {
        e.printStackTrace();
    }
}
private String getResourcePath(String filename) {
    return Objects.requireNonNull(getClass().getResource("/" + filename)).getPath();
}


    }

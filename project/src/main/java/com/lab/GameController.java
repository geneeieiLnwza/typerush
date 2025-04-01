package com.lab;

import javafx.animation.KeyFrame; // กำหนดการทำงานแต่ละช่วงเวลา
import javafx.animation.ScaleTransition; // ทำ animation
import javafx.animation.Timeline;  // สร้างanimation แบบมีช่วงเวลา
import javafx.animation.TranslateTransition; // ทำ animation เลื่อนตำแหน่ง
import javafx.application.Platform;  // สั่งให้โค้ดรันบน javafx ui thread
import javafx.event.ActionEvent; // จัดการ event ที่เกิดจากการกดปุ่ม
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;  // Layout แบบตาราง
import javafx.stage.Stage;
import javafx.util.Duration;  // กำหนดระยะเวลา
import javafx.scene.text.Text;  // สร้างตัวอักษรทีละตัว
import javafx.scene.text.TextFlow;  // จัดกลุ่มตัวอักษรเพื่อแสดงประโยค

import java.io.*; // สำหรับ InputStream, FileReader, BufferedReader, etc.
import java.util.*; // สำหรับ List, ArrayList, Random, Objects, etc.

public class GameController {

    @FXML
    private Label timerLabel; // แสดงเวลา
    @FXML
    private TextArea typingField; // ช่องให้พิมพ์ข้อความ
    @FXML
    private Button startButton; // ปุ่มเริ่ม/รีสตาร์ทเกม
    @FXML
    private Label resultLabelwpm; // แสดง WPM (คำต่อนาที)
    @FXML
    private Label resultLabelaccuracy; // แสดงคะแนนความแม่นยำ
    @FXML
    private TextFlow sampleTextFlow; // แสดงข้อความตัวอย่างทีละตัว

    private static final String BASE_CSV = "text.csv"; // ชื่อไฟล์ข้อความพื้นฐาน
    public static final String USER_CSV = "text_user.csv"; // ชื่อไฟล์ข้อความที่ผู้ใช้เพิ่ม

    private String currentText; // ข้อความที่กำลังใช้ให้พิมพ์
    private int timeLeft = 60; // เวลาเริ่มต้น 60 วินาที
    private Timeline timeline; // สำหรับจับเวลาแบบ real-time
    private boolean testStarted = false; // เช็คว่าเริ่มเกมแล้วหรือยัง
    private long startTime; // เวลาเริ่มต้นของเกม 
    private int correctWords = 0; // จำนวนคำที่พิมพ์ถูก
    private int totalTypedWords = 0; // จำนวนคำที่พิมพ์ทั้งหมด
    private int mistakeCount = 0; // นับจำนวนคำผิด
    private int lastWpm = 0; // ผลลัพธ์ WPM ล่าสุด
    private int lastScore = 0; // คะแนนล่าสุด

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
        if (!testStarted) { // ถ้ายังไม่เริ่มเกม
            testStarted = true; // เริ่มเกม เพื่อไม่ให้เข้าเงื่อนไขนี้ซ้ำ
            correctWords = 0; // รีเซ็ตจำนวนคำที่พิมพ์ถูกต้อง
            totalTypedWords = 0; // รีเซ็ตจำนวนคำที่พิมพ์ทั้งหมด
            mistakeCount = 0; // รีเซ็ตจำนวนคำผิด
            startTime = System.currentTimeMillis(); // บันทึกเวลาที่เริ่มเล่นเกม 
            nextSentence(); // โหลดประโยคแรก
            typingField.setDisable(false); // เปิดให้พิมพ์ใน TextArea ได้
            typingField.setText("");       // เคลียร์ข้อความใน TextArea
            typingField.setPrefHeight(40); // รีเซ็ตความสูงของ TextArea

            typingField.requestFocus(); // โฟกัสไปที่ช่องพิมพ์
            timeLeft = 60; // นับถอยหลังเป็น 60 วินาที
            timerLabel.setText("Time: " + timeLeft + "s");  // แสดงเวลาเริ่มต้นบนหน้าจอ
            resultLabelwpm.setText(""); // เคลียร์ข้อความผลลัพธ์ WPM ก่อนเริ่ม
            resultLabelaccuracy.setText(""); // เคลียร์คะแนนความแม่นยำก่อนเริ่ม

            typingField.setOnKeyReleased(e -> checkCompletion()); // ตั้งให้เมื่อปล่อยปุ่ม จะตรวจข้อความ

            if (timeline != null) {
                timeline.stop(); // ถ้ามี timer ตัวเก่าอยู่ ให้หยุดก่อน
            }

            timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> { // สร้าง timeline นับถอยหลัง ทำงานทุก ๆ 1 วินาที
                long elapsed = (System.currentTimeMillis() - startTime) / 1000; // คำนวณเวลาที่ผ่านไปเป็นวินาที
                timeLeft = 60 - (int) elapsed; // ลบเวลาออกจาก 60 เพื่อให้เหลือเวลา

                Platform.runLater(() -> {
                    timerLabel.setText("Time: " + timeLeft + "s"); // อัปเดตข้อความเวลาใน ui
                    updateResultsRealtime(); // คำนวณ WPM/Accuracy แบบเรียลไทม์
                });

                if (timeLeft <= 0) {
                    endTest(); // เวลาหมด เจบเกม

                }
            }));

            
    timeline.setCycleCount(Timeline.INDEFINITE); // ให้ timeline ทำงานต่อไปเรื่อย ๆ 
    timeline.play(); // เริ่มนับเวลา

    startButton.setText("Restart"); // เปลี่ยนข้อความปุ่มเป็น “Restart” สำหรับเล่นใหม่
        } else {
            restartTest(); // ถ้าเกมเริ่มอยู่แล้ว ให้เรียกเมธอดรีสตาร์ทเกม
        }
    }

    private void restartTest() { // เมธอดรีสตาร์ทเกมใหม่ 
    testStarted = false; // รีเซ็ตสถานะว่าเกมยังไม่เริ่ม 
    if (timeline != null) // ถ้ามี timeline ที่กำลังทำงานอยู่
        timeline.stop();  // หยุดการนับเวลาทันที
    startTest(); // เรียกเมธอด startTest() เพื่อเริ่มเกมใหม่ทั้งหมด
    }

    private void nextSentence() { // เมธอดนี้ทำหน้าที่โหลดประโยคใหม่มาให้พิมพ์
        List<String> texts = new ArrayList<>(); // สร้าง list ว่างไว้เก็บข้อความทั้งหมด
    
        loadFromCsv(BASE_CSV, texts); // โหลดข้อความจากไฟล์ text.csv 
        loadFromCsv(USER_CSV, texts); // โหลดข้อความจากไฟล์ text_user.csv 
        if (texts.isEmpty()) { // ถ้าไม่มีข้อความใด ๆ เลยในทั้ง 2 ไฟล์
            sampleTextFlow.getChildren().setAll(new Text("No text available."));   // แสดงข้อความแจ้งเตือนใน TextFlow ว่าไม่มีข้อความให้พิมพ์
            return; // ออกจากเมธอดทันที
        }
    
        // เลือกข้อความจาก list แบบสุ่มมา 1 ประโยค และเก็บใน currentText
        currentText = texts.get(new Random().nextInt(texts.size()));
    
        // เตรียม TextFlow ให้แสดงตัวอักษรทีละตัว (เพื่อใช้เปลี่ยนสีแต่ละตัวได้)
        sampleTextFlow.getChildren().clear(); // ล้างข้อความเดิมที่เคยแสดงออกก่อน
        for (char c : currentText.toCharArray()) { // วนลูปทุกตัวอักษรในประโยค
            Text t = new Text(String.valueOf(c)); // สร้าง object Text ทีละตัวจากอักษร
    
            t.setStyle("-fx-fill: white; -fx-font-size: 18px;");
            // ตั้งค่าสีและขนาดของตัวอักษร
            sampleTextFlow.getChildren().add(t); // เพิ่ม Text แต่ละตัวลงใน TextFlow
        }
        typingField.setText(""); // ล้างช่องพิมพ์ให้พร้อมเริ่มพิมพ์ประโยคใหม่
    
        // แสดงข้อความใน console ว่ามีข้อความอะไรบ้าง
        System.out.println("\uD83D\uDCC4 Loaded texts:"); // แสดงหัวข้อ
        for (String text : texts) { // วนลูปข้อความทั้งหมดที่โหลดมา
            System.out.println("\uD83D\uDC49 " + text); // แสดงทีละข้อความพร้อม emoji ชี้
        }
    }

    private void loadFromCsv(String path, List<String> list) { 
        // เมธอดนี้ใช้โหลดไฟล์ข้อความ (.csv) จากพาธที่ระบุ แล้วเพิ่มแต่ละบรรทัดลงใน list ที่ส่งเข้ามา
    
        try {
            InputStream input; // สร้างตัวแปร input สำหรับอ่านข้อมูลจากไฟล์
            if (path.equals(BASE_CSV)) {
                // ถ้าพาธที่ส่งเข้ามาเป็นไฟล์ BASE_CSV (text.csv) ซึ่งอยู่ใน resource
                input = getClass().getResourceAsStream("/" + path); 
                // ใช้ getResourceAsStream โหลดไฟล์จาก resources 
            } else {
                // ถ้าเป็นไฟล์ภายนอก 
                input = new FileInputStream(path); 
                // ใช้ FileInputStream อ่านจากไฟล์ที่อยู่ในเครื่องโดยตรง
            }
    
            // ครอบ InputStream ด้วย InputStreamReader และ BufferedReader เพื่อให้สามารถอ่านทีละบรรทัดได้
            BufferedReader reader = new BufferedReader(new InputStreamReader(input)); 
            String line; // สร้างตัวแปรสำหรับเก็บข้อความแต่ละบรรทัดที่อ่านได้
            while ((line = reader.readLine()) != null) {
                // วนลูปอ่านไฟล์ทีละบรรทัดจนกว่าจะเจอ null 
                list.add(line.trim()); 
                // ตัดช่องว่างหัว-ท้ายออก เพิ่มข้อความลงใน list ที่ส่งเข้ามา
            }
            reader.close(); // ปิด reader เพื่อคืนทรัพยากร
        } catch (IOException | NullPointerException e) {
            // ถ้าเกิดข้อผิดพลาดในการอ่านไฟล์ resource ไม่เจอ 
            System.out.println("⚠️ Error reading file: " + path); 
            // แสดงข้อความเตือนใน console พร้อมชื่อไฟล์ที่อ่านไม่ได้
        }
    }
    
    private void checkCompletion() {
        // เมธอดนี้จะตรวจว่า user พิมพ์ถูกหรือไม่ และเปลี่ยนสีตัวอักษรที่แสดงให้ตรงกับผล
        String typedText = typingField.getText(); // ดึงข้อความที่ผู้ใช้พิมพ์ใน TextArea มาทั้งหมด
        for (int i = 0; i < sampleTextFlow.getChildren().size(); i++) {
            // วนลูปทุกตัวอักษรที่แสดงอยู่ใน TextFlow (คือตัวอักษรของ currentText)
    
            Text letter = (Text) sampleTextFlow.getChildren().get(i);
            // ดึงตัวอักษรที่ตำแหน่ง i จาก TextFlow (เป็นชนิด Text)
    
            if (i < typedText.length()) {
                // ถ้ายังมีตัวอักษรที่ผู้ใช้พิมพ์อยู่ในตำแหน่งนี้
    
                char typedChar = typedText.charAt(i);       // ตัวอักษรที่ผู้ใช้พิมพ์
                char expectedChar = currentText.charAt(i);  // ตัวอักษรที่ควรจะพิมพ์ในตำแหน่งนี้
    
                if (typedChar == expectedChar) {
                    // ถ้าผู้ใช้พิมพ์ตรงกับที่ควรจะเป็น
                    letter.setStyle("-fx-fill: #00ff00; -fx-font-size: 18px;"); // ตั้งสีตัวอักษรเป็นเขียว 
                } else {
                    // ถ้าพิมพ์ผิด
                    letter.setStyle("-fx-fill: #ff4444; -fx-font-size: 18px;"); // ตั้งสีตัวอักษรเป็นแดง 
                    shakeNode(typingField); // เรียกเมธอดสั่นช่องพิมพ์ เพื่อแสดง feedback ว่าพิมพ์ผิด
                }
            } else {
                // ถ้าผู้ใช้ยังไม่ได้พิมพ์ถึงตัวอักษรนี้
                letter.setStyle("-fx-fill: white; -fx-font-size: 18px;");
                // ตั้งค่าสี
        }
        if (typedText.equals(currentText)) {
            // ถ้าข้อความที่พิมพ์ตรงกับ currentText แบบเป๊ะทุกตัวอักษร
            String[] words = typedText.split("\\s+"); // แยกข้อความที่พิมพ์ออกเป็นคำ โดยแบ่งด้วยช่องว่าง
            correctWords += words.length; // เพิ่มจำนวนคำที่พิมพ์ถูกต้อง
            totalTypedWords += words.length; // เพิ่มจำนวนคำทั้งหมดที่พิมพ์
            nextSentence(); // ไปยังประโยคถัดไปให้ผู้ใช้พิมพ์
            }
        }
    }
    

    private void endTest() {
        if (timeline != null)
            timeline.stop(); // หยุดนาฬิกาจับเวลา ถ้ายังทำงานอยู่
        typingField.setDisable(true); // ปิดไม่ให้พิมพ์อะไรเพิ่มได้อีก
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000; // คำนวณเวลาที่ผ่านไปทั้งหมดหน่วยเป็นวินาที 
       
        if (elapsedTime == 0)
            elapsedTime = 1; // ป้องกันการหารด้วยศูนย์ ถ้าเกมจบเร็วเกินไป
        int wpm = (int) ((correctWords * 60) / elapsedTime); // คำนวณคำต่อนาที (WPM) = (คำที่พิมพ์ถูก * 60) / เวลาที่ใช้จริง
        int total = totalTypedWords == 0 ? correctWords : totalTypedWords; // ถ้ายังไม่มีคำที่พิมพ์ทั้งหมด ให้ใช้ correctWords แทน
        double baseScore = ((double) correctWords / total) * 100; // คำนวณเปอร์เซ็นต์ความแม่นยำจากคำที่พิมพ์ถูกทั้งหมด
        double penalty = Math.min(mistakeCount * 5, 50); // ลงโทษคะแนนจากจำนวนคำผิด: คำผิดละ 5 คะแนน 
        int finalScore = (int) Math.max(0, baseScore - penalty);// คะแนนสุดท้าย = ความแม่นยำ - โทษ 
        lastWpm = wpm; // เก็บค่า WPM ไว้ใช้ตอนแสดงย้อนหลัง
        lastScore = finalScore; // เก็บคะแนนไว้แสดงย้อนหลัง
    
        resultLabelwpm.setText("Results: " + wpm + " WPM"); // แสดง WPM
        resultLabelaccuracy.setText("Score: " + finalScore + "%");  // แสดงคะแนนรวมเป็นเปอร์เซ็นต์
    }

    private void updateResultsRealtime() {
        long elapsedMillis = System.currentTimeMillis() - startTime; // เวลาที่ผ่านไปตั้งแต่เริ่มเกม (หน่วยมิลลิวินาที)
        double elapsedMinutes = elapsedMillis / 60000.0; // แปลงเป็นนาที 
        String typed = typingField.getText(); // ดึงข้อความที่ผู้ใช้พิมพ์ในขณะนั้น
        int correctChars = countCorrectCharacters(currentText, typed);// นับจำนวนตัวอักษรที่พิมพ์ถูกต้อง 
        int totalTyped = typed.length();// ความยาวของข้อความที่ผู้ใช้พิมพ์ 
        int wpm = elapsedMinutes > 0 ? (int) ((correctChars / 5.0) / elapsedMinutes) : 0;// คำนวณ WPM จากจำนวน character ถูกต้อง 
        double accuracy = totalTyped == 0 ? 100.0 : ((double) correctChars / totalTyped) * 100;// คำนวณความแม่นยำ = ตัวที่ถูก / ทั้งหมด * 100 
        resultLabelwpm.setText(String.format("Live: %d WPM", wpm));// แสดง WPM แบบ Real-Time
        resultLabelaccuracy.setText(String.format("Accuracy: %.1f%%", accuracy));// แสดงความแม่นยำแบบ Real-Time
    }

    private int countCorrectCharacters(String expected, String actual) {
        int correct = 0; // ตัวแปรนับจำนวนตัวอักษรที่พิมพ์ถูก
        int len = Math.min(expected.length(), actual.length());// ใช้ความยาวที่สั้นกว่าระหว่าง expected กับ actual เพื่อไม่ให้เกิน index
        for (int i = 0; i < len; i++) {
            if (expected.charAt(i) == actual.charAt(i)) {
                // ถ้าตัวอักษรที่พิมพ์ตรงกับที่ควรจะเป็น
                correct++; // เพิ่มตัวนับ
            }
        }
        return correct; // คืนค่าจำนวนตัวอักษรถูกต้อง
    }

    @FXML
    private GridPane root; // ← ต้องผูกจาก FXML ด้วย

    @FXML
    private void initialize() {
    // ตั้ง event ว่าเมื่อผู้ใช้กดปุ่ม Enter ให้เปลี่ยนไปยังประโยคถัดไป
    typingField.setOnKeyPressed(event -> {
        if (event.getCode().toString().equals("ENTER")) {
            nextSentence(); // โหลดประโยคใหม่
        }
    });

    // ตั้งให้ TextArea ขยายความสูงอัตโนมัติตามจำนวนข้อความที่พิมพ์
    typingField.textProperty().addListener((obs, oldText, newText) -> {
        double height = computeTextAreaHeight(typingField); // คำนวณความสูงใหม่
        typingField.setPrefHeight(height); // ตั้งความสูงให้ TextArea ใหม่ตามเนื้อหา
    });

    // Responsive: ปรับขนาดตัวอักษรตามความกว้างของหน้าจอ
    root.widthProperty().addListener((obs, oldVal, newVal) -> {
        double width = newVal.doubleValue(); // ความกว้างใหม่ของหน้าต่าง

        double baseFontSize = width / 60; // ขนาดฟอนต์ปกติ
        double bigFontSize = width / 80;  // ขนาดฟอนต์ที่ใช้กับผลลัพธ์ / timer

        // ปรับฟอนต์แต่ละส่วนให้สอดคล้องกับขนาดหน้าจอ
        resultLabelwpm.setStyle("-fx-font-size: " + bigFontSize + "px;");
        resultLabelaccuracy.setStyle("-fx-font-size: " + bigFontSize + "px;");
        timerLabel.setStyle("-fx-font-size: " + bigFontSize + "px;");
        typingField.setStyle("-fx-font-size: " + baseFontSize + "px;");
        startButton.setStyle("-fx-font-size: " + baseFontSize + "px;");
    });

    startButton.setCursor(javafx.scene.Cursor.HAND); // ตั้งให้เมาส์กลายเป็นรูปมือเมื่อชี้ที่ปุ่ม
}

    @FXML
    private void showResults() {
        resultLabelwpm.setText("Last Results: " + lastWpm + " WPM");  // แสดง WPM ล่าสุดที่เก็บไว้ตอนจบเกม
        resultLabelaccuracy.setText("Score: " + lastScore + "%"); // แสดงคะแนนความแม่นยำล่าสุด
    }

    @FXML
    private void switchToManageTexts(ActionEvent event) {
        try {
            // โหลด FXML ของหน้าจัดการข้อความ
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manage_texts.fxml"));
            Parent root = loader.load(); // โหลด root node จาก FXML
    
            // ดึง Stage (หน้าต่าง) ปัจจุบันจากปุ่มที่กด
            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    
            // เปลี่ยน scene ปัจจุบันให้แสดงหน้าจอจัดการข้อความ
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Manage Sample Texts"); // ตั้งชื่อหน้าต่าง
            currentStage.setMaximized(true); // ขยายให้เต็มจอ
            currentStage.show(); // แสดงหน้าจอใหม่
        } catch (IOException e) {
            e.printStackTrace(); // ถ้าโหลด FXML ไม่ได้ให้แสดง error
        }
    }

    private String getResourcePath(String filename) {
        // คืน path ของไฟล์ resource ที่อยู่ใน /resources โดยเช็คว่าไม่เป็น null
        return Objects.requireNonNull(getClass().getResource("/" + filename)).getPath();
    }
    
    private double computeTextAreaHeight(TextArea textArea) {
        Text text = new Text(textArea.getText()); // สร้าง Text object จากข้อความที่พิมพ์
        text.setFont(textArea.getFont());         // ใช้ฟอนต์เดียวกันกับ TextArea
        text.setWrappingWidth(textArea.getWidth() - 20); // ตั้งความกว้างให้เท่ากับ TextArea 
    
        return text.getLayoutBounds().getHeight() + 30; // คืนค่าความสูงจริงของข้อความ + เพิ่มอีก 30px เผื่อ padding
    }

    private void shakeNode(javafx.scene.Node node) {
        // สร้าง animation แบบเลื่อนตำแหน่งซ้ายขวาสลับไปมา
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);      // เริ่มจากตำแหน่ง X เดิม
        tt.setByX(10);       // เลื่อนออกไปทางขวา 10 พิกเซล
        tt.setCycleCount(4); // ทำทั้งหมด 4 รอบ
        tt.setAutoReverse(true); // สลับกลับซ้าย-ขวาไปมา
        tt.play();           // เริ่มเล่น animation
    }

}

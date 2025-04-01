package com.lab;

import javafx.application.Application; // สร้าง javafx
import javafx.fxml.FXMLLoader; //โหลดไฟล์ fxml
import javafx.scene.Parent; // แทน root node ของ ui
import javafx.scene.Scene; //กำหนดฉาก javafx
import javafx.stage.Stage; //หน้าต่าง

import java.util.Objects; // ตรวจ null ตอนโหลด resนurce

public class TypingApp extends Application {
// extends Application เพราะเป็นจุดเริ่มต้นของ javafx จะเรียกเมธอด start(Stage primaryStage) เมื่อโปรแกรมเริ่มทำงาน
    
@Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/game.fxml"))); // โหลด layout จาก game.fxml
        Parent root = loader.load(); // โหลดโครงสร้าง ui

        Scene scene = new Scene(root); // สร้าง Scene และใส่ stylesheet
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()); //ใส่ไฟล์ style.css เพื่อปรับแต่ง ui

        // ตั้งชื่อหน้าต่าง แสดงแบบเต็มจอ
        primaryStage.setTitle("Typing Speed Test"); // หัวข้อ 
        primaryStage.setScene(scene); //กำหนดฉาก ให้หน้าต่างหลักของแอป
        primaryStage.setMaximized(true); // เปิดเต็มจอตอนเริ่ม
        primaryStage.show(); // แสดงหน้าจอ
    }

    public static void main(String[] args) {
        launch(args); 
    }
}

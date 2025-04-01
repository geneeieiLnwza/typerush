package com.lab;

import javafx.fxml.FXML; //ผูก controller กับ fxml
import javafx.fxml.FXMLLoader; //โหลด fxml
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;  // TextField, Button, ListView
import javafx.stage.Stage;

// ใช้งาน FileReader, Writer
import java.io.BufferedReader; // อ่านไฟล์แบบมี buffer
import java.io.BufferedWriter; // เขียนไฟล์แบบมี buffer
import java.io.FileReader; // อ่านไฟล์จากระบบไฟล์แบบ character stream
import java.io.FileWriter; // เขียนไฟล์ไปยังระบบไฟล์แบบcharacter stream
import java.io.IOException; // เอาไว้จัดการข้อผิดพลาด

public class ManageTextsController {

    @FXML private TextField textInputField; // ช่องกรอกข้อความใหม่
    @FXML private ListView<String> textListView;   // แสดงข้อความที่เพิ่งเพิ่มเข้ามาในรอบนี้

    private final String userCsvPath = "text_user.csv"; // เปลี่ยนจาก CSV_PATH

    // เมธอดนี้จะถูกเรียกเมื่อเปิดหน้าจอ Manage Texts 
    @FXML
    private void initialize() { 
        // เมธอดพิเศษของ JavaFX ที่จะถูกเรียกเมื่อโหลด FXML เสร็จ
       
        textListView.getItems().clear(); // ล้างข้อความทั้งหมดใน ListView เผื่อมีของเก่าอยู่
        try (BufferedReader reader = new BufferedReader(new FileReader(userCsvPath))) {
            // สร้าง BufferedReader เพื่ออ่านไฟล์ text_user.csv ทีละบรรทัด

            String line; // ตัวแปรเก็บแต่ละบรรทัดที่อ่านได้
            while ((line = reader.readLine()) != null) { // วนลูปอ่านไฟล์ทีละบรรทัด readLine คืนค่า null
                textListView.getItems().add(line.trim()); // ตัดช่องว่างหัวท้ายเพิ่มเข้า ListView
            }
        } catch (IOException e) {   // หากเกิดข้อผิดพลาดตอนอ่านไฟล์ 
            e.printStackTrace();  // แสดงรายละเอียดข้อผิดพลาดใน console
        }
    }
    
    // เมื่อกดปุ่ม "Add" จะเพิ่มคำใหม่เข้า list และเซฟ
    @FXML
    private void onAddText() {
        String newText = textInputField.getText().trim(); // ตัดช่องว่าง
        if (!newText.isEmpty()) {
            textListView.getItems().add(newText); // add คำใหม่
            textInputField.clear(); // เคลียร์ช่องกรอก
            saveSampleTexts(); // บันทึกลง text.csv
        }
    }

    // เมื่อเลือกคำจาก ListView แล้วแก้ไข
    @FXML
    private void onEditText() {
        int selectedIndex = textListView.getSelectionModel().getSelectedIndex();  // ดึง index ของข้อความที่ถูกเลือกใน ListView
        if (selectedIndex >= 0) { // ถ้ามีการเลือกข้อความอยู่จริงIndex >= 0
            String updatedText = textInputField.getText().trim(); // ดึงข้อความใหม่จากช่องกรอก ตัดช่องว่างหัว ท้ายออก
            if (!updatedText.isEmpty()) { // ถ้าข้อความใหม่ไม่ว่างเปล่า
                textListView.getItems().set(selectedIndex, updatedText); // แก้ที่แสดง
                textInputField.clear(); // เคลียร์ข้อความในช่องออกหลังจากแก้เสร็จ
                saveSampleTexts(); // เซฟใหม่ทั้งไฟล์
            }
        }
    }

    // ลบข้อความจาก list และเซฟใหม่ทั้งไฟล์
    @FXML
    private void onDeleteText() { // ดึงตำแหน่งของรายการ (index) ที่ผู้ใช้เลือกใน ListView
        int selectedIndex = textListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {  
            textListView.getItems().remove(selectedIndex);    // ลบข้อความใน ListView ที่ตำแหน่งที่เลือก          // ลบจากที่แสดง
            saveSampleTexts(); // เซฟใหม่ทั้งไฟล์
        }
    }

    // ปิดหน้าแล้วกลับไปหน้าเกม
    @FXML
    private void switchToGameScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game.fxml")); // โหลดไฟล์ game.fxml
            Parent root = loader.load();   // สร้าง root node ของ ui จาก fxml
            Stage stage = (Stage) textListView.getScene().getWindow(); // ดึงหน้าต่างปัจจุบันที่แสดง ListView อยู่
            stage.setScene(new Scene(root)); // สร้าง Scene ใหม่จาก root ที่โหลดมา แล้วตั้งให้กับ Stage
            stage.setTitle("Typing Speed Test");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // ถ้าเกิดปัญหาแสดง error ใน console
        }
    }

    // บันทึกข้อความแล้วปิดหน้าต่าง
    @FXML
    private void onSaveAndClose() {
        saveSampleTexts();  // บันทึกก่อนปิด
        ((Stage) textListView.getScene().getWindow()).close(); // ปิดหน้าต่าง
    }

    // เซฟข้อความทั้งหมดลง text_user.csv
    private void saveSampleTexts() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userCsvPath))) {
            // สร้าง BufferedWriter เพื่อเขียนไฟล์แบบบรรทัดต่อบรรทัด ใช้ try-with-resources เพื่อให้ปิดไฟล์อัตโนมัติ
            
            for (String text : textListView.getItems()) {  // วนลูปข้อความทั้งหมดใน ListView
                writer.write(text); // เขียนข้อความ
                writer.newLine(); // ขึ้นบรรทัดใหม่
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
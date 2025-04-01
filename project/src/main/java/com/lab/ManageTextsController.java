package com.lab;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ManageTextsController {

    @FXML private TextField textInputField;        // ช่องกรอกข้อความใหม่
    @FXML private ListView<String> textListView;   // แสดงข้อความที่เพิ่งเพิ่มเข้ามาในรอบนี้

    private final String userCsvPath = "text_user.csv"; // เปลี่ยนจาก CSV_PATH


    // เมธอดนี้จะถูกเรียกเมื่อเปิดหน้าจอ Manage Texts
    
    @FXML
    private void initialize() {
        textListView.getItems().clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(userCsvPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                textListView.getItems().add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    


    // เมื่อกดปุ่ม "Add" จะเพิ่มคำใหม่เข้า list และเซฟ
    @FXML
    private void onAddText() {
        String newText = textInputField.getText().trim();   // ตัดช่องว่าง
        if (!newText.isEmpty()) {
            textListView.getItems().add(newText);
            textInputField.clear();                          // เคลียร์ช่องกรอก
            saveSampleTexts();                               // บันทึกลง text.csv
        }
    }

    // เมื่อเลือกคำจาก ListView แล้วแก้ไข
    @FXML
    private void onEditText() {
        int selectedIndex = textListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            String updatedText = textInputField.getText().trim();
            if (!updatedText.isEmpty()) {
                textListView.getItems().set(selectedIndex, updatedText);         // แก้ที่แสดง
                textInputField.clear();
                saveSampleTexts();   // เซฟใหม่ทั้งไฟล์
            }
        }
    }

    // เมื่อลบข้อความจาก ListView
    @FXML
    private void onDeleteText() {
        int selectedIndex = textListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {  
            textListView.getItems().remove(selectedIndex);           // ลบจากที่แสดง
            saveSampleTexts();  // เซฟใหม่ทั้งไฟล์
        }
    }

    // ปิดหน้าแล้วกลับไปหน้าเกม
    @FXML
    private void switchToGameScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) textListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Typing Speed Test");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ปิดหน้าจอพร้อมบันทึกข้อมูลล่าสุด
    @FXML
    private void onSaveAndClose() {
        saveSampleTexts();  // บันทึกก่อนปิด
        ((Stage) textListView.getScene().getWindow()).close();
    }

    private void saveSampleTexts() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userCsvPath))) {
            for (String text : textListView.getItems()) {
                writer.write(text);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    



}
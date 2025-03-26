package com.lab;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ManageTextsController {
    @FXML
    private TextField textInputField;
    @FXML
    private ListView<String> textListView;

    @FXML
    private void initialize() {
        textListView.getItems().clear(); //ทำให้ ListView เริ่มต้นว่างเปล่า
    }

    @FXML
    private void onAddText() {
        String newText = textInputField.getText().trim();
        if (!newText.isEmpty()) {
            GameController.SAMPLE_TEXTS.add(newText);
            textListView.getItems().add(newText); //เพิ่มเฉพาะคำที่พิมพ์ใหม่เข้าไป
            textInputField.clear();
            saveSampleTexts(); //บันทึกข้อความลงไฟล์ CSV
        }
    }
    

    @FXML
    private void onEditText() {
        int selectedIndex = textListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            String updatedText = textInputField.getText().trim();
            if (!updatedText.isEmpty()) {
                GameController.SAMPLE_TEXTS.set(selectedIndex, updatedText);
                textListView.getItems().set(selectedIndex, updatedText);
                textInputField.clear();
                saveSampleTexts(); //บันทึก CSV หลังแก้ไขข้อความ
            }
        }
    }

    @FXML
    private void onDeleteText() {
        int selectedIndex = textListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            GameController.SAMPLE_TEXTS.remove(selectedIndex);
            textListView.getItems().remove(selectedIndex);
            saveSampleTexts(); //บันทึก CSV หลังลบข้อความ
        }
    }

    @FXML
    private void switchToGameScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game.fxml")); // ตรวจสอบว่า path ถูกต้อง
            Parent root = loader.load();

            Stage stage = (Stage) textListView.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Typing Speed Test");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSaveAndClose() {
        saveSampleTexts(); //บันทึก CSV ก่อนปิดหน้าต่าง
        ((Stage) textListView.getScene().getWindow()).close();
    }

    //เพิ่มเมธอดบันทึกข้อความลง CSV
    private void saveSampleTexts() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("text.csv"))) {
            for (String text : GameController.SAMPLE_TEXTS) {
                writer.write(text);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
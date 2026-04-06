package com.compass.demo1;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class ActivityDetailController {
    @FXML private Label nameLabel;
    @FXML private Label descriptionLabel;
    @FXML private ImageView profileImage;

    // Kapatma butonu
    @FXML
    private void handleClose() {
        Stage stage = (Stage) nameLabel.getScene().getWindow();
        stage.close();
    }

    // Veriyi dışarıdan (MainPage'den) almak için metod
    public void setActivityData(String name, String description, String imagePath) {
        nameLabel.setText(name);
        descriptionLabel.setText(description);

        // Eğer profil fotosu varsa yükle, yoksa default icon kalsın
        if (imagePath != null && !imagePath.isEmpty()) {
            profileImage.setImage(new Image(imagePath));
        }
    }
}

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

    // To get the data from the Main Page
    public void setActivityData(String name, String description, String imagePath) {
        nameLabel.setText(name);
        descriptionLabel.setText(description);

        // if the user has a profile picture, upload it, otherwise, the default icon will be shown.
        if (imagePath != null && !imagePath.isEmpty()) {
            profileImage.setImage(new Image(imagePath));
        }
    }
}

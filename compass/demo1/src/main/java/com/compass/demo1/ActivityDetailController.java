package com.compass.demo1;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.util.Base64;

public class ActivityDetailController {
    @FXML private Label nameLabel;
    @FXML private Label descriptionLabel;
    @FXML private ImageView profileImage;


    @FXML
    private void handleClose() {
        Stage stage = (Stage) nameLabel.getScene().getWindow();
        stage.close();
    }

    public void setActivityData(String name, String description, String photoBase64) {
        nameLabel.setText(name);
        descriptionLabel.setText(description);


        if (photoBase64 != null && !photoBase64.isEmpty()) {
            try {

                byte[] imageBytes = Base64.getDecoder().decode(photoBase64);
                profileImage.setImage(new Image(new ByteArrayInputStream(imageBytes)));
            } catch (Exception e) {

                setDefaultProfileImage();
            }
        } else {
            setDefaultProfileImage();
        }


    }
    private void setDefaultProfileImage() {
        try {
            profileImage.setImage(new Image(getClass().getResourceAsStream("icons/user.png")));
        } catch (Exception e) {
            System.out.println("Default user icon not found!");
        }
    }
}

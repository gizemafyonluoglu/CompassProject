package com.compass.demo1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ProfileController {
    private String uploadedFileBase64;
    private String uploadedFileName;

    @FXML private ImageView profileImageView;
    @FXML private Label defaultUserIcon;
    @FXML private TextField nameTextField;
    @FXML private Label emailLabel;

    @FXML private HBox interestsBox;
    @FXML private FlowPane interestsToggleGroup;

    @FXML private Label fileNameLabel;

    @FXML private Pane overlay;
    @FXML private VBox signOutPopup;
    @FXML private VBox boardMembershipPopup;
    @FXML private VBox interestsPopup;

    @FXML
    public void initialize() {
        Circle clip = new Circle(90, 90, 90);
        profileImageView.setClip(clip);

        User currentUser = SessionManager.getCurrentUser();

        if (currentUser != null) {
            nameTextField.setText(currentUser.getName() + " " + currentUser.getSurname());
            emailLabel.setText(currentUser.getEmail());
            String base64 = currentUser.getProfilePhotoBase64();
            if (base64 != null && !base64.isEmpty()) {
                byte[] imageBytes = Base64.getDecoder().decode(base64);
                Image img = new Image(new ByteArrayInputStream(imageBytes));
                profileImageView.setImage(img);
                defaultUserIcon.setVisible(false);
            }
        }

        renderInterestsDisplay();
    }

    private void renderInterestsDisplay() {
        interestsBox.getChildren().clear();

        List<Interest> userInterests = SessionManager.getCurrentUser().getInterests();
        int maxVisible = 2;

        for (int i = 0; i < userInterests.size(); i++) {
            if (i < maxVisible) {
                Label pill = new Label(userInterests.get(i).getInterestName());
                pill.getStyleClass().add("interest-pill");
                interestsBox.getChildren().add(pill);
            } else {
                int hiddenCount = userInterests.size() - maxVisible;
                Label overflowPill = new Label("+" + hiddenCount);
                overflowPill.setStyle("-fx-background-color: #E5E7EB; -fx-text-fill: #4B5563; -fx-padding: 5 12 5 12; -fx-background-radius: 20; -fx-font-weight: bold;");
                interestsBox.getChildren().add(overflowPill);
                break;
            }
        }
    }

    @FXML
    public void showInterestsPopup() {
        hideAllPopups();
        overlay.setVisible(true);
        interestsPopup.setVisible(true);

        List<Interest> dbInterests = SessionManager.getCurrentUser().getInterests();

        for (Node node : interestsToggleGroup.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton btn = (ToggleButton) node;
                btn.setSelected(false);

                for (Interest interest : dbInterests) {
                    if (interest.getInterestName().equals(btn.getText())) {
                        btn.setSelected(true);
                        break;
                    }
                }
            }
        }
    }

    @FXML
    public void saveInterests() {
        List<Interest> newInterests = new ArrayList<>();
        int idCounter = 1;

        for (Node node : interestsToggleGroup.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton btn = (ToggleButton) node;
                if (btn.isSelected()) {
                    newInterests.add(new Interest("INT" + idCounter++, btn.getText()));
                }
            }
        }

        User me = SessionManager.getCurrentUser();
        me.updateInterests(newInterests);
        Database.getInstance().saveUser(me);

        renderInterestsDisplay();
        hideAllPopups();
    }

    @FXML
    public void handleChangePhoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {

                byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());
                String base64String = Base64.getEncoder().encodeToString(fileBytes);
                Image img = new Image(new ByteArrayInputStream(fileBytes));
                profileImageView.setImage(img);
                defaultUserIcon.setVisible(false);
                User me = SessionManager.getCurrentUser();
                me.setProfilePhotoBase64(base64String);
                Database.getInstance().saveUser(me);

            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Fotoğraf yüklenirken hata oluştu!", ButtonType.OK);
                alert.showAndWait();
            }
        }
    }

    @FXML
    public void handleFileUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF veya Resim", "*.pdf", "*.png", "*.jpg"));
        Stage stage = (Stage) overlay.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                byte[] fileBytes = java.nio.file.Files.readAllBytes(selectedFile.toPath());
                uploadedFileBase64 = java.util.Base64.getEncoder().encodeToString(fileBytes);
                uploadedFileName = selectedFile.getName();
                fileNameLabel.setText(uploadedFileName);
                fileNameLabel.setStyle("-fx-text-fill: #8A2BE2; -fx-font-weight: bold;");
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void submitBoardMembership() {
        if (uploadedFileName != null && !uploadedFileName.isEmpty()) {
            User me = SessionManager.getCurrentUser();
            String requestId = "REQ-" + System.currentTimeMillis();
            String clubName = "";

            if (me instanceof ClubBoardMember) {
                clubName = ((ClubBoardMember) me).getClubName();
            }

            MembershipRequest request = new MembershipRequest(
                    requestId, uploadedFileName, clubName, java.time.LocalDate.now(), me.getUserId()
            );
            request.setDocumentBase64(uploadedFileBase64);

            Database.getInstance().saveMembershipRequest(request);

            hideAllPopups();
            fileNameLabel.setText("Upload a file...");
            fileNameLabel.setStyle("-fx-text-fill: #9CA3AF;");
            uploadedFileBase64 = null;
            uploadedFileName = null;
        }
    }
    @FXML
    public void showBoardMembershipPopup() { hideAllPopups(); overlay.setVisible(true); boardMembershipPopup.setVisible(true); }

    @FXML
    public void showSignOutPopup() { hideAllPopups(); overlay.setVisible(true); signOutPopup.setVisible(true); }

    @FXML
    public void confirmSignOut(ActionEvent event) throws IOException {
        SessionManager.setCurrentUser(null);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/compass/demo1/loginPage.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 900, 600));
        stage.show();
    }

    @FXML
    public void hideAllPopups() {
        if (overlay != null) overlay.setVisible(false);
        if (signOutPopup != null) signOutPopup.setVisible(false);
        if (boardMembershipPopup != null) boardMembershipPopup.setVisible(false);
        if (interestsPopup != null) interestsPopup.setVisible(false);
    }

    @FXML public void goToHome(ActionEvent event) throws IOException { switchScene(event, "/com/compass/demo1/mainPage.fxml"); }
    @FXML public void goToFriends(ActionEvent event) throws IOException { switchScene(event, "/com/compass/demo1/friendsPage.fxml"); }
    @FXML public void goToActivity(ActionEvent event) throws IOException { switchScene(event, "/com/compass/demo1/activityPage.fxml"); }
    @FXML public void goToCalendar(ActionEvent event) throws IOException { switchScene(event, "/com/compass/demo1/calendarPage.fxml"); }

    private void switchScene(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 900, 600));
        stage.show();
    }
}
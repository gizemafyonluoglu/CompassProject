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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileController {

    @FXML private ImageView profileImageView;
    @FXML private Label defaultUserIcon;
    @FXML private TextField nameTextField;

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

            // Profil fotoğrafı path'i varsa yükle
            // if (currentUser.getProfilePhotoPath() != null && !currentUser.getProfilePhotoPath().isEmpty()) {
            //    profileImageView.setImage(new Image(currentUser.getProfilePhotoPath()));
            //    defaultUserIcon.setVisible(false);
            // }
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

        // Not: Eğer User sınıfında setInterests() metodu yoksa, "interests" listesini public yapman
        // veya updateInterests gibi bir metot eklemen gerekebilir. Standart olarak setInterests farz ediyoruz.
        // me.setInterests(newInterests);

        // Alternatif (Listeyi temizleyip yeniden ekleme):
        me.updateInterests(newInterests);
        Database.getInstance().saveUser(me);

        // SİHRİN GERÇEKLEŞTİĞİ YER: Değişiklikleri Veritabanına Kaydet!
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
            String path = selectedFile.toURI().toString();
            profileImageView.setImage(new Image(path));
            defaultUserIcon.setVisible(false);

            // User nesnesini güncelle ve veritabanına kaydet
            User me = SessionManager.getCurrentUser();
            // me.setProfilePhotoPath(path); // Eğer User'da bu metot varsa açabilirsin
            Database.getInstance().saveUser(me);
        }
    }

    @FXML
    public void handleFileUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF veya Resim", "*.pdf", "*.png", "*.jpg"));
        Stage stage = (Stage) overlay.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            fileNameLabel.setText(selectedFile.getName());
            fileNameLabel.setStyle("-fx-text-fill: #8A2BE2; -fx-font-weight: bold;");
        }
    }

    @FXML
    public void submitBoardMembership() {
        if (!fileNameLabel.getText().equals("Upload a file...")) {
            System.out.println("Dosya onaya gitti: " + fileNameLabel.getText());
            hideAllPopups();
            fileNameLabel.setText("Upload a file...");
            fileNameLabel.setStyle("-fx-text-fill: #9CA3AF;");

            // İleride burada Database'e yeni bir MembershipRequest kaydedilebilir.
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
        if(overlay != null) overlay.setVisible(false);
        if(signOutPopup != null) signOutPopup.setVisible(false);
        if(boardMembershipPopup != null) boardMembershipPopup.setVisible(false);
        if(interestsPopup != null) interestsPopup.setVisible(false);
    }

    // --- MENÜ GEÇİŞLERİ ---
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
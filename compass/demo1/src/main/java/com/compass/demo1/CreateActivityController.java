package com.compass.demo1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class CreateActivityController {

    // --- FXML BİLEŞENLERİ (Tasarımdaki sırayla) ---
    @FXML private CheckBox switchVisibility;
    @FXML private CheckBox switchType;
    @FXML private HBox typeToggleContainer;

    @FXML private TextField inpCategory;
    @FXML private TextField inpName;
    @FXML private TextField inpDesc;
    @FXML private TextField inpPlace;
    @FXML private DatePicker datePicker;
    @FXML private TextField inpTime; // Örn: "14:00"
    @FXML private TextField inpQuota;

    @FXML private Pane overlay;
    @FXML private VBox categoryPopup;
    private List<String> selectedCategories = new java.util.ArrayList<>();

    @FXML
    public void initialize() {
        User me = SessionManager.getCurrentUser();

        // Kulüp Yöneticisi Kontrolü (Sadece onaylı kulüp üyeleri kulüp aktivitesi açabilir)
        if (switchType != null) {
            switchType.setDisable(true);
        }
        if (typeToggleContainer != null) {
            typeToggleContainer.setOpacity(0.5);
        }

        // 2. Eğer kullanıcı ONAYLI BİR KULÜP YÖNETİCİSİ ise kilidi aç!
        if (me instanceof ClubBoardMember && ((ClubBoardMember) me).isBoardStatusApproved()) {
            if (switchType != null) {
                switchType.setDisable(false);
            }
            if (typeToggleContainer != null) {
                typeToggleContainer.setOpacity(1.0); // Rengi normale döndür
            }
        }
    }

    @FXML
    public void handleSubmit(ActionEvent event) {
        try {
            // 1. FORM VERİLERİNİ TOPLA
            String name = inpName.getText();
            String desc = inpDesc.getText();
            String category = inpCategory.getText();
            String place = inpPlace.getText();
            LocalDate date = datePicker.getValue();

            // Saati Parse Et (Örn: "14:00 - 15:00" arasından sadece başlangıcı alıyoruz)
            String rawTime = inpTime.getText().split("-")[0].trim();
            LocalTime time = LocalTime.parse(rawTime);

            int quota = Integer.parseInt(inpQuota.getText());
            String visibility = (switchVisibility != null && switchVisibility.isSelected()) ? "Friends Only" : "Public";

            // MongoDB için benzersiz ID oluştur
            String activityId = "ACT-" + UUID.randomUUID().toString().substring(0, 8);

            User me = SessionManager.getCurrentUser();
            Activity newActivity;

            // 2. OOP POLİMORFİZMİ: Kulüp mü yoksa Normal mi?
            if (switchType != null && switchType.isSelected() && me instanceof ClubBoardMember) {
                ClubBoardMember boardMember = (ClubBoardMember) me;
                newActivity = new ClubActivity(
                        activityId, name, desc, category, place, date, time, quota,
                        visibility, "Club", boardMember.getClubName(), true
                );
            } else {
                newActivity = new Activity(
                        activityId, name, desc, category, place, date, time, quota,
                        visibility, "Regular"
                );
            }

            // Kurucu olarak kendini ekle
            newActivity.addParticipant(me);

            // 3. MONGODB KAYIT İŞLEMLERİ (Kritik Nokta)
            Database db = Database.getInstance();

            // A) Aktiviteyi activities koleksiyonuna kaydet
            db.saveActivity(newActivity);

            // B) Kullanıcının kendi "Oluşturduklarım" listesini güncelle ve kaydet
            List<Activity> created = me.getCreatedActivities();
            created.add(newActivity);
            me.setCreatedActivities(created);

            if (me.getCalendar() != null) {
                me.getCalendar().addActivity(newActivity);
            }

            db.saveUser(me);
            // MongoDB'de kullanıcının createdActivityIds listesi güncellenir

            // 4. BAŞARI MESAJI VE YÖNLENDİRME
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Aktivite başarıyla oluşturuldu!", ButtonType.OK);
            alert.showAndWait();

            goToHome(event);

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lütfen tüm alanları doğru doldurun (Saat: 14:00 gibi).", ButtonType.OK);
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    // Pop-up'ı açar
    @FXML
    public void showCategoryPopup() {
        if (overlay != null) overlay.setVisible(true);
        if (categoryPopup != null) categoryPopup.setVisible(true);
    }

    // Pop-up'ı kapatır
    @FXML
    public void hideCategoryPopup() {
        if (overlay != null) overlay.setVisible(false);
        if (categoryPopup != null) categoryPopup.setVisible(false);
    }

    // Butonlara tıklandığında seçimi ayarlar ve rengini değiştirir
    @FXML
    public void toggleCategory(ActionEvent event) {
        Node node = (Node) event.getSource();
        Button btn = (Button) node;

        if (node.getStyleClass().contains("selected")) {
            node.getStyleClass().remove("selected");
            selectedCategories.remove(btn.getText());
        } else {
            node.getStyleClass().add("selected");
            selectedCategories.add(btn.getText());
        }
    }

    // "OK" tuşuna basıldığında seçilenleri TextField'a yazar
    @FXML
    public void confirmCategories() {
        inpCategory.setText(String.join(", ", selectedCategories));
        hideCategoryPopup();
    }

    @FXML
    public void handleCancel(ActionEvent event) throws IOException {
        goToHome(event);
    }

    // --- NAVİGASYON ---


    @FXML public void goToProfile(ActionEvent event) throws IOException { switchScene(event, "profilePage.fxml"); }
    @FXML public void goToHome(ActionEvent event) throws IOException { switchScene(event, "mainPage.fxml"); }
    @FXML public void goToFriends(ActionEvent event) throws IOException {switchScene(event, "/com/compass/demo1/friendsPage.fxml"); }
    @FXML public void goToCalendar(ActionEvent event) throws IOException { switchScene(event, "/com/compass/demo1/calendarPage.fxml");}
    @FXML public void goToActivity(ActionEvent event) throws IOException { switchScene(event, "createActivity.fxml"); }

    private void switchScene(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 900, 600));
        stage.show();
    }
}

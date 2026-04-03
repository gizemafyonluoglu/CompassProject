package com.compass.demo1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivityPageController {

    @FXML private VBox activitiesListContainer;
    @FXML private TextField searchField;

    @FXML private Pane overlay;
    @FXML private VBox filterPopup;
    @FXML private VBox conflictPopup;


    private Activity pendingActivity = null;


    private List<Activity> allActivities = new ArrayList<>();

    @FXML
    public void initialize() {

        Locale.setDefault(Locale.ENGLISH);
        User me = SessionManager.getCurrentUser();


        if (allActivities.isEmpty()) {
            Activity a1 = new Activity("A1", "Tarkan Concert", "Müzik şöleni", "Music", "if performance hall", LocalDate.of(2026, 2, 26), LocalTime.of(20, 0), 2, "Public", "Regular");
            Activity a2 = new Activity("A2", "Kalben Concert", "Canlı müzik", "Music", "Milyon Performance", LocalDate.of(2026, 2, 27), LocalTime.of(20, 0), 2, "Public", "Regular");
            Activity a3 = new Activity("A3", "Yaşar Concert", "Nostalji", "Music", "CerModern", LocalDate.of(2026, 2, 28), LocalTime.of(21, 0), 3, "Public", "Regular");
            Activity a4 = new Activity("A4", "Watching Toy Story", "Film gecesi", "Cinema", "mayfest", LocalDate.of(2026, 3, 3), LocalTime.of(19, 0), 5, "Public", "Regular");


            a2.addParticipant(new User("Test1", "A", "B", "1@bilkent.edu.tr", "123"));
            a2.addParticipant(new User("Test2", "C", "D", "2@bilkent.edu.tr", "123"));

            allActivities.add(a1);
            allActivities.add(a2);
            allActivities.add(a3);
            allActivities.add(a4);


            if (me != null && me.getCalendar().getActivities().isEmpty() && me.getCalendar().getPlans().isEmpty()) {
                Activity fakeConflict = new Activity("Fake1", "Dolu Saat", "", "", "", LocalDate.of(2026, 2, 28), LocalTime.of(21, 0), 10, "", "");
                me.getCalendar().addActivity(fakeConflict);
            }
        }

        // Sayfa açıldığında tüm listeyi ekrana çiz
        renderActivities("");

        // 3. ARAMA (SEARCH) DİNAMİĞİ
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            renderActivities(newValue);
        });
    }


    private void renderActivities(String searchText) {
        activitiesListContainer.getChildren().clear();
        String lowerCaseFilter = searchText.toLowerCase();

        for (Activity act : allActivities) {

            if (act.isFull() || act.isCancelled()) continue;


            if (searchText.isEmpty() ||
                    act.getActivityName().toLowerCase().contains(lowerCaseFilter) ||
                    act.getPlace().toLowerCase().contains(lowerCaseFilter)) {

                HBox row = createActivityRow(act);
                activitiesListContainer.getChildren().add(row);
            }
        }
    }

    
    private HBox createActivityRow(Activity act) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("card-row");
        row.setPadding(new Insets(10, 20, 10, 20));

        HBox nameBox = new HBox(10); nameBox.setAlignment(Pos.CENTER_LEFT); nameBox.setPrefWidth(180);
        Label userIcon = new Label("👤"); userIcon.setStyle("-fx-font-size: 18; -fx-text-fill: #8A2BE2;");
        Label lblName = new Label(act.getActivityName()); lblName.setStyle("-fx-font-weight: bold;");
        nameBox.getChildren().addAll(userIcon, lblName);

        Label lblPlace = new Label(act.getPlace()); lblPlace.getStyleClass().add("badge-blue"); lblPlace.setPrefWidth(200);

        // LocalDate ve LocalTime'ı String olarak ekrana basıyoruz
        Label lblDate = new Label(act.getDate().toString()); lblDate.getStyleClass().add("badge-orange"); lblDate.setPrefWidth(100);

        // Bitiş saati için LocalTime'a 2 saat ekleyelim görsel olarak
        String timeText = act.getTime().toString() + " - " + act.getTime().plusHours(2).toString();
        Label lblTime = new Label(timeText); lblTime.getStyleClass().add("badge-green"); lblTime.setPrefWidth(120);

        // Katılımcı sayısını listenden dinamik alıyoruz!
        Label lblQuota = new Label(act.getJoinedUsers().size() + "/" + act.getQuota());
        lblQuota.setStyle("-fx-text-fill: #8A2BE2; -fx-font-weight: bold;"); lblQuota.setPrefWidth(60);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        // --- ÇAKIŞMA (CONFLICT) KONTROLÜ ---
        User me = SessionManager.getCurrentUser();
        boolean hasConflict = false;
        if (me != null) {
            hasConflict = me.getCalendar().checkConflict(act); // Senin yazdığın kontrol algoritması!
        }

        Button actionBtn = new Button("+");
        if (hasConflict) {
            actionBtn.getStyleClass().add("btn-plus-red");
            actionBtn.setOnAction(e -> showConflictPopup(act));
        } else {
            actionBtn.getStyleClass().add("btn-plus-green");
            actionBtn.setOnAction(e -> joinActivity(act, actionBtn));
        }

        row.getChildren().addAll(nameBox, lblPlace, lblDate, lblTime, lblQuota, spacer, actionBtn);
        return row;
    }

    // --- POP-UP'LAR VE AKSİYONLAR ---
    private void showConflictPopup(Activity act) {
        pendingActivity = act;
        hideAllPopups();
        overlay.setVisible(true);
        conflictPopup.setVisible(true);
    }

    @FXML
    public void joinConflictActivity() {
        if (pendingActivity != null) {
            // Butona erişimimiz olmadığı için tüm sayfayı yeniden çizdireceğiz
            User me = SessionManager.getCurrentUser();
            if (me != null) {
                pendingActivity.addParticipant(me);
                me.getCalendar().addActivity(pendingActivity);
            }
            renderActivities(searchField.getText());
        }
        hideAllPopups();
    }

    private void joinActivity(Activity act, Button btn) {
        User me = SessionManager.getCurrentUser();
        if (me != null) {
            act.addParticipant(me); // Aktiviteye kendini ekle
            me.getCalendar().addActivity(act); // Takvimine aktiviteyi ekle
        }

        btn.setText("✔");
        btn.setDisable(true);
        btn.setStyle("-fx-background-color: #9CA3AF; -fx-text-fill: white; -fx-background-radius: 50;");

        // Ekranda kotanın güncellenmesi için sayfayı yeniden çizdiriyoruz
        renderActivities(searchField.getText());
    }

    @FXML public void showFilterPopup() { hideAllPopups(); overlay.setVisible(true); filterPopup.setVisible(true); }
    @FXML public void applyFilters() { hideAllPopups(); }
    @FXML public void hideAllPopups() { if(overlay != null) overlay.setVisible(false); if(filterPopup != null) filterPopup.setVisible(false); if(conflictPopup != null) conflictPopup.setVisible(false); }

    // --- SAYFA GEÇİŞLERİ ---
    @FXML public void goToProfile(ActionEvent event) throws IOException { switchScene(event, "profilePage.fxml"); }
    @FXML public void goToHome(ActionEvent event) throws IOException { switchScene(event, "mainPage.fxml"); }
    @FXML public void goToFriends(ActionEvent event) throws IOException {switchScene(event, "/com/compass/demo1/friendsPage.fxml"); }
    @FXML public void goToCalendar(ActionEvent event) throws IOException { switchScene(event, "/com/compass/demo1/calendarPage.fxml");}
    @FXML public void goToCreateActivity(ActionEvent event) throws IOException { switchScene(event, "createActivity.fxml"); }

    private void switchScene(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 900, 600));
        stage.show();
    }
}
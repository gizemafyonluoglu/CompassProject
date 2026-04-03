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
import java.util.List;

public class FriendsController {

    @FXML private VBox usersListContainer;
    @FXML private TextField searchField;
    @FXML private Label pageTitle;
    @FXML private Label recentSearchesLabel;

    // Sadece 2 ekranımız var: Blocked modu AÇIK veya KAPALI
    private boolean isBlockedScreen = false;

    // İLERİDE VERİTABANINDAN GELECEK: Sistemdeki Tüm Kullanıcılar
    private List<User> allUsersInDatabase;

    @FXML
    public void initialize() {
        // --- SİHRİN GERÇEKLEŞTİĞİ YER ---
        // Artık test verileriyle uğraşmıyoruz, tüm kullanıcıları direkt DB'den çekiyoruz!
        allUsersInDatabase = Database.getInstance().getAllUsers();

        // Sayfa açılışında Friends ekranını çiz
        renderList("");

        // Arama yapıldıkça ekranı anlık güncelle
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            renderList(newVal);
            // Friends ekranında arama yapılıyorsa "Recent Searches" yazısını göster
            recentSearchesLabel.setVisible(!newVal.isEmpty() && !isBlockedScreen);
        });
    }

    // --- 2 EKRANLI ÇİZİM MANTIĞI ---
    private void renderList(String searchText) {
        usersListContainer.getChildren().clear();
        String filter = searchText.toLowerCase();
        User me = SessionManager.getCurrentUser();

        if (me == null) return;

        if (isBlockedScreen) {
            // EKRAN 2: Sadece engellenenler ve onlar arasında arama
            for (User user : me.getBlockedUsers()) {
                String fullName = (user.getName() + " " + user.getSurname()).toLowerCase();
                if (fullName.contains(filter)) {
                    usersListContainer.getChildren().add(createUserRow(user, "BLOCKED"));
                }
            }
        } else {
            // EKRAN 1: Friends Ekranı
            if (searchText.isEmpty()) {
                // Arama yokken sadece arkadaşlar
                for (User user : me.getFriends()) {
                    usersListContainer.getChildren().add(createUserRow(user, "FRIEND"));
                }
            } else {
                // Arama varken engelli OLMAYAN herkes (Arkadaşlar + Yabancılar)
                for (User user : allUsersInDatabase) {
                    // Kendimizi veya engellediklerimizi arama sonucunda gösterme
                    if (user.equals(me) || me.getBlockedUsers().contains(user)) continue;

                    String fullName = (user.getName() + " " + user.getSurname()).toLowerCase();
                    if (fullName.contains(filter)) {
                        String status = me.getFriends().contains(user) ? "FRIEND" : "STRANGER";
                        usersListContainer.getChildren().add(createUserRow(user, status));
                    }
                }
            }
        }
    }

    // --- SATIR (KART) TASARIMI OLUŞTURUCU ---
    private HBox createUserRow(User user, String status) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("card-row");
        row.setPadding(new Insets(10, 20, 10, 20));

        // İkon ve İsim
        HBox nameBox = new HBox(15); nameBox.setAlignment(Pos.CENTER_LEFT);
        Label iconLabel = new Label("👤");
        iconLabel.setStyle("-fx-background-color: #F3E8FF; -fx-text-fill: #8A2BE2; -fx-font-size: 18; -fx-padding: 10; -fx-background-radius: 50;");
        Label lblName = new Label(user.getName() + " " + user.getSurname());
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        nameBox.getChildren().addAll(iconLabel, lblName);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        // Dinamik Ortak İlgi Alanları (İki kullanıcının Interest objelerini karşılaştırıyoruz)
        HBox interestsBox = new HBox(10); interestsBox.setAlignment(Pos.CENTER); interestsBox.setPrefWidth(200);
        User me = SessionManager.getCurrentUser();

        if (me != null) {
            for (Interest myInterest : me.getInterests()) {
                for (Interest theirInterest : user.getInterests()) {
                    if (myInterest.getInterestName().equals(theirInterest.getInterestName())) {
                        Label pill = new Label(theirInterest.getInterestName());
                        pill.getStyleClass().add("common-interest-pill");
                        interestsBox.getChildren().add(pill);
                        break;
                    }
                }
            }
        }

        // Dinamik Butonlar
        Button actionBtn = new Button(); actionBtn.setPrefWidth(120);

        if (status.equals("FRIEND")) {
            actionBtn.setText("Block"); actionBtn.getStyleClass().add("btn-action-red");
            actionBtn.setOnAction(e -> handleAction(user, "BLOCK"));
        } else if (status.equals("BLOCKED")) {
            actionBtn.setText("Unblock"); actionBtn.getStyleClass().add("btn-action-green");
            actionBtn.setOnAction(e -> handleAction(user, "UNBLOCK"));
        } else if (status.equals("STRANGER")) {
            actionBtn.setText("👤+ Add Friend"); actionBtn.getStyleClass().add("btn-action-blue");
            actionBtn.setOnAction(e -> handleAction(user, "ADD_FRIEND"));
        }

        row.getChildren().addAll(nameBox, spacer, interestsBox, actionBtn);
        return row;
    }

    // Ekranlar arası geçiş (Toggle)
    @FXML
    public void toggleView() {
        isBlockedScreen = !isBlockedScreen;
        pageTitle.setText(isBlockedScreen ? "Blocked Friends" : "Friends");
        searchField.clear();
        renderList("");
    }

    // Buton işlemleri (Senin User sınıfındaki metotlarla veriyi güncelle, DB'ye kaydet ve ekranı yenile)
    private void handleAction(User targetUser, String action) {
        User me = SessionManager.getCurrentUser();
        if (me == null) return;

        // Senin User.java içindeki klas OOP metotların çalışıyor
        if (action.equals("BLOCK")) {
            me.blockUser(targetUser);
        } else if (action.equals("UNBLOCK")) {
            me.unblockUser(targetUser);
        } else if (action.equals("ADD_FRIEND")) {
            me.addFriend(targetUser);
        }

        // GERÇEK UYGULAMA MANTIĞI: Değişikliği Veritabanına da Kaydet!
        Database.getInstance().saveUser(me);

        renderList(searchField.getText()); // Ekranı son duruma göre yenile
    }

    // --- MENÜ GEÇİŞLERİ ---
    @FXML public void goToProfile(ActionEvent event) throws IOException { switchScene(event, "profilePage.fxml"); }
    @FXML public void goToHome(ActionEvent event) throws IOException { switchScene(event, "mainPage.fxml"); }
    @FXML public void goToActivity(ActionEvent event) throws IOException { switchScene(event, "activityPage.fxml"); }
    @FXML public void goToCalendar(ActionEvent event) throws IOException { switchScene(event, "calendarPage.fxml"); }

    private void switchScene(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 900, 600));
        stage.show();
    }
}
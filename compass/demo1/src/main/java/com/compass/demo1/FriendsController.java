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

    private boolean isBlockedScreen = false;
    private List<User> allUsersInDatabase;

    @FXML
    public void initialize() {
        allUsersInDatabase = Database.getInstance().getAllUsers();
        renderList("");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            renderList(newVal);
            recentSearchesLabel.setVisible(!newVal.isEmpty() && !isBlockedScreen);
        });
    }

    // --- 2-SCREEN DRAWING LOGIC ---
    private void renderList(String searchText) {
        usersListContainer.getChildren().clear();
        String filter = searchText.toLowerCase();
        User me = SessionManager.getCurrentUser();

        if (me == null) return;

        if (isBlockedScreen) {
            // Screen 2: Only blocked friends
            for (User user : me.getBlockedUsers()) {
                String fullName = (user.getName() + " " + user.getSurname()).toLowerCase();
                if (fullName.contains(filter)) {
                    usersListContainer.getChildren().add(createUserRow(user, "BLOCKED"));
                }
            }
        } else {
            // Screen 1: Friends screen
            if (searchText.isEmpty()) {
                // Arama yokken sadece arkadaşlar
                for (User user : me.getFriends()) {
                    usersListContainer.getChildren().add(createUserRow(user, "FRIEND"));
                }
            } else {
                // Everyone who is not blocked while the call is active (Friends + Strangers)
                for (User user : allUsersInDatabase) {
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

        // Icon and name
        HBox nameBox = new HBox(15); nameBox.setAlignment(Pos.CENTER_LEFT);
        Label iconLabel = new Label("👤");
        iconLabel.setStyle("-fx-background-color: #F3E8FF; -fx-text-fill: #8A2BE2; -fx-font-size: 18; -fx-padding: 10; -fx-background-radius: 50;");
        Label lblName = new Label(user.getName() + " " + user.getSurname());
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        nameBox.getChildren().addAll(iconLabel, lblName);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

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

    // Toggle
    @FXML
    public void toggleView() {
        isBlockedScreen = !isBlockedScreen;
        pageTitle.setText(isBlockedScreen ? "Blocked Friends" : "Friends");
        searchField.clear();
        renderList("");
    }

    private void handleAction(User targetUser, String action) {
        User me = SessionManager.getCurrentUser();
        if (me == null) return;
        if (action.equals("BLOCK")) {
            me.blockUser(targetUser);
        } else if (action.equals("UNBLOCK")) {
            me.unblockUser(targetUser);
        } else if (action.equals("ADD_FRIEND")) {
            me.addFriend(targetUser);
        }
        Database.getInstance().saveUser(me);
        renderList(searchField.getText());
    }

    // --- MENU TRANSITIONS ---
    @FXML public void goToProfile(ActionEvent event) throws IOException { switchScene(event, "profilePage.fxml"); }
    @FXML public void goToHome(ActionEvent event) throws IOException { switchScene(event, "mainPage.fxml"); }
    @FXML public void goToActivity(ActionEvent event) throws IOException { switchScene(event, "activityPage.fxml"); }
    @FXML public void goToCalendar(ActionEvent event) throws IOException { switchScene(event, "calendarPage.fxml"); }

    private void switchScene(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        boolean wasFullScreen = stage.isFullScreen();
        stage.setScene(new Scene(root));
        stage.setFullScreen(wasFullScreen);
        stage.show();
    }
}
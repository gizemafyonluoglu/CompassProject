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
        allActivities = Database.getInstance().getActivities();

        renderActivities("");

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

        HBox nameBox = new HBox(10);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        nameBox.setPrefWidth(180);
        Label userIcon = new Label("👤");
        userIcon.setStyle("-fx-font-size: 18; -fx-text-fill: #8A2BE2;");
        Label lblName = new Label(act.getActivityName());
        lblName.setStyle("-fx-font-weight: bold;");
        nameBox.getChildren().addAll(userIcon, lblName);

        Label lblPlace = new Label(act.getPlace());
        lblPlace.getStyleClass().add("badge-blue");
        lblPlace.setPrefWidth(200);

        Label lblDate = new Label(act.getDate().toString());
        lblDate.getStyleClass().add("badge-orange");
        lblDate.setPrefWidth(100);

        String timeText = act.getTime().toString() + " - " + act.getTime().plusHours(2).toString();
        Label lblTime = new Label(timeText);
        lblTime.getStyleClass().add("badge-green");
        lblTime.setPrefWidth(120);

        Label lblQuota = new Label(act.getJoinedUsers().size() + "/" + act.getQuota());
        lblQuota.setStyle("-fx-text-fill: #8A2BE2; -fx-font-weight: bold;");
        lblQuota.setPrefWidth(60);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        User me = SessionManager.getCurrentUser();
        boolean alreadyJoined = false;
        if (me != null) {
            for (User u : act.getJoinedUsers()) {
                if (u.getUserId().equals(me.getUserId())) {
                    alreadyJoined = true;
                    break;
                }
            }
        }

        Button actionBtn = new Button();

        if (alreadyJoined) {

            actionBtn.setText("✔");
            actionBtn.setDisable(true);
            actionBtn.setStyle("-fx-background-color: #9CA3AF; -fx-text-fill: white; -fx-background-radius: 50; -fx-min-width: 35; -fx-min-height: 35;");
        } else {
            boolean hasConflict = false;
            if (me != null) {
                hasConflict = me.getCalendar().checkConflict(act);
            }

            actionBtn.setText("+");
            if (hasConflict) {
                actionBtn.getStyleClass().add("btn-plus-red");
                actionBtn.setOnAction(e -> showConflictPopup(act));
            } else {
                actionBtn.getStyleClass().add("btn-plus-green");
                actionBtn.setOnAction(e -> joinActivity(act, actionBtn));
            }
        }

        row.getChildren().addAll(nameBox, lblPlace, lblDate, lblTime, lblQuota, spacer, actionBtn);
        return row;
    }

    private void showConflictPopup(Activity act) {
        pendingActivity = act;
        hideAllPopups();
        overlay.setVisible(true);
        conflictPopup.setVisible(true);
    }

    @FXML
    public void joinConflictActivity() {
        if (pendingActivity != null) {
            User me = SessionManager.getCurrentUser();
            if (me != null) {

                boolean alreadyJoined = false;
                for (User u : pendingActivity.getJoinedUsers()) {
                    if (u.getUserId().equals(me.getUserId())) {
                        alreadyJoined = true;
                        break;
                    }
                }

                if (!alreadyJoined) {
                    pendingActivity.addParticipant(me);
                    me.getCalendar().addActivity(pendingActivity);
                    Database db = Database.getInstance();
                    db.saveActivity(pendingActivity);

                    List<Activity> attended = me.getAttendedActivities();
                    attended.add(pendingActivity);
                    me.setAttendedActivities(attended);
                    db.saveUser(me);
                }
            }
            renderActivities(searchField.getText());
        }
        hideAllPopups();
    }

    private void joinActivity(Activity act, Button btn) {
        User me = SessionManager.getCurrentUser();
        if (me == null) return;
        for (User u : act.getJoinedUsers()) {
            if (u.getUserId().equals(me.getUserId())) {
                return; 
            }
        }

        act.addParticipant(me);
        me.getCalendar().addActivity(act);
        Database db = Database.getInstance();
        db.saveActivity(act);

        List<Activity> attended = me.getAttendedActivities();
        attended.add(act);
        me.setAttendedActivities(attended);
        db.saveUser(me);

        btn.setText("✔");
        btn.setDisable(true);
        btn.setStyle("-fx-background-color: #9CA3AF; -fx-text-fill: white; -fx-background-radius: 50;");

        renderActivities(searchField.getText());
    }

    @FXML public void showFilterPopup() { hideAllPopups(); overlay.setVisible(true); filterPopup.setVisible(true); }
    @FXML public void applyFilters() { hideAllPopups(); }
    @FXML public void hideAllPopups() {
        if (overlay != null) overlay.setVisible(false);
        if (filterPopup != null) filterPopup.setVisible(false);
        if (conflictPopup != null) conflictPopup.setVisible(false);
    }

    @FXML public void goToProfile(ActionEvent event) throws IOException { switchScene(event, "profilePage.fxml"); }
    @FXML public void goToHome(ActionEvent event) throws IOException { switchScene(event, "mainPage.fxml"); }
    @FXML public void goToFriends(ActionEvent event) throws IOException { switchScene(event, "/com/compass/demo1/friendsPage.fxml"); }
    @FXML public void goToCalendar(ActionEvent event) throws IOException { switchScene(event, "/com/compass/demo1/calendarPage.fxml"); }
    @FXML public void goToCreateActivity(ActionEvent event) throws IOException { switchScene(event, "createActivity.fxml"); }

    private void switchScene(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 900, 600));
        stage.show();
    }
}

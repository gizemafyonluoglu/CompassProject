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
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.scene.Cursor;

public class ActivityPageController {

    @FXML private VBox activitiesListContainer;
    @FXML private TextField searchField;

    @FXML private Pane overlay;
    @FXML private VBox filterPopup;
    @FXML private VBox conflictPopup;
    @FXML private DatePicker filterDatePicker;
    @FXML private TextField filterTimeField;
    @FXML private FlowPane interestsFlowPane;

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
        nameBox.setPrefWidth(160);


        javafx.scene.image.ImageView profileView = new javafx.scene.image.ImageView();
        String photoBase64 = act.getJoinedUsers().get(0).getProfilePhotoBase64();

        if (photoBase64 != null && !photoBase64.isEmpty()) {
            try {
                byte[] imageBytes = java.util.Base64.getDecoder().decode(photoBase64);
                profileView.setImage(new javafx.scene.image.Image(new java.io.ByteArrayInputStream(imageBytes)));
            } catch (Exception e) {
                profileView.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("icons/user.png")));
            }
        } else {
            try {
                profileView.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("icons/user.png")));
            } catch (Exception e) {
                System.out.println("Default image not found");
            }
        }


        profileView.setFitHeight(30);
        profileView.setFitWidth(30);
        profileView.setClip(new javafx.scene.shape.Circle(15, 15, 15));


        Label lblName = new Label(act.getActivityName());
        lblName.setStyle("-fx-font-weight: bold;");


        nameBox.getChildren().addAll(profileView, lblName);

        Label lblPlace = new Label(act.getPlace());
        lblPlace.getStyleClass().add("badge-blue");
        lblPlace.setPrefWidth(120);

        Label lblDate = new Label(act.getDate().toString());
        lblDate.getStyleClass().add("badge-orange");
        lblDate.setPrefWidth(130);

        String timeText = act.getTime().toString() + " - " + act.getTime().plusHours(2).toString();
        Label lblTime = new Label(timeText);
        lblTime.getStyleClass().add("badge-green");
        lblTime.setPrefWidth(140);

        Label lblQuota = new Label(act.getJoinedUsers().size() + "/" + act.getQuota());
        lblQuota.setStyle("-fx-text-fill: #8A2BE2; -fx-font-weight: bold;");
        lblQuota.setPrefWidth(50);

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

        //ACTİVİTY DETAİL

        row.setCursor(javafx.scene.Cursor.HAND);
        row.setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof Button)) {
                openActivityPopUp(act);
            }
        });
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
    @FXML
    public void applyFilters() {

        LocalDate selectedDate = filterDatePicker.getValue();

        LocalTime selectedTime = null;
        String timeText = filterTimeField.getText();
        if (timeText != null && !timeText.trim().isEmpty()) {
            try {
                selectedTime = LocalTime.parse(timeText.trim());
            } catch (Exception e) {
                System.out.println("Saat formatı hatalı, filtreye dahil edilmedi.");
            }
        }

        String locationFilter = null;
        String typeFilter = null;

        ActivityFilter myFilter = new ActivityFilter(selectedDate, selectedTime, locationFilter, typeFilter);
        List<Activity> filteredList = myFilter.applyFilter(allActivities);



        List<String> selectedInterests = new ArrayList<>();
        for (Node node : interestsFlowPane.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton tb = (ToggleButton) node;
                if (tb.isSelected()) {
                    selectedInterests.add(tb.getText()); 
                }
            }
        }

        if (!selectedInterests.isEmpty()) {
            List<Activity> interestFilteredList = new ArrayList<>();
            for (Activity act : filteredList) {

                if (selectedInterests.contains(act.getCategory())) {
                    interestFilteredList.add(act);
                }
            }
            filteredList = interestFilteredList; 
        }

  
        activitiesListContainer.getChildren().clear();
        for (Activity act : filteredList) {

            if (act.isFull() || act.isCancelled()) continue;

            HBox row = createActivityRow(act);
            activitiesListContainer.getChildren().add(row);
        }

        hideAllPopups();
    }

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
        boolean wasFullScreen = stage.isFullScreen();
        double currentWidth = stage.getWidth();
        double currentHeight = stage.getHeight();
        stage.setScene(new Scene(root, currentWidth, currentHeight));
        if (!wasFullScreen) {
            stage.setWidth(currentWidth);
            stage.setHeight(currentHeight);
            stage.centerOnScreen();
        }
        stage.setFullScreen(wasFullScreen);
        stage.show();
    }

    private void openActivityPopUp(Activity act) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ActivityDetail.fxml"));
            Parent root = loader.load();

            ActivityDetailController controller = loader.getController();
            controller.setActivityData(
                    act.getJoinedUsers().get(0).getName() + act.getJoinedUsers().get(0).getSurname(),
                    act.getDescription(),
                    null
            );

            Stage popupStage = new Stage();
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popupStage.initStyle(javafx.stage.StageStyle.UNDECORATED);

            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
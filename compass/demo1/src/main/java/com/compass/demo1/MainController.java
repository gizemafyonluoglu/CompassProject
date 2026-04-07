package com.compass.demo1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class MainController {

    @FXML private VBox activityContainer;

    @FXML
    public void initialize() {

        loadUserActivities();
    }

    private void loadUserActivities() {
        activityContainer.getChildren().clear();
        User me = SessionManager.getCurrentUser();

        if (me == null) return;

        for (Activity act : me.getCreatedActivities()) {
            if (!act.isCancelled()) {
                addActivityRow(act, true);
            }
        }
        for (Activity act : me.getAttendedActivities()) {
            if (!act.isCancelled()) {
                addActivityRow(act, false);
            }
        }
    }

    private void addActivityRow(Activity act, boolean isOwner) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("card-row");
        row.setPadding(new Insets(10, 20, 10, 20));
        StackPane profileBox = new StackPane();
        profileBox.setPrefSize(40, 40);

        ImageView profileView = new ImageView();
        String photoBase64 = act.getJoinedUsers().get(0).getProfilePhotoBase64();

        if (photoBase64 != null && !photoBase64.isEmpty()) {
            try {

                byte[] imageBytes = java.util.Base64.getDecoder().decode(photoBase64);
                profileView.setImage(new Image(new java.io.ByteArrayInputStream(imageBytes)));
            } catch (Exception e) {

                profileView.setImage(new Image(getClass().getResourceAsStream("icons/user.png")));
            }
        } else {

            try {
                profileView.setImage(new Image(getClass().getResourceAsStream("icons/user.png")));
            } catch (Exception e) {
                System.out.println("Default image not found");
            }
        }

        profileView.setFitHeight(40);
        profileView.setFitWidth(40);
        profileView.setClip(new javafx.scene.shape.Circle(20, 20, 20));

        profileBox.getChildren().add(profileView);
        if (act instanceof ClubActivity) {
            Label badge = new Label("✔");
            badge.setStyle("-fx-background-color: white; -fx-text-fill: #16A34A; -fx-font-size: 10px; " +
                    "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 0 4 0 4; " +
                    "-fx-border-color: #16A34A; -fx-border-radius: 10; -fx-border-width: 1.5;");
            StackPane.setAlignment(badge, Pos.BOTTOM_RIGHT);
            badge.setTranslateX(5);
            badge.setTranslateY(5);
            profileBox.getChildren().add(badge);
        }
        Label lblName = new Label(act.getActivityName());
        lblName.setPrefWidth(160);
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: black;");
        Label lblPlace = new Label(act.getPlace());
        lblPlace.getStyleClass().add("badge-blue");
        lblPlace.setPrefWidth(120);

        Label lblDate = new Label(act.getDate().toString());
        lblDate.getStyleClass().add("badge-orange");
        lblDate.setPrefWidth(130);
        String timeStr = act.getTime().toString() + " - " + act.getTime().plusMinutes(90).toString();
        Label lblTime = new Label(timeStr);
        lblTime.getStyleClass().add("badge-green");
        lblTime.setPrefWidth(140);
        Label lblQuota = new Label(act.getJoinedUsers().size() + "/" + act.getQuota());
        lblQuota.setStyle("-fx-text-fill: #8A2BE2; -fx-font-weight: bold;");
        lblQuota.setPrefWidth(50);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button actionBtn = new Button(isOwner ? "X" : "—");
        String btnColor = isOwner ? "#EF4444" : "#F97316";
        actionBtn.setStyle("-fx-background-color: " + btnColor + "; -fx-text-fill: white; " +
                "-fx-background-radius: 50; -fx-min-width: 32; -fx-min-height: 32; -fx-cursor: hand;");
        actionBtn.setOnAction(e -> {
            if (isOwner) {

                handleCancelProcess(act, row);
            } else {

                handleLeaveProcess(act, row);
            }
        });

        row.getChildren().addAll(profileBox, lblName, lblPlace, lblDate, lblTime, lblQuota, spacer, actionBtn);

        //ACTİVİTY DETAİL
        row.setCursor(javafx.scene.Cursor.HAND);
        row.setOnMouseClicked(event -> {

            if (!(event.getTarget() instanceof Button)) {
                openActivityPopUp(act);
            }
        });
        activityContainer.getChildren().add(row);
    }
    private void handleCancelProcess(Activity act, HBox row) {
        LocalDateTime startTime = LocalDateTime.of(act.getDate(), act.getTime());
        long hoursLeft = Duration.between(LocalDateTime.now(), startTime).toHours();

        if (hoursLeft < 3 && hoursLeft >= 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Time Conflict!");
            alert.setContentText("The activity starts in less than 3 hours. You cannot cancel.");

            javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #8A2BE2; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-font-size: 14px; -fx-text-fill: #6B7280; -fx-font-weight: bold;");
            ((Stage) dialogPane.getScene().getWindow()).initStyle(javafx.stage.StageStyle.UNDECORATED);


            Button okBtn = (Button) dialogPane.lookupButton(ButtonType.OK);
            if (okBtn != null) {
                String btnNormal = "-fx-background-color: #8A2BE2; -fx-text-fill: white; -fx-background-radius: 15px; -fx-cursor: hand; -fx-padding: 8 20 8 20;";
                String btnHover = "-fx-background-color: #6d22b3; -fx-text-fill: white; -fx-background-radius: 15px; -fx-cursor: hand; -fx-padding: 8 20 8 20;";

                okBtn.setStyle(btnNormal);
                okBtn.setOnMouseEntered(e -> okBtn.setStyle(btnHover));
                okBtn.setOnMouseExited(e -> okBtn.setStyle(btnNormal));
            }

            alert.showAndWait();
        } else {
            if (confirmAction("Cancel activity", "Are you sure you want to delete this activity?")) {
                Database db = Database.getInstance();
                User me = SessionManager.getCurrentUser();

                act.cancelActivity();
                db.saveActivity(act);

                List<Activity> created = me.getCreatedActivities();
                created.remove(act);
                me.setCreatedActivities(created);
                me.getCalendar().removeActivity(act);
                db.saveUser(me);

                activityContainer.getChildren().remove(row);
            }
        }
    }

    private void handleLeaveProcess(Activity act, HBox row) {
        if (confirmAction("Leave the activity",   " Are you sure you want to quit "  + act.getActivityName())) {
            User me = SessionManager.getCurrentUser();
            Database db = Database.getInstance();

            act.removeParticipant(me);
            db.saveActivity(act);

            me.getCalendar().removeActivity(act);

            List<Activity> attended = me.getAttendedActivities();
            attended.remove(act);
            me.setAttendedActivities(attended);
            db.saveUser(me);

            activityContainer.getChildren().remove(row);
        }
    }

    private boolean confirmAction(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #8A2BE2; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-font-size: 14px; -fx-text-fill: #6B7280; -fx-font-weight: bold;");
        ((Stage) dialogPane.getScene().getWindow()).initStyle(javafx.stage.StageStyle.UNDECORATED);


        Button okBtn = (Button) dialogPane.lookupButton(ButtonType.OK);
        Button cancelBtn = (Button) dialogPane.lookupButton(ButtonType.CANCEL);

        String btnNormal = "-fx-background-color: #8A2BE2; -fx-text-fill: white; -fx-background-radius: 15px; -fx-cursor: hand; -fx-padding: 8 20 8 20;";
        String btnHover = "-fx-background-color: #6d22b3; -fx-text-fill: white; -fx-background-radius: 15px; -fx-cursor: hand; -fx-padding: 8 20 8 20;";

        if (okBtn != null) {
            okBtn.setStyle(btnNormal);
            okBtn.setOnMouseEntered(e -> okBtn.setStyle(btnHover));
            okBtn.setOnMouseExited(e -> okBtn.setStyle(btnNormal));
        }
        if (cancelBtn != null) {
            cancelBtn.setStyle(btnNormal);
            cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(btnHover));
            cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(btnNormal));
        }

        return alert.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }
    @FXML public void goToProfile(ActionEvent event) throws IOException { switchScene(event, "profilePage.fxml"); }
    @FXML public void goToFriends(ActionEvent event) throws IOException { switchScene(event, "friendsPage.fxml"); }
    @FXML public void goToHome(ActionEvent event) throws IOException { }
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
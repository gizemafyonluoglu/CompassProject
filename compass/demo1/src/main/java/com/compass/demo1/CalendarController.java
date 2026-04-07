package com.compass.demo1;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class CalendarController {

    @FXML private VBox mainContentArea;
    @FXML private Button modeBtn;
    @FXML private Label pageTitle, lblCourse, lblPerm, lblOneTime;
    @FXML private HBox daysHeader;
    @FXML private VBox calendarGrid;
    @FXML private Pane eventsOverlay;
    @FXML private Pane overlay;
    @FXML private VBox addPlanPopup;
    @FXML private ComboBox<String> cmbDay;
    @FXML private TextField inpName, inpStart, inpEnd;
    @FXML private RadioButton rbCourse, rbPerm, rbOneTime;

    private boolean isDayMode = true;
    private final int HOUR_HEIGHT = 60; 

    @FXML
    public void initialize() {

        ToggleGroup group = new ToggleGroup();
        rbCourse.setToggleGroup(group); rbPerm.setToggleGroup(group); rbOneTime.setToggleGroup(group);
        cmbDay.getItems().addAll("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");

        Platform.runLater(() -> renderCalendar());
    }
    @FXML
    public void toggleMode() {
        isDayMode = !isDayMode;

        if (isDayMode) {
            modeBtn.setText("☼ Day Mode (8:00 - 18:00)");
            modeBtn.getStyleClass().setAll("btn-mode");
            mainContentArea.setStyle("-fx-background-color: white;");
            setTextColor("#000000");
        } else {
            modeBtn.setText("☾ Night Mode (18:00 - 8:00)");
            modeBtn.getStyleClass().setAll("btn-mode-night");
            mainContentArea.setStyle("-fx-background-color: #111827;");
            setTextColor("#FFFFFF"); 
        }
        renderCalendar();
    }

    private void setTextColor(String colorHex) {
        lblCourse.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: bold;");
        lblPerm.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: bold;");
        lblOneTime.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: bold;");
    }
    private void renderCalendar() {
        calendarGrid.getChildren().clear();
        eventsOverlay.getChildren().clear();
        daysHeader.getChildren().clear();

        int startHour = isDayMode ? 8 : 18;
        int endHour = isDayMode ? 18 : 32; 
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        double colWidth = (mainContentArea.getWidth() - 130) / 7;

        for (String day : days) {
            Label lblDay = new Label(day);
            lblDay.setPrefWidth(colWidth);
            lblDay.setAlignment(Pos.CENTER);
            lblDay.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (isDayMode ? "#000" : "#FFF") + ";");
            daysHeader.getChildren().add(lblDay);
        }
        for (int i = startHour; i <= endHour; i++) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.TOP_LEFT);
            row.setPrefHeight(HOUR_HEIGHT);

            String hourText = String.format("%02d:00", i % 24);
            Label timeLabel = new Label(hourText);
            timeLabel.setPrefWidth(50);
            timeLabel.setStyle("-fx-text-fill: " + (isDayMode ? "#4B5563" : "#D1D5DB") + "; -fx-font-size: 12;");

            Region line = new Region();
            HBox.setHgrow(line, Priority.ALWAYS);
            line.getStyleClass().add(isDayMode ? "calendar-line" : "calendar-line-night");

            row.getChildren().addAll(timeLabel, line);
            calendarGrid.getChildren().add(row);
        }

        User me = SessionManager.getCurrentUser();
        if (me == null) return;
        for (Plan plan : me.getCalendar().getPlans()) {
            drawEventBox(
                    plan.getTitle(),
                    plan.getStartTime(),
                    plan.getEndTime(),
                    plan.getDate(),
                    plan, 
                    startHour, endHour, colWidth
            );
        }
        for (Activity act : me.getCalendar().getActivities()) {

            drawEventBox(
                    act.getActivityName(),
                    act.getTime(),
                    act.getTime().plusMinutes(90),
                    act.getDate(),
                    null, 
                    startHour, endHour, colWidth
            );
        }
    }
    private void drawEventBox(String title, LocalTime start, LocalTime end, LocalDate date, Plan planTypeObj, int viewStartHour, int viewEndHour, double colWidth) {
        double startDecimal = start.getHour() + (start.getMinute() / 60.0);
        double endDecimal = end.getHour() + (end.getMinute() / 60.0);
        if (startDecimal < 8 && !isDayMode) startDecimal += 24;
        if (endDecimal < 8 && !isDayMode) endDecimal += 24;
        if (startDecimal >= viewStartHour && startDecimal < viewEndHour) {

            VBox box = new VBox(2);
            box.setPrefWidth(colWidth - 10);
            double yOffset = (startDecimal - viewStartHour) * HOUR_HEIGHT;
            double height = (endDecimal - startDecimal) * HOUR_HEIGHT;
            int dayIndex = date.getDayOfWeek().getValue() - 1;
            double xOffset = 60 + (dayIndex * colWidth) + 5;

            box.setLayoutX(xOffset);
            box.setLayoutY(yOffset + 8);
            box.setPrefHeight(height);
            if (planTypeObj instanceof CoursePlan) {
                box.getStyleClass().add("event-course");
            } else if (planTypeObj instanceof PermanentPlan) {
                box.getStyleClass().add("event-permanent");
            } else {
                box.getStyleClass().add("event-onetime"); 
            }
            Label nameLbl = new Label(title);
            nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

            String timeText = String.format("%02d:%02d - %02d:%02d", start.getHour(), start.getMinute(), end.getHour(), end.getMinute());
            Label timeLbl = new Label(timeText);
            timeLbl.setStyle("-fx-text-fill: white; -fx-font-size: 10;");

            box.getChildren().addAll(nameLbl, timeLbl);
            eventsOverlay.getChildren().add(box);
        }
    }
    @FXML public void showAddPlanPopup() { overlay.setVisible(true); addPlanPopup.setVisible(true); }
    @FXML public void hidePopup() { overlay.setVisible(false); addPlanPopup.setVisible(false); }

    @FXML
    public void addNewPlan() {
        try {
            String name = inpName.getText();
            String[] startParts = inpStart.getText().split(":");
            String[] endParts = inpEnd.getText().split(":");
            LocalTime startTime = LocalTime.of(Integer.parseInt(startParts[0].trim()), Integer.parseInt(startParts[1].trim()));
            LocalTime endTime = LocalTime.of(Integer.parseInt(endParts[0].trim()), Integer.parseInt(endParts[1].trim()));
            int dayIndex = cmbDay.getSelectionModel().getSelectedIndex() + 1; 
            LocalDate planDate = LocalDate.now().with(DayOfWeek.of(dayIndex));

            String planId = "P" + System.currentTimeMillis();
            Plan newPlan;
            if (rbPerm.isSelected()) {
                newPlan = new PermanentPlan(planId, name, planDate, startTime, endTime);
            } else if (rbOneTime.isSelected()) {
                newPlan = new OneTimePlan(planId, name, planDate, startTime, endTime);
            } else {
                newPlan = new CoursePlan(planId, name, planDate, startTime, endTime);
            }

            User me = SessionManager.getCurrentUser();

            newPlan.setOwnerUserId(me.getUserId());
            me.getCalendar().addPlan(newPlan);

            Database db = Database.getInstance();
            db.savePlan(newPlan);
            db.saveUser(me);

            hidePopup();
            renderCalendar(); 

        } catch (Exception e) {
            System.out.println("HATA: Saat formatı hatalı girildi (örn: 14:00 şeklinde girin)");
            e.printStackTrace();
        }
    }
    @FXML public void goToProfile(ActionEvent event) throws IOException { switchScene(event, "profilePage.fxml"); }
    @FXML public void goToFriend(ActionEvent event) throws IOException { switchScene(event, "friendsPage.fxml"); }
    @FXML public void goToHome(ActionEvent event) throws IOException { switchScene(event, "mainPage.fxml"); }
    @FXML public void goToActivities(ActionEvent event) throws IOException { switchScene(event, "activityPage.fxml"); }

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
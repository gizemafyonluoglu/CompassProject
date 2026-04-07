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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class AdminPanelController {

    @FXML private TilePane requestsContainer;
    @FXML private Pane overlay;
    @FXML private VBox signOutPopup;

    private Database db;

    @FXML
    public void initialize() {
        db = Database.getInstance();
        loadPendingRequests();
    }

    private void loadPendingRequests() {
        if (requestsContainer == null) return;
        requestsContainer.getChildren().clear();

        List<MembershipRequest> pendingRequests = db.getPendingMembershipRequests();

        if (pendingRequests.isEmpty()) {
            Label noReq = new Label("No pending board membership requests.");
            noReq.setStyle("-fx-font-size: 16; -fx-text-fill: #94A3B8;");
            requestsContainer.getChildren().add(noReq);
            return;
        }

        for (MembershipRequest req : pendingRequests) {
            VBox card = createRequestCard(req);
            requestsContainer.getChildren().add(card);
        }
    }

    private VBox createRequestCard(MembershipRequest req) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        card.setPrefWidth(350);

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);

        String displayName = req.getUserId();
        if (displayName != null && !displayName.isEmpty()) {
            User submitter = db.getUserById(displayName);
            if (submitter != null) {
                displayName = submitter.getName() + " " + submitter.getSurname();
            }
        }

        Label nameLbl = new Label("From: " + displayName);
        nameLbl.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLbl = new Label("Pending");
        statusLbl.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 10; -fx-font-size: 12;");

        topBar.getChildren().addAll(nameLbl, spacer, statusLbl);

        HBox fileArea = new HBox(15);
        fileArea.setAlignment(Pos.CENTER_LEFT);
        fileArea.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 10; -fx-padding: 15;");

        ImageView fileIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/compass/demo1/icons/file.png")));
        fileIcon.setFitWidth(30);
        fileIcon.setFitHeight(30);
        fileIcon.setPreserveRatio(true);

        Label fileLbl = new Label("File: " + req.getDocumentPath());
        fileLbl.setStyle("-fx-font-size: 14; -fx-text-fill: #334155;");

        Button viewFileBtn = new Button("View");
        viewFileBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 5 10; -fx-cursor: hand;");
        viewFileBtn.setOnAction(e -> openFile(req));

        fileArea.getChildren().addAll(fileIcon, fileLbl, viewFileBtn);

        HBox btnArea = new HBox(15);
        btnArea.setAlignment(Pos.CENTER);

        Button approveBtn = new Button("✓ Approve");
        approveBtn.setPrefWidth(150);
        approveBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10; -fx-cursor: hand;");
        approveBtn.setOnAction(e -> handleAction(req, "approved"));

        Button rejectBtn = new Button("✕ Reject");
        rejectBtn.setPrefWidth(150);
        rejectBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10; -fx-cursor: hand;");
        rejectBtn.setOnAction(e -> handleAction(req, "rejected"));

        btnArea.getChildren().addAll(approveBtn, rejectBtn);

        card.getChildren().addAll(topBar, fileArea, btnArea);
        return card;
    }

    private void handleAction(MembershipRequest req, String status) {
        db.updateRequestStatus(req.getRequestId(), status);
        if ("approved".equals(status)) {
            db.approveClubBoardMember(req.getUserId(), req.getClubName());
            System.out.println("Successfully approved: " + req.getUserId() + " ID'li kullanıcı " + req.getClubName() + " yöneticisi yapıldı.");
        } else {
            System.out.println("Request declined: " + req.getRequestId());
        }
        loadPendingRequests();
    }

    private void openFile(MembershipRequest req) {
        String base64 = req.getDocumentBase64();
        if (base64 == null || base64.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "No file attached to this request.", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        try {
            byte[] fileBytes = Base64.getDecoder().decode(base64);
            String fileName = req.getDocumentPath();
            File tempFile = new File(System.getProperty("java.io.tmpdir"), fileName);
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(fileBytes);
            }
            java.awt.Desktop.getDesktop().open(tempFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showSignOutPopup() {
        if (overlay != null) overlay.setVisible(true);
        if (signOutPopup != null) signOutPopup.setVisible(true);
    }

    @FXML
    public void hideSignOutPopup() {
        if (overlay != null) overlay.setVisible(false);
        if (signOutPopup != null) signOutPopup.setVisible(false);
    }

    @FXML
    public void handleConfirmSignOut(ActionEvent event) {
        SessionManager.setCurrentUser(null);
        System.out.println("The admin has logged out.");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("loginPage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean wasFullScreen = stage.isFullScreen();
            stage.setScene(new Scene(root));
            stage.setFullScreen(wasFullScreen);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
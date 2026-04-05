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

import java.io.IOException;
import java.util.List;

public class AdminPanelController {

    // FXML Bağlantıları
    @FXML private VBox adminNav; // Sidebar'daki admin kısmı
    @FXML private TilePane requestsContainer; // Kartların dizileceği ızgara

    // Database
    private Database db;

    @FXML
    public void initialize() {
        db = Database.getInstance();
        User me = SessionManager.getCurrentUser();

        // 1. Kendi sidebar'ımızda admin ikonunu aktif et
        if (adminNav != null) {
            adminNav.setVisible(true);
            adminNav.setManaged(true);
            // Şu an admin panelinde olduğumuz için ikona "aktif" stili verebilirsin (CSS ile)
        }

        // 2. Bekleyen Talepleri Yükle
        loadPendingRequests();
    }

    // MongoDB'den "pending" talepleri çeker
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
        Label nameLbl = new Label("Talebi Atan User ID: " + req.getRequestId());
        nameLbl.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

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

        Label fileLbl = new Label("Board Membership File: " + req.getDocumentPath());
        fileLbl.setStyle("-fx-font-size: 14; -fx-text-fill: #334155;");

        fileArea.getChildren().addAll(fileIcon, fileLbl);



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
    // 1. Başvuru formunun durumunu güncelle (Request koleksiyonu)
    db.updateRequestStatus(req.getRequestId(), status);

    // 2. Eğer onaylandıysa kullanıcıyı asıl veritabanında (Users koleksiyonu) güncelle
    if ("approved".equals(status)) {
        db.approveClubBoardMember(req.getUserId(), req.getClubName());
        System.out.println("Başarılı: " + req.getUserId() + " ID'li kullanıcı " + req.getClubName() + " yöneticisi yapıldı.");
    } else {
        System.out.println("Talep reddedildi: " + req.getRequestId());
    }

    // 3. Ekrandaki bekleyenler listesini yenile
    loadPendingRequests();
}


    @FXML public void goToProfile(ActionEvent event) throws IOException { switchScene(event, "profilePage.fxml"); }
    @FXML public void goToFriend(ActionEvent event) throws IOException { switchScene(event, "friendsPage.fxml"); }
    @FXML public void goToHome(ActionEvent event) throws IOException { switchScene(event, "mainPage.fxml"); }
    @FXML public void goToActivities(ActionEvent event) throws IOException { switchScene(event, "activityPage.fxml"); }
    @FXML public void goToCalendar(ActionEvent event) throws IOException { switchScene(event, "activityPage.fxml"); }


    private void switchScene(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 900, 600));
        stage.show();
    }
}

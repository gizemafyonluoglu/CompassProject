
package com.compass.demo1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField loginEmailField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Label loginErrorLabel;

    @FXML private TextField signUpNameField;
    @FXML private TextField signUpSurnameField;
    @FXML private TextField signUpEmailField;
    @FXML private PasswordField signUpPass, signUpPassAgain;
    @FXML private Label signUpErrorLabel;

    @FXML private PasswordField newPass, confirmPass;
    @FXML private Label errorLabel;
    @FXML private TextField forgotPassEmailField;

    @FXML private TextField verificationCodeField;

    @FXML private Pane overlay;
    @FXML private VBox signUpBox, setPasswordBox, forgotPasswordBox, interestsBox, verificationBox;

    private Database db;
    private String currentVerificationCode;
    private boolean isSignUpVerification = false;

    private String pendingForgotPasswordEmail;
    private String pendingSignUpEmail;

    @FXML
    public void initialize() {
        db = Database.getInstance();
    }

    @FXML
    public void handleSignIn(ActionEvent event) {
        String email = loginEmailField.getText() == null ? "" : loginEmailField.getText().trim();
        String password = loginPasswordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showErrorMessage(loginErrorLabel, "Lütfen tüm alanları doldurun!");
            return;
        }

        User user = db.getUser(email);

        if (user != null && user.getPassword().equals(password)) {
            SessionManager.setCurrentUser(user);
            if(user instanceof Admin){
                goToAdminPanel(event);
            }
            else{
                goToMainPage(event);
            }
        } else {
            showErrorMessage(loginErrorLabel, "E-posta veya şifre hatalı!");
        }
    }

    @FXML
    public void handleCreateAccount(ActionEvent event) {
        String name = signUpNameField.getText() == null ? "" : signUpNameField.getText().trim();
        String surname = signUpSurnameField.getText() == null ? "" : signUpSurnameField.getText().trim();
        String email = signUpEmailField.getText() == null ? "" : signUpEmailField.getText().trim();
        String p1 = signUpPass.getText();
        String p2 = signUpPassAgain.getText();

        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || p1.isEmpty() || p2.isEmpty()) {
            showErrorMessage(signUpErrorLabel, "Lütfen tüm alanları doldurun!");
        } else if (!email.contains("@")) {
            showErrorMessage(signUpErrorLabel, "Geçerli bir e-posta girin!");
        } else if (!p1.equals(p2)) {
            showErrorMessage(signUpErrorLabel, "Şifreler uyuşmuyor!");
        } else if (db.getUser(email) != null) {
            showErrorMessage(signUpErrorLabel, "Bu e-posta zaten kayıtlı!");
        } else {
            currentVerificationCode = String.valueOf((int) (Math.random() * 900000) + 100000);
            pendingSignUpEmail = email;

            EmailSender.sendVerificationEmail(email, currentVerificationCode);
            System.out.println("Kayıt doğrulama kodu gönderildi: " + currentVerificationCode);

            isSignUpVerification = true;

            hideAll();
            overlay.setVisible(true);
            verificationBox.setVisible(true);
        }
    }

    @FXML
    public void handleForgotPassword(ActionEvent event) {
        String email = forgotPassEmailField.getText() == null ? "" : forgotPassEmailField.getText().trim();

        if (email.isEmpty()) {
            showErrorMessage(errorLabel, "Lütfen e-posta adresinizi girin.");
            return;
        }

        User user = db.getUser(email);
        if (user != null) {
            currentVerificationCode = String.valueOf((int) (Math.random() * 900000) + 100000);
            pendingForgotPasswordEmail = email;

            EmailSender.sendForgotPassword(email, currentVerificationCode);
            System.out.println("Şifre sıfırlama kodu gönderildi: " + currentVerificationCode);

            isSignUpVerification = false;

            hideAll();
            overlay.setVisible(true);
            verificationBox.setVisible(true);
        } else {
            showErrorMessage(errorLabel, "E-posta bulunamadı.");
        }
    }

    @FXML
    public void handleVerifyCode(ActionEvent event) {
        String enteredCode = verificationCodeField.getText() == null ? "" : verificationCodeField.getText().trim();

        if (enteredCode.equals(currentVerificationCode)) {
            if (isSignUpVerification) {
                String name = signUpNameField.getText() == null ? "" : signUpNameField.getText().trim();
                String surname = signUpSurnameField.getText() == null ? "" : signUpSurnameField.getText().trim();
                String email = pendingSignUpEmail;
                String p1 = signUpPass.getText();

                String newUserId = "U" + System.currentTimeMillis();
                User newUser = new User(newUserId, name, surname, email, p1);
                db.saveUser(newUser);
                SessionManager.setCurrentUser(newUser);

                hideAll();
                goToMainPage(event);
            } else {
                showSetPassword();
            }
        } else {
            showErrorMessage(errorLabel, "Hatalı doğrulama kodu!");
        }
    }

    @FXML
    public void handleUpdatePassword() {
        String p1 = newPass.getText();
        String p2 = confirmPass.getText();
        String email = pendingForgotPasswordEmail;

        if (p1.isEmpty() || p2.isEmpty()) {
            showErrorMessage(errorLabel, "Şifre alanlarını doldurun!");
        } else if (!p1.equals(p2)) {
            showErrorMessage(errorLabel, "Şifreler uyuşmuyor!");
        } else {
            User user = db.getUser(email);
            if (user != null) {
                user.setPassword(p1);
                db.saveUser(user);
                System.out.println("Şifre başarıyla güncellendi.");

                errorLabel.setVisible(false);
                hideAll();
            } else {
                showErrorMessage(errorLabel, "Kullanıcı bulunamadı!");
            }
        }
    }

    private void goToMainPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/compass/demo1/mainPage.fxml"));
            Parent mainRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(mainRoot, 900, 600));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void goToAdminPanel(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/compass/demo1/AdminPanel.fxml"));
            Parent mainRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(mainRoot, 900, 600));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML public void showSignUp() { hideAll(); overlay.setVisible(true); signUpBox.setVisible(true); }
    @FXML public void showForgotPassword() { hideAll(); overlay.setVisible(true); forgotPasswordBox.setVisible(true); }
    @FXML public void showSetPassword() { hideAll(); overlay.setVisible(true); setPasswordBox.setVisible(true); }
    @FXML public void showInterests() { hideAll(); overlay.setVisible(true); interestsBox.setVisible(true); }
    @FXML public void hideInterests() { interestsBox.setVisible(false); overlay.setVisible(false); }

    @FXML
    public void toggleInterest(ActionEvent event) {
        Node node = (Node) event.getSource();
        if (node.getStyleClass().contains("selected")) {
            node.getStyleClass().remove("selected");
        } else {
            node.getStyleClass().add("selected");
        }
    }

    @FXML
    public void hideAll() {
        if (overlay != null) overlay.setVisible(false);
        if (signUpBox != null) signUpBox.setVisible(false);
        if (forgotPasswordBox != null) forgotPasswordBox.setVisible(false);
        if (verificationBox != null) verificationBox.setVisible(false);
        if (interestsBox != null) interestsBox.setVisible(false);
        if (setPasswordBox != null) setPasswordBox.setVisible(false);
        if (verificationCodeField != null) verificationCodeField.clear();
    }

    private void showErrorMessage(Label label, String message) {
        if (label != null) {
            label.setText(message);
            label.setStyle("-fx-text-fill: red;");
            label.setVisible(true);
        }
    }
}
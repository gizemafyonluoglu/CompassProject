
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
import javafx.scene.control.Button;
import java.util.ArrayList;
import java.util.List;

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

    @FXML private Label forgotPassErrorLabel;
    @FXML private Label verifyErrorLabel;

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
            showErrorMessage(loginErrorLabel, "Please fill in all the fields!");
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
            showErrorMessage(loginErrorLabel, "Email or password is incorrect!");
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
            showErrorMessage(signUpErrorLabel, "Please fill in all the fields!");
        } else if (!email.endsWith("@ug.bilkent.edu.tr")) {
            showErrorMessage(signUpErrorLabel, "Only accounts with the username @ug.bilkent.edu.tr can register!");
        } else if (!p1.equals(p2)) {
            showErrorMessage(signUpErrorLabel, "The passwords do not match!");
        } else if (db.getUser(email) != null) {
            showErrorMessage(signUpErrorLabel, "This email is already registered!");
        } else {
            currentVerificationCode = String.valueOf((int) (Math.random() * 900000) + 100000);
            pendingSignUpEmail = email;

            EmailSender.sendVerificationEmail(email, currentVerificationCode);
            System.out.println("Registration verification code sent: " + currentVerificationCode);

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
            showErrorMessage(forgotPassErrorLabel, "Please enter your Bilkent e-mail address.");
            return;
        }

        if (!email.isEmpty() && !email.endsWith("@ug.bilkent.edu.tr")) {
            showErrorMessage(forgotPassErrorLabel, "Enter a valid Bilkent email address!");
            return;
        }

        User user = db.getUser(email);
        if (user != null) {
            currentVerificationCode = String.valueOf((int) (Math.random() * 900000) + 100000);
            pendingForgotPasswordEmail = email;

            EmailSender.sendForgotPassword(email, currentVerificationCode);
            System.out.println("Password reset code sent: " + currentVerificationCode);

            isSignUpVerification = false;

            hideAll();
            overlay.setVisible(true);
            verificationBox.setVisible(true);
        } else {
            showErrorMessage(forgotPassErrorLabel, "Email not found.");
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

                List<Interest> selectedInterests = getSelectedSignUpInterests();
                newUser.setInterests(selectedInterests);

                db.saveUser(newUser);
                SessionManager.setCurrentUser(newUser);

                hideAll();
                goToMainPage(event);
            } else {
                showSetPassword();
            }
        } else {
            showErrorMessage(verifyErrorLabel, "Incorrect verification code!");
        }
    }

    @FXML
    public void handleUpdatePassword() {
        String p1 = newPass.getText();
        String p2 = confirmPass.getText();
        String email = pendingForgotPasswordEmail;

        if (p1.isEmpty() || p2.isEmpty()) {
            showErrorMessage(errorLabel, "Fill in the password fields!");
        } else if (!p1.equals(p2)) {
            showErrorMessage(errorLabel, "The passwords do not match!");
        } else {
            User user = db.getUser(email);
            if (user != null) {
                user.setPassword(p1);
                db.saveUser(user);
                System.out.println("Password successfully updated.");

                errorLabel.setVisible(false);
                hideAll();
            } else {
                showErrorMessage(errorLabel, "User not found!");
            }
        }
    }

    private void goToMainPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/compass/demo1/mainPage.fxml"));
            Parent mainRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            boolean wasFullScreen = stage.isFullScreen();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();

            stage.setScene(new Scene(mainRoot, currentWidth, currentHeight));
            stage.setFullScreen(wasFullScreen);
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
            boolean wasFullScreen = stage.isFullScreen();
            stage.setScene(new Scene(mainRoot));
            stage.setFullScreen(wasFullScreen); 
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML public void showSignUp() { hideAll(); overlay.setVisible(true); signUpBox.setVisible(true); }
    @FXML public void showForgotPassword() { hideAll(); overlay.setVisible(true); forgotPasswordBox.setVisible(true); }
    @FXML public void showSetPassword() { hideAll(); overlay.setVisible(true); setPasswordBox.setVisible(true); }
    @FXML public void showInterests() { hideAll(); overlay.setVisible(true); interestsBox.setVisible(true); }
    @FXML public void hideInterests() {
        if (interestsBox != null) {
            interestsBox.setVisible(false);
        }
        if (signUpBox != null) {
            signUpBox.setVisible(true);
        }
    }

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
        if (loginErrorLabel != null) loginErrorLabel.setVisible(false);
        if (signUpErrorLabel != null) signUpErrorLabel.setVisible(false);
        if (errorLabel != null) errorLabel.setVisible(false);
        if (errorLabel != null) errorLabel.setVisible(false);
    }

    private void showErrorMessage(Label label, String message) {
        if (label != null) {
            label.setText(message);
            label.setStyle("-fx-text-fill: red;");
            label.setVisible(true);
        }
    }
    private List<Interest> getSelectedSignUpInterests() {
        List<Interest> selectedInterests = new ArrayList<>();
        int idCounter = 1;

        if (interestsBox == null) {
            return selectedInterests;
        }

        collectSelectedInterests(interestsBox, selectedInterests, idCounter);
        return selectedInterests;
    }

    private int collectSelectedInterests(Parent parent, List<Interest> selectedInterests, int idCounter) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof Button) {
                Button button = (Button) child;

                if (button.getStyleClass().contains("selected")) {
                    String interestName = button.getText() == null ? "" : button.getText().trim();

                    if (!interestName.isEmpty()) {
                        selectedInterests.add(new Interest("INT" + idCounter, interestName));
                        idCounter++;
                    }
                }
            }

            if (child instanceof Parent) {
                idCounter = collectSelectedInterests((Parent) child, selectedInterests, idCounter);
            }
        }

        return idCounter;
    }
}
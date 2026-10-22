package controller;

import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Model;
import model.User;
import service.Validator;

public class ChangePasswordController {
    @FXML
    private PasswordField currentPassword;
    @FXML
    private PasswordField newPassword;
    @FXML
    private PasswordField confirmPassword;
    @FXML
    private Button saveButton;
    @FXML
    private Button closeButton;
    @FXML
    private Label status;

    private Stage stage;
    private Stage parentStage;
    private Model model;

    public ChangePasswordController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        saveButton.setOnAction(event -> {
            changePassword();
        });

        closeButton.setOnAction(event -> {
            stage.close();
        });
    }

    private void changePassword() {
        // Passwords are used exactly as typed, they are never trimmed
        String currentText = currentPassword.getText();
        String newText = newPassword.getText();
        String confirmText = confirmPassword.getText();
        User currentUser = model.getCurrentUser();

        if (currentText.isEmpty()) {
            showError("Please enter your current password");
            return;
        }

        try {
            boolean currentIsRight = model.getUserDao().passwordMatches(currentUser.getUserId(), currentText);
            if (!currentIsRight) {
                showError("Your current password is not correct");
                return;
            }

            // The new password must follow the same rules as signup
            String errorMessage = Validator.validatePassword(newText);
            if (errorMessage != null) {
                showError(errorMessage);
                return;
            }

            if (!newText.equals(confirmText)) {
                showError("The new passwords do not match");
                return;
            }

            if (newText.equals(currentText)) {
                showError("The new password must be different from your current password");
                return;
            }

            // Every check passed, so now the password is saved
            boolean saved = model.getUserDao().updatePassword(currentUser.getUserId(), newText);
            if (saved) {
                currentPassword.clear();
                newPassword.clear();
                confirmPassword.clear();
                status.setText("Password changed");
                status.setTextFill(Color.GREEN);
            } else {
                showError("Could not change your password");
            }
        } catch (SQLException e) {
            showError("Could not change your password: " + e.getMessage());
        }
    }

    private void showError(String text) {
        status.setText(text);
        status.setTextFill(Color.RED);
    }

    // Shows the window on top of the profile window and waits until it is closed
    public void showStage(Pane root) {
        Scene scene = new Scene(root, 420, 380);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Change password");
        stage.initOwner(parentStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}
package controller;

import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Model;
import model.User;
import service.Validator;

public class ProfileController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField firstName;
    @FXML
    private TextField lastName;
    @FXML
    private Button saveButton;
    @FXML
    private Button closeButton;
    @FXML
    private Label status;

    private Stage stage;
    private Stage parentStage;
    private Model model;

    public ProfileController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        // Fill the boxes with the current details
        User currentUser = model.getCurrentUser();
        usernameField.setText(currentUser.getUsername());
        firstName.setText(currentUser.getFirstName());
        lastName.setText(currentUser.getLastName());

        saveButton.setOnAction(event -> {
            saveProfile();
        });

        closeButton.setOnAction(event -> {
            stage.close();
        });
    }

    private void saveProfile() {
        String firstNameText = firstName.getText().trim();
        String lastNameText = lastName.getText().trim();

        // Same name rules as signup
        String errorMessage = Validator.validateName(firstNameText, "First name");
        if (errorMessage == null) {
            errorMessage = Validator.validateName(lastNameText, "Last name");
        }

        if (errorMessage != null) {
            status.setText(errorMessage);
            status.setTextFill(Color.RED);
            return;
        }

        User currentUser = model.getCurrentUser();
        try {
            boolean saved = model.getUserDao().updateName(currentUser.getUserId(), firstNameText, lastNameText);
            if (!saved) {
                status.setText("Could not save your profile");
                status.setTextFill(Color.RED);
                return;
            }

            // Only change the user in memory after the database update worked
            currentUser.setFirstName(firstNameText);
            currentUser.setLastName(lastNameText);
            firstName.setText(firstNameText);
            lastName.setText(lastNameText);
            status.setText("Profile saved");
            status.setTextFill(Color.GREEN);
        } catch (SQLException e) {
            status.setText("Could not save your profile: " + e.getMessage());
            status.setTextFill(Color.RED);
        }
    }

    // Shows the window on top of the home window and waits until it is closed
    public void showStage(Pane root) {
        Scene scene = new Scene(root, 400, 350);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Edit profile");
        stage.initOwner(parentStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}
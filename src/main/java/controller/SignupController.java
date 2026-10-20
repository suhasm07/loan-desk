package controller;

import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Model;
import model.User;
import service.Validator;

public class SignupController {
	@FXML
	private TextField username;
	@FXML
	private TextField password;
	@FXML
	private TextField firstName;
	@FXML
	private TextField lastName;
	@FXML
	private Button createUser;
	@FXML
	private Button close;
	@FXML
	private Label status;
	
	private Stage stage;
	private Stage parentStage;
	private Model model;
	
	public SignupController(Stage parentStage, Model model) {
		this.stage = new Stage();
		this.parentStage = parentStage;
		this.model = model;
	}

	@FXML
	public void initialize() {
		createUser.setOnAction(event -> {
			// Username and names are trimmed and the password is left exactly as typed
			String usernameText = username.getText().trim();
			String passwordText = password.getText();
			String firstNameText = firstName.getText().trim();
			String lastNameText = lastName.getText().trim();

			// Check each field and keep the first problem we find
			String errorMessage = Validator.validateUsername(usernameText);
			if (errorMessage == null) {
				errorMessage = Validator.validatePassword(passwordText);
			}
			if (errorMessage == null) {
				errorMessage = Validator.validateName(firstNameText, "First name");
			}
			if (errorMessage == null) {
				errorMessage = Validator.validateName(lastNameText, "Last name");
			}

			if (errorMessage != null) {
				status.setText(errorMessage);
				status.setTextFill(Color.RED);
				return;
			}

			try {
				User user = model.getUserDao().createUser(usernameText, passwordText, firstNameText, lastNameText);
				if (user != null) {
					status.setText("Created " + user.getUsername());
					status.setTextFill(Color.GREEN);
					username.clear();
					password.clear();
					firstName.clear();
					lastName.clear();
				} else {
					status.setText("That username is already taken");
					status.setTextFill(Color.RED);
				}
			} catch (SQLException e) {
				status.setText("Could not save the account: " + e.getMessage());
				status.setTextFill(Color.RED);
			}
		});

		close.setOnAction(event -> {
			stage.close();
			parentStage.show();
		});
	}
	
	public void showStage(Pane root) {
		Scene scene = new Scene(root, 500, 400);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setTitle("Sign up");
		stage.show();
	}
}

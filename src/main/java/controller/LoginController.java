package controller;

import java.io.IOException;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Model;
import model.User;

public class LoginController {
	@FXML
	private TextField name;
	@FXML
	private PasswordField password;
	@FXML
	private Label message;
	@FXML
	private Button login;
	@FXML
	private Button signup;

	private Model model;
	private Stage stage;
	
	public LoginController(Stage stage, Model model) {
		this.stage = stage;
		this.model = model;
	}
	
	@FXML
	public void initialize() {		
		login.setOnAction(event -> {
			// Username is trimmed, the password is used exactly as typed
			String usernameText = name.getText().trim();
			String passwordText = password.getText();

			if (usernameText.isEmpty() || passwordText.isEmpty()) {
				showError("Please enter your username and password");
				return;
			}

			try {
				User user = model.getUserDao().getUser(usernameText, passwordText);
				if (user == null) {
					// Stay on the login screen, keep the username, clear the password
					showError("Wrong username or password");
					password.clear();
					return;
				}
				model.setCurrentUser(user);
				openHome();
			} catch (SQLException e) {
				showError("Could not check your login: " + e.getMessage());
			}
		});
		
		signup.setOnAction(event -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SignupView.fxml"));
				
				// Customize controller instance
				SignupController signupController =  new SignupController(stage, model);

				loader.setController(signupController);
				VBox root = loader.load();
				
				signupController.showStage(root);
				
				clearFields();
				
				stage.close();
			} catch (IOException e) {
				showError("Could not open the signup window: " + e.getMessage());
			}});
	}

	// Opens the home window after a successful login
	private void openHome() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeView.fxml"));
			HomeController homeController = new HomeController(stage, model);

			loader.setController(homeController);
			VBox root = loader.load();

			homeController.showStage(root);
			clearFields();
			stage.close();
		} catch (IOException e) {
			showError("Could not open the home window: " + e.getMessage());
		}
	}

	private void showError(String text) {
		message.setText(text);
		message.setTextFill(Color.RED);
	}

	private void clearFields() {
		message.setText("");
		name.clear();
		password.clear();
	}

	
	public void showStage(Pane root) {
		Scene scene = new Scene(root, 500, 300);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setTitle("Welcome");
		stage.show();
	}
}


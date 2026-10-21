package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Model;
import model.User;

import java.io.IOException;

public class HomeController {
	private Model model;
	private Stage stage;
	private Stage parentStage;

	@FXML
	private Label welcomeLabel;
	@FXML
	private Button loansButton;
	@FXML
	private Button exportButton;
	@FXML
	private Button profileButton;
	@FXML
	private Button logoutButton;
	@FXML
	private Label status;

	public HomeController(Stage parentStage, Model model) {
		this.stage = new Stage();
		this.parentStage = parentStage;
		this.model = model;
	}
	
	// Add your code to complete the functionality of the program

	@FXML
	public void initialize() {
		showWelcomeMessage();

		// These screens are built in later steps
		loansButton.setOnAction(event -> {
			status.setText("Loan management is coming soon");
		});

		exportButton.setOnAction(event -> {
			status.setText("Export is coming soon");
		});

		profileButton.setOnAction(event -> {
			openProfile();
		});

		logoutButton.setOnAction(event -> {
			logout();
		});

		// Closing the window with the X also ends the session
		stage.setOnCloseRequest(event -> {
			model.setCurrentUser(null);
		});
	}

	// Shows the logged-in user's first and last name
	private void showWelcomeMessage() {
		User currentUser = model.getCurrentUser();
		welcomeLabel.setText("Welcome, " + currentUser.getFullName());
	}

	// Opens the profile window. It waits here until the profile window is closed and then the welcome message is updated in case the name was changed.
	private void openProfile() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ProfileView.fxml"));
			ProfileController profileController = new ProfileController(stage, model);

			loader.setController(profileController);
			VBox root = loader.load();

			profileController.showStage(root);
			showWelcomeMessage();
			status.setText("");
		} catch (IOException e) {
			status.setText("Could not open the profile window: " + e.getMessage());
		}
	}

	// Clears the session, closes this window and goes back to the login screen
	private void logout() {
		model.setCurrentUser(null);
		stage.close();
		parentStage.show();
	}
	
	
	
	public void showStage(Pane root) {
		Scene scene = new Scene(root, 500, 300);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setTitle("Home");
		stage.show();
	}
}

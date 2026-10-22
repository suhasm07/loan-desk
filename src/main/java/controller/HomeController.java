package controller;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Loan;
import model.Model;
import model.User;
import service.LoanExporter;

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

	@FXML
	public void initialize() {
		showWelcomeMessage();

		loansButton.setOnAction(event -> {
			openLoans();
		});

		exportButton.setOnAction(event -> {
			exportLoans();
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

	// Opens the loans window. It waits here until the loans window is closed.
	private void openLoans() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoansView.fxml"));
			LoansController loansController = new LoansController(stage, model);

			loader.setController(loansController);
			VBox root = loader.load();

			loansController.showStage(root);
			status.setText("");
		} catch (IOException e) {
			showMessage("Could not open the loans window: " + e.getMessage(), Color.RED);
		}
	}

	// Opens the profile window. It waits here until the profile window is closed,
	// then the welcome message is updated in case the name was changed.
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
			showMessage("Could not open the profile window: " + e.getMessage(), Color.RED);
		}
	}

	// Saves all of the user's loans to a text file that the user picks
	private void exportLoans() {
		User currentUser = model.getCurrentUser();
		LocalDate today = LocalDate.now();

		// Read the loans fresh from the database, so the statuses are up to date
		List<Loan> loans;
		try {
			loans = model.getLoanDao().getLoansForUser(currentUser.getUserId());
		} catch (SQLException e) {
			showMessage("Could not read your loans: " + e.getMessage(), Color.RED);
			return;
		}

		String reportText = LoanExporter.buildReport(currentUser, loans, today);

		// The file chooser asks before overwriting an existing file
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Export all loans");
		fileChooser.setInitialFileName("loandesk-loans-" + today + ".txt");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"));
		File chosenFile = fileChooser.showSaveDialog(stage);

		// The user closed the file chooser without saving
		if (chosenFile == null) {
			showMessage("Export cancelled. Nothing was changed.", Color.BLACK);
			return;
		}

		try {
			LoanExporter.writeToFile(chosenFile, reportText);
			// Only shown after the file has really been written
			showMessage("Exported " + loans.size() + " loan(s) to " + chosenFile.getName(), Color.GREEN);
		} catch (IOException e) {
			showMessage("Could not write the file: " + e.getMessage(), Color.RED);
		}
	}

	// Clears the session, closes this window and goes back to the login screen
	private void logout() {
		model.setCurrentUser(null);
		stage.close();
		parentStage.show();
	}

	private void showMessage(String text, Color color) {
		status.setText(text);
		status.setTextFill(color);
	}

	public void showStage(Pane root) {
		Scene scene = new Scene(root, 500, 350);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setTitle("Home");
		stage.show();
	}
}
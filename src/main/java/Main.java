import java.io.IOException;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import model.Model;
import controller.LoginController;

public class Main extends Application {
	private Model model;

	@Override
	public void init() {
		model = new Model();

		// Safety net: if something unexpected goes wrong, the user sees a message instead of nothing happening
		Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
			error.printStackTrace();
			Platform.runLater(() -> {
				Alert alert = new Alert(Alert.AlertType.ERROR, "Something went wrong: " + error.getMessage());
				alert.setHeaderText(null);
				alert.show();
			});
		});
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			model.setup();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));

			// Customize controller instance
			LoginController loginController = new LoginController(primaryStage, model);

			loader.setController(loginController);

			GridPane root = loader.load();

			loginController.showStage(root);
		} catch (IOException | SQLException | RuntimeException e) {
			// The database file is never deleted or replaced, the user is only told what went wrong
			showStartError(primaryStage, e);
		}
	}

	// Shows a small window explaining why the application could not start
	private void showStartError(Stage primaryStage, Exception error) {
		Label errorLabel = new Label("LoanDesk could not start.\n\n" + error.getMessage()
				+ "\n\nYour saved data has not been changed.");
		errorLabel.setWrapText(true);
		errorLabel.setPadding(new Insets(15));

		Scene scene = new Scene(errorLabel, 450, 200);
		primaryStage.setTitle("Error");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	// Called when the application closes, so no user stays logged in
	@Override
	public void stop() {
		model.setCurrentUser(null);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
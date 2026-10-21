package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Loan;
import model.Model;
import model.User;

public class LoansController {
    @FXML
    private TableView<Loan> loanTable;
    @FXML
    private TableColumn<Loan, String> idColumn;
    @FXML
    private TableColumn<Loan, String> codeColumn;
    @FXML
    private TableColumn<Loan, String> nameColumn;
    @FXML
    private TableColumn<Loan, String> borrowerColumn;
    @FXML
    private TableColumn<Loan, String> categoryColumn;
    @FXML
    private TableColumn<Loan, String> loanDateColumn;
    @FXML
    private TableColumn<Loan, String> dueDateColumn;
    @FXML
    private TableColumn<Loan, String> returnDateColumn;
    @FXML
    private TableColumn<Loan, String> statusColumn;
    @FXML
    private TableColumn<Loan, String> noteColumn;
    @FXML
    private Button addButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button closeButton;
    @FXML
    private Label status;

    private Stage stage;
    private Stage parentStage;
    private Model model;

    // The date used to work out the status. It is set again every time the loans are loaded.
    private LocalDate today = LocalDate.now();

    public LoansController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        setupColumns();
        loanTable.setPlaceholder(new Label("You have no loan records yet."));

        addButton.setOnAction(event -> {
            openLoanForm();
        });

        refreshButton.setOnAction(event -> {
            loadLoans();
        });

        closeButton.setOnAction(event -> {
            stage.close();
        });

        loadLoans();
    }

    // Tells each column what text to show for a loan
    private void setupColumns() {
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getLoanId())));
        codeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getItemCode()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getItemName()));
        borrowerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBorrowerName()));
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory().getLabel()));
        loanDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLoanDate().toString()));
        dueDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDueDate().toString()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus(today).getLabel()));

        // A dash is shown when there is no return date or no note
        returnDateColumn.setCellValueFactory(data -> {
            LocalDate returnDate = data.getValue().getReturnDate();
            if (returnDate == null) {
                return new SimpleStringProperty("-");
            }
            return new SimpleStringProperty(returnDate.toString());
        });

        noteColumn.setCellValueFactory(data -> {
            String note = data.getValue().getNote();
            if (note == null || note.trim().isEmpty()) {
                return new SimpleStringProperty("-");
            }
            // Line breaks are replaced with spaces so the note stays on one line in the table
            return new SimpleStringProperty(note.replace("\r", " ").replace("\n", " "));
        });
    }

    // Loads the logged-in user's loans from the database and works out the statuses
    private void loadLoans() {
        today = LocalDate.now();
        User currentUser = model.getCurrentUser();

        try {
            List<Loan> loans = model.getLoanDao().getLoansForUser(currentUser.getUserId());
            loanTable.setItems(FXCollections.observableArrayList(loans));
            status.setText("Showing " + loans.size() + " loan(s)");
            status.setTextFill(Color.BLACK);
        } catch (SQLException e) {
            // The old list stays on screen if loading fails
            status.setText("Could not load your loans: " + e.getMessage());
            status.setTextFill(Color.RED);
        }
    }

    // Opens the add loan form. It waits here until the form is closed.
    private void openLoanForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoanFormView.fxml"));
            LoanFormController formController = new LoanFormController(stage, model);

            loader.setController(formController);
            VBox root = loader.load();

            formController.showStage(root);

            // Reload the table if a loan was saved
            if (formController.isSaved()) {
                loadLoans();
                status.setText("Loan added. " + status.getText());
                status.setTextFill(Color.GREEN);
            }
        } catch (IOException e) {
            status.setText("Could not open the add loan form: " + e.getMessage());
            status.setTextFill(Color.RED);
        }
    }

    // Shows the window on top of the home window and waits until it is closed
    public void showStage(Pane root) {
        Scene scene = new Scene(root, 950, 500);
        stage.setScene(scene);
        stage.setTitle("My loans");
        stage.initOwner(parentStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}
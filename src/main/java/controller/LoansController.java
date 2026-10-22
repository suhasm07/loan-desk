package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
    private Label hintLabel;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button returnButton;
    @FXML
    private Button deleteButton;
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
            openLoanForm(null);
        });

        editButton.setOnAction(event -> {
            editSelectedLoan();
        });

        returnButton.setOnAction(event -> {
            returnSelectedLoan();
        });

        deleteButton.setOnAction(event -> {
            deleteSelectedLoan();
        });

        refreshButton.setOnAction(event -> {
            loadLoans();
        });

        closeButton.setOnAction(event -> {
            stage.close();
        });

        // Every time the user clicks a different row, the buttons are updated
        loanTable.getSelectionModel().selectedItemProperty().addListener((observable, oldLoan, newLoan) -> {
            updateButtons(newLoan);
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

    // Turns the buttons on or off and shows a hint, depending on which loan is selected
    private void updateButtons(Loan selectedLoan) {
        if (selectedLoan == null) {
            editButton.setDisable(true);
            returnButton.setDisable(true);
            deleteButton.setDisable(true);
            hintLabel.setText("Select a loan in the table to edit it, mark it returned or delete it.");
        } else if (selectedLoan.isReturned()) {
            // A returned loan is locked, but it can still be deleted
            editButton.setDisable(true);
            returnButton.setDisable(true);
            deleteButton.setDisable(false);
            hintLabel.setText("This loan has been returned, so it cannot be edited or returned again. "
                    + "It can still be deleted.");
        } else {
            editButton.setDisable(false);
            returnButton.setDisable(false);
            deleteButton.setDisable(false);
            hintLabel.setText("Selected loan " + selectedLoan.getItemCode()
                    + ": you can edit it, mark it returned or delete it.");
        }
    }

    // Loads the logged-in user's loans from the database and works out the statuses
    private void loadLoans() {
        today = LocalDate.now();
        User currentUser = model.getCurrentUser();

        try {
            List<Loan> loans = model.getLoanDao().getLoansForUser(currentUser.getUserId());
            loanTable.setItems(FXCollections.observableArrayList(loans));
            loanTable.getSelectionModel().clearSelection();
            updateButtons(null);
            showMessage("Showing " + loans.size() + " loan(s)", Color.BLACK);
        } catch (SQLException e) {
            // The old list stays on screen if loading fails
            showMessage("Could not load your loans: " + e.getMessage(), Color.RED);
        }
    }

    // Gets the loan the user clicked on. If nothing is selected, a message explains what to do.
    private Loan getSelectedLoan() {
        Loan selectedLoan = loanTable.getSelectionModel().getSelectedItem();
        if (selectedLoan == null) {
            showMessage("Please select a loan in the table first", Color.RED);
        }
        return selectedLoan;
    }

    private void editSelectedLoan() {
        Loan selectedLoan = getSelectedLoan();
        if (selectedLoan == null) {
            return;
        }
        if (selectedLoan.isReturned()) {
            showMessage("This loan has been returned, so it cannot be edited", Color.RED);
            return;
        }
        openLoanForm(selectedLoan);
    }

    private void returnSelectedLoan() {
        Loan selectedLoan = getSelectedLoan();
        if (selectedLoan == null) {
            return;
        }
        if (selectedLoan.isReturned()) {
            showMessage("This loan has already been returned", Color.RED);
            return;
        }

        // Ask first, because a return cannot be undone
        boolean confirmed = askYesNo("Mark " + selectedLoan.getItemCode() + " as returned today?");
        if (!confirmed) {
            return;
        }

        User currentUser = model.getCurrentUser();
        try {
            boolean worked = model.getLoanDao().markReturned(selectedLoan.getLoanId(), currentUser.getUserId(),
                    LocalDate.now());
            if (worked) {
                loadLoans();
                showMessage(selectedLoan.getItemCode() + " marked as returned. " + status.getText(), Color.GREEN);
            } else {
                showMessage("Could not mark the loan as returned", Color.RED);
            }
        } catch (SQLException e) {
            showMessage("Could not mark the loan as returned: " + e.getMessage(), Color.RED);
        }
    }

    private void deleteSelectedLoan() {
        Loan selectedLoan = getSelectedLoan();
        if (selectedLoan == null) {
            return;
        }

        // Ask first, because a deleted loan cannot be brought back
        boolean confirmed = askYesNo("Delete the loan for " + selectedLoan.getItemCode() + " ("
                + selectedLoan.getItemName() + ")? This cannot be undone.");
        if (!confirmed) {
            return;
        }

        User currentUser = model.getCurrentUser();
        try {
            boolean worked = model.getLoanDao().deleteLoan(selectedLoan.getLoanId(), currentUser.getUserId());
            if (worked) {
                loadLoans();
                showMessage("Loan deleted. " + status.getText(), Color.GREEN);
            } else {
                showMessage("Could not delete the loan", Color.RED);
            }
        } catch (SQLException e) {
            showMessage("Could not delete the loan: " + e.getMessage(), Color.RED);
        }
    }

    // Shows a Yes / No question and gives back true if the user clicked Yes
    private boolean askYesNo(String question) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, question, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.initOwner(stage);
        Optional<ButtonType> answer = alert.showAndWait();
        return answer.isPresent() && answer.get() == ButtonType.YES;
    }

    // Opens the loan form. Pass null to add a new loan, or a loan to edit it.
    // It waits here until the form is closed.
    private void openLoanForm(Loan loanToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoanFormView.fxml"));
            LoanFormController formController = new LoanFormController(stage, model, loanToEdit);

            loader.setController(formController);
            VBox root = loader.load();

            formController.showStage(root);

            // Reload the table if the loan was saved
            if (formController.isSaved()) {
                loadLoans();
                if (loanToEdit == null) {
                    showMessage("Loan added. " + status.getText(), Color.GREEN);
                } else {
                    showMessage("Loan updated. " + status.getText(), Color.GREEN);
                }
            }
        } catch (IOException e) {
            showMessage("Could not open the loan form: " + e.getMessage(), Color.RED);
        }
    }

    private void showMessage(String text, Color color) {
        status.setText(text);
        status.setTextFill(color);
    }

    // Shows the window on top of the home window and waits until it is closed
    public void showStage(Pane root) {
        Scene scene = new Scene(root, 950, 520);
        stage.setScene(scene);
        stage.setTitle("My loans");
        stage.initOwner(parentStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}
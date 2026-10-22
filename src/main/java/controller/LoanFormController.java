package controller;

import java.sql.SQLException;
import java.time.LocalDate;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Category;
import model.Loan;
import model.Model;
import model.User;
import service.Validator;

public class LoanFormController {
    @FXML
    private Label loanDateLabel;
    @FXML
    private TextField itemCode;
    @FXML
    private TextField itemName;
    @FXML
    private TextField borrowerName;
    @FXML
    private ComboBox<Category> categoryBox;
    @FXML
    private DatePicker dueDatePicker;
    @FXML
    private TextArea noteArea;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label status;

    private Stage stage;
    private Stage parentStage;
    private Model model;

    // This is the loan being edited. It stays null when a new loan is being added.
    private Loan loanToEdit;

    // The loan date cannot be changed. It is today for a new loan,
    // or the original loan date when editing.
    private LocalDate loanDate;

    // The loans screen checks this after the form closes
    private boolean saved = false;

    // Used for adding a new loan
    public LoanFormController(Stage parentStage, Model model) {
        this(parentStage, model, null);
    }

    // Used for editing an existing loan
    public LoanFormController(Stage parentStage, Model model, Loan loanToEdit) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
        this.loanToEdit = loanToEdit;

        if (loanToEdit == null) {
            this.loanDate = LocalDate.now();
        } else {
            this.loanDate = loanToEdit.getLoanDate();
        }
    }

    @FXML
    public void initialize() {
        loanDateLabel.setText("Loan date (cannot be changed): " + loanDate);
        categoryBox.getItems().setAll(Category.values());

        // When editing, start with the loan's current details in the boxes
        if (loanToEdit != null) {
            itemCode.setText(loanToEdit.getItemCode());
            itemName.setText(loanToEdit.getItemName());
            borrowerName.setText(loanToEdit.getBorrowerName());
            categoryBox.setValue(loanToEdit.getCategory());
            dueDatePicker.setValue(loanToEdit.getDueDate());
            noteArea.setText(loanToEdit.getNote());
        }

        saveButton.setOnAction(event -> {
            saveLoan();
        });

        cancelButton.setOnAction(event -> {
            stage.close();
        });
    }

    public boolean isSaved() {
        return saved;
    }

    private void saveLoan() {
        // Read what the user typed. The text fields are trimmed, the note is kept as typed.
        String newCode = Validator.cleanItemCode(itemCode.getText());
        String newName = itemName.getText().trim();
        String newBorrower = borrowerName.getText().trim();
        Category newCategory = categoryBox.getValue();
        LocalDate newDueDate = dueDatePicker.getValue();
        String newNote = noteArea.getText();

        // Check each field and keep the first problem we find
        String errorMessage = Validator.validateItemCode(newCode);
        if (errorMessage == null) {
            errorMessage = Validator.validateItemName(newName);
        }
        if (errorMessage == null) {
            errorMessage = Validator.validateBorrowerName(newBorrower);
        }
        if (errorMessage == null) {
            errorMessage = Validator.validateCategory(newCategory);
        }
        if (errorMessage == null) {
            errorMessage = Validator.validateDueDate(newDueDate, loanDate);
        }
        if (errorMessage == null) {
            errorMessage = Validator.validateNote(newNote);
        }

        if (errorMessage != null) {
            showError(errorMessage);
            return;
        }

        User currentUser = model.getCurrentUser();
        try {
            // When editing, the loan's own ID is ignored so it is not a duplicate of itself
            int ignoreLoanId = 0;
            if (loanToEdit != null) {
                ignoreLoanId = loanToEdit.getLoanId();
            }

            // The same user cannot lend the same item twice at the same time
            boolean alreadyOut = model.getLoanDao().hasOutstandingLoan(currentUser.getUserId(), newCode, ignoreLoanId);
            if (alreadyOut) {
                showError("You already have an unreturned loan for item code " + newCode);
                return;
            }

            if (loanToEdit == null) {
                addNewLoan(currentUser, newCode, newName, newBorrower, newCategory, newDueDate, newNote);
            } else {
                updateExistingLoan(currentUser, newCode, newName, newBorrower, newCategory, newDueDate, newNote);
            }
        } catch (SQLException e) {
            showError("Could not save the loan: " + e.getMessage());
        }
    }

    // A new loan has no ID yet and no return date
    private void addNewLoan(User currentUser, String newCode, String newName, String newBorrower,
                            Category newCategory, LocalDate newDueDate, String newNote) throws SQLException {
        Loan newLoan = new Loan(0, currentUser.getUserId(), newCode, newName, newBorrower,
                newCategory, loanDate, newDueDate, null, newNote);
        model.getLoanDao().addLoan(newLoan);

        // Only reached when the database save worked
        saved = true;
        stage.close();
    }

    // The updated loan is a copy, so the loan in the table is not changed unless the save works
    private void updateExistingLoan(User currentUser, String newCode, String newName, String newBorrower,
                                    Category newCategory, LocalDate newDueDate, String newNote) throws SQLException {
        Loan updatedLoan = new Loan(loanToEdit.getLoanId(), currentUser.getUserId(), newCode, newName,
                newBorrower, newCategory, loanDate, newDueDate, null, newNote);
        boolean worked = model.getLoanDao().updateLoan(updatedLoan);

        if (!worked) {
            showError("Could not update the loan. It may already have been returned.");
            return;
        }

        saved = true;
        stage.close();
    }

    private void showError(String text) {
        status.setText(text);
        status.setTextFill(Color.RED);
    }

    // Shows the form on top of the loans window and waits until it is closed
    public void showStage(Pane root) {
        Scene scene = new Scene(root, 450, 600);
        stage.setScene(scene);
        stage.setResizable(false);
        if (loanToEdit == null) {
            stage.setTitle("Add loan");
        } else {
            stage.setTitle("Edit loan");
        }
        stage.initOwner(parentStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}
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

    // The loan date is always today, and the user cannot change it
    private LocalDate today = LocalDate.now();

    // The loans screen checks this after the form closes
    private boolean saved = false;

    public LoanFormController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        loanDateLabel.setText("Loan date (set automatically): " + today);
        categoryBox.getItems().setAll(Category.values());

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
        String itemCodeText = Validator.cleanItemCode(itemCode.getText());
        String itemNameText = itemName.getText().trim();
        String borrowerText = borrowerName.getText().trim();
        Category category = categoryBox.getValue();
        LocalDate dueDate = dueDatePicker.getValue();
        String noteText = noteArea.getText();

        // Check each field and keep the first problem we find
        String errorMessage = Validator.validateItemCode(itemCodeText);
        if (errorMessage == null) {
            errorMessage = Validator.validateItemName(itemNameText);
        }
        if (errorMessage == null) {
            errorMessage = Validator.validateBorrowerName(borrowerText);
        }
        if (errorMessage == null) {
            errorMessage = Validator.validateCategory(category);
        }
        if (errorMessage == null) {
            errorMessage = Validator.validateDueDate(dueDate, today);
        }
        if (errorMessage == null) {
            errorMessage = Validator.validateNote(noteText);
        }

        if (errorMessage != null) {
            showError(errorMessage);
            return;
        }

        User currentUser = model.getCurrentUser();
        try {
            // The same user cannot lend the same item twice at the same time
            boolean alreadyOut = model.getLoanDao().hasOutstandingLoan(currentUser.getUserId(), itemCodeText, 0);
            if (alreadyOut) {
                showError("You already have an unreturned loan for item code " + itemCodeText);
                return;
            }

            // A new loan has no ID yet and no return date
            Loan loan = new Loan(0, currentUser.getUserId(), itemCodeText, itemNameText, borrowerText,
                    category, today, dueDate, null, noteText);
            model.getLoanDao().addLoan(loan);

            // Only reached when the database save worked
            saved = true;
            stage.close();
        } catch (SQLException e) {
            showError("Could not save the loan: " + e.getMessage());
        }
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
        stage.setTitle("Add loan");
        stage.initOwner(parentStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}
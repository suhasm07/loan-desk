package model;

import java.time.LocalDate;


// One loan record. The status is never stored. It is worked out from the dates.

public class Loan {
    private int loanId;
    private int userId;
    private String itemCode;
    private String itemName;
    private String borrowerName;
    private Category category;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate; // stays null until the item is returned
    private String note;

    public Loan() {
    }

    // Used when loading a loan from the database
    public Loan(int loanId, int userId, String itemCode, String itemName, String borrowerName,
                Category category, LocalDate loanDate, LocalDate dueDate, LocalDate returnDate, String note) {
        this.loanId = loanId;
        this.userId = userId;
        setItemCode(itemCode);
        this.itemName = itemName;
        this.borrowerName = borrowerName;
        this.category = category;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.note = note;
    }

    // getters and setters
    public int getLoanId() {
        return loanId;
    }

    public int getUserId() {
        return userId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public Category getCategory() {
        return category;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public String getNote() {
        return note;
    }

    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    // Trims the extra spaces and converts to uppercase so that everything is saved in uppercase
    public void setItemCode(String itemCode) {
        if (itemCode == null) {
            this.itemCode = null;
        } else {
            this.itemCode = itemCode.trim().toUpperCase();
        }
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isReturned() {
        return returnDate != null;
    }

    public LoanStatus getStatus(LocalDate today) {
        if (returnDate != null) {   // if the return date is not null, it means that it is returned
            return LoanStatus.RETURNED;
        }
        if (today.isAfter(dueDate)) {   // return OVERDUE is the due date was before today
            return LoanStatus.OVERDUE;
        }
        return LoanStatus.ON_LOAN;
    }
}
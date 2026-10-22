package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import model.Category;
import model.Loan;

public class LoanDaoImpl implements LoanDao {
    private final String TABLE_NAME = "loans";

    public LoanDaoImpl() {
    }

    @Override
    public void setup() throws SQLException {
        try (Connection connection = Database.getConnection();
             Statement stmt = connection.createStatement();) {
            String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + "loan_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "user_id INTEGER NOT NULL, "
                    + "item_code TEXT NOT NULL, "
                    + "item_name TEXT NOT NULL, "
                    + "borrower_name TEXT NOT NULL, "
                    + "category TEXT NOT NULL, "
                    + "loan_date TEXT NOT NULL, "
                    + "due_date TEXT NOT NULL, "
                    + "return_date TEXT, "
                    + "note TEXT NOT NULL DEFAULT '', "
                    + "FOREIGN KEY (user_id) REFERENCES users(user_id))";
            stmt.executeUpdate(sql);
        }
    }

    // Saves a new loan and gives back the same loan with its new ID
    @Override
    public Loan addLoan(Loan loan) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME
                + " (user_id, item_code, item_name, borrower_name, category, loan_date, due_date, return_date, note) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
            stmt.setInt(1, loan.getUserId());
            stmt.setString(2, loan.getItemCode());
            stmt.setString(3, loan.getItemName());
            stmt.setString(4, loan.getBorrowerName());
            stmt.setString(5, loan.getCategory().name());
            stmt.setString(6, loan.getLoanDate().toString());
            stmt.setString(7, loan.getDueDate().toString());

            // The return date is empty until the item is returned
            if (loan.getReturnDate() == null) {
                stmt.setString(8, null);
            } else {
                stmt.setString(8, loan.getReturnDate().toString());
            }

            stmt.setString(9, loan.getNote());
            stmt.executeUpdate();

            // Read back the ID the database made
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    loan.setLoanId(keys.getInt(1));
                }
            }
            return loan;
        }
    }

    // Gets all the loans of one user, newest loan date first, then highest loan ID first
    @Override
    public List<Loan> getLoansForUser(int userId) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE user_id = ? "
                + "ORDER BY loan_date DESC, loan_id DESC";
        List<Loan> loans = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    loans.add(makeLoan(rs));
                }
            }
        }
        return loans;
    }

    // Checks if this user already has an unreturned loan with this item code. ignoreLoanId is the loan being edited (use 0 when creating a new loan),
    // so a loan is never counted as a duplicate of itself.
    @Override
    public boolean hasOutstandingLoan(int userId, String itemCode, int ignoreLoanId) throws SQLException {
        String sql = "SELECT loan_id FROM " + TABLE_NAME
                + " WHERE user_id = ? AND item_code = ? AND return_date IS NULL AND loan_id <> ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);) {
            stmt.setInt(1, userId);
            stmt.setString(2, itemCode);
            stmt.setInt(3, ignoreLoanId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Changes the editable details of a loan. The loan ID, owner and loan date are never changed here.
    // The WHERE part only matches the owner's own loan that has not been returned yet.
    @Override
    public boolean updateLoan(Loan loan) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME
                + " SET item_code = ?, item_name = ?, borrower_name = ?, category = ?, due_date = ?, note = ? "
                + "WHERE loan_id = ? AND user_id = ? AND return_date IS NULL";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);) {
            stmt.setString(1, loan.getItemCode());
            stmt.setString(2, loan.getItemName());
            stmt.setString(3, loan.getBorrowerName());
            stmt.setString(4, loan.getCategory().name());
            stmt.setString(5, loan.getDueDate().toString());
            stmt.setString(6, loan.getNote());
            stmt.setInt(7, loan.getLoanId());
            stmt.setInt(8, loan.getUserId());

            int rowsChanged = stmt.executeUpdate();
            return rowsChanged == 1;
        }
    }

    // Records the return date. Only works for the owner's own loan that is still out,
    // so a loan cannot be returned twice.
    @Override
    public boolean markReturned(int loanId, int userId, LocalDate returnDate) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET return_date = ? "
                + "WHERE loan_id = ? AND user_id = ? AND return_date IS NULL";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);) {
            stmt.setString(1, returnDate.toString());
            stmt.setInt(2, loanId);
            stmt.setInt(3, userId);

            int rowsChanged = stmt.executeUpdate();
            return rowsChanged == 1;
        }
    }

    // Turns one row of the result into a Loan object
    private Loan makeLoan(ResultSet rs) throws SQLException {
        int loanId = rs.getInt("loan_id");
        int userId = rs.getInt("user_id");
        String itemCode = rs.getString("item_code");
        String itemName = rs.getString("item_name");
        String borrowerName = rs.getString("borrower_name");
        Category category = Category.valueOf(rs.getString("category"));
        LocalDate loanDate = LocalDate.parse(rs.getString("loan_date"));
        LocalDate dueDate = LocalDate.parse(rs.getString("due_date"));
        String note = rs.getString("note");

        // An empty return date comes back as null
        LocalDate returnDate = null;
        String returnDateText = rs.getString("return_date");
        if (returnDateText != null) {
            returnDate = LocalDate.parse(returnDateText);
        }

        return new Loan(loanId, userId, itemCode, itemName, borrowerName, category, loanDate, dueDate,
                returnDate, note);
    }
}
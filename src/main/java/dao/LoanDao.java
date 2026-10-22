package dao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import model.Loan;

/**
 * Handles saving and loading loans.
 * Every method that reads loans takes the user ID, so a user only gets their own loans.
 */
public interface LoanDao {
    void setup() throws SQLException;
    Loan addLoan(Loan loan) throws SQLException;
    List<Loan> getLoansForUser(int userId) throws SQLException;
    boolean hasOutstandingLoan(int userId, String itemCode, int ignoreLoanId) throws SQLException;
    boolean updateLoan(Loan loan) throws SQLException;
    boolean markReturned(int loanId, int userId, LocalDate returnDate) throws SQLException;
}
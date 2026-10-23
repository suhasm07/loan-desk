import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dao.Database;
import dao.LoanDao;
import dao.LoanDaoImpl;
import dao.UserDao;
import dao.UserDaoImpl;
import model.Category;
import model.Loan;
import model.LoanStatus;
import model.User;

public class LoanLogicTest {
    // JUnit makes a new empty folder for every test and deletes it afterwards
    @TempDir
    Path tempFolder;

    private LoanDao loanDao;
    private User alex;
    private User priya;

    @BeforeEach
    void setUp() throws SQLException {
        // Use a temporary database file, never the real one
        Database.useTestDatabase("jdbc:sqlite:" + tempFolder.resolve("test.db"));
        UserDao userDao = new UserDaoImpl();
        loanDao = new LoanDaoImpl();
        userDao.setup();
        loanDao.setup();

        alex = userDao.createUser("Alex_1", "Abcdef1!", "Alex", "Smith");
        priya = userDao.createUser("Priya_2", "Abcdef1!", "Priya", "Patel");
    }

    @AfterEach
    void tearDown() {
        Database.useDefaultDatabase();
    }

    // Makes a loan for a user with fixed dates, not saved yet
    private Loan makeLoan(User owner, String itemCode, LocalDate loanDate) {
        return new Loan(0, owner.getUserId(), itemCode, "Cordless drill", "Sam", Category.TOOLS,
                loanDate, loanDate.plusDays(7), null, "");
    }

    // Scenario 7 (boundary): due today is still on loan, the next day it is overdue
    @Test
    void loanDueTodayIsOnLoanAndOverdueTheNextDay() {
        Loan loan = new Loan(1, 1, "DRILL-01", "Cordless drill", "Sam", Category.TOOLS,
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 12), null, "");

        assertEquals(LoanStatus.ON_LOAN, loan.getStatus(LocalDate.of(2026, 10, 12)));
        assertEquals(LoanStatus.OVERDUE, loan.getStatus(LocalDate.of(2026, 10, 13)));
    }

    // Scenario 8 (invalid): the same item cannot be lent twice at the same time
    @Test
    void sameItemCodeCannotBeLentTwiceAtOnce() throws SQLException {
        Loan saved = loanDao.addLoan(makeLoan(alex, "DRILL-01", LocalDate.of(2026, 10, 10)));

        // Blocked for the same user
        assertTrue(loanDao.hasOutstandingLoan(alex.getUserId(), "DRILL-01", 0));
        // A different code is a different item
        assertFalse(loanDao.hasOutstandingLoan(alex.getUserId(), "DRILL-02", 0));
        // Another user can use the same code in their own register
        assertFalse(loanDao.hasOutstandingLoan(priya.getUserId(), "DRILL-01", 0));
        // When editing, the loan is not a duplicate of itself
        assertFalse(loanDao.hasOutstandingLoan(alex.getUserId(), "DRILL-01", saved.getLoanId()));
    }

    // Scenario 9 (valid): a loan is returned once, stays in the history,
    // and the same code can be lent again as a new loan
    @Test
    void returnedLoanStaysInHistoryAndCodeCanBeLentAgain() throws SQLException {
        Loan firstLoan = loanDao.addLoan(makeLoan(alex, "DRILL-01", LocalDate.of(2026, 10, 10)));

        assertTrue(loanDao.markReturned(firstLoan.getLoanId(), alex.getUserId(), LocalDate.of(2026, 10, 13)));
        // A second return is refused, and the first return date is kept
        assertFalse(loanDao.markReturned(firstLoan.getLoanId(), alex.getUserId(), LocalDate.of(2026, 10, 14)));

        // The code is free again
        assertFalse(loanDao.hasOutstandingLoan(alex.getUserId(), "DRILL-01", 0));
        Loan secondLoan = loanDao.addLoan(makeLoan(alex, "DRILL-01", LocalDate.of(2026, 10, 14)));
        assertTrue(secondLoan.getLoanId() != firstLoan.getLoanId());

        // Both loans are in the history. The newest loan date comes first.
        List<Loan> alexLoans = loanDao.getLoansForUser(alex.getUserId());
        assertEquals(2, alexLoans.size());
        assertEquals(secondLoan.getLoanId(), alexLoans.get(0).getLoanId());
        assertEquals(LocalDate.of(2026, 10, 13), alexLoans.get(1).getReturnDate());
    }

    // Scenario 10 (invalid): another user cannot delete, return or edit my loan
    @Test
    void anotherUserCannotDeleteReturnOrEditMyLoan() throws SQLException {
        Loan saved = loanDao.addLoan(makeLoan(alex, "DRILL-01", LocalDate.of(2026, 10, 10)));

        assertFalse(loanDao.deleteLoan(saved.getLoanId(), priya.getUserId()));
        assertFalse(loanDao.markReturned(saved.getLoanId(), priya.getUserId(), LocalDate.of(2026, 10, 13)));

        Loan attempt = new Loan(saved.getLoanId(), priya.getUserId(), "HACKED", "Hacked", "Hacker",
                Category.OTHER, LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 20), null, "");
        assertFalse(loanDao.updateLoan(attempt));

        // The loan is still there and unchanged
        List<Loan> alexLoans = loanDao.getLoansForUser(alex.getUserId());
        assertEquals(1, alexLoans.size());
        assertEquals("DRILL-01", alexLoans.get(0).getItemCode());
        assertNull(alexLoans.get(0).getReturnDate());

        // The other user does not see it at all
        assertEquals(0, loanDao.getLoansForUser(priya.getUserId()).size());
    }
}
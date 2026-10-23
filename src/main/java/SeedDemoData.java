import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;

import dao.Database;
import dao.LoanDao;
import dao.LoanDaoImpl;
import dao.UserDao;
import dao.UserDaoImpl;
import model.Category;
import model.Loan;
import model.User;

/**
 * DEMO DATA ONLY. The normal application never runs this.
 *
 * It makes a separate database, data/demo.db, with two made-up users and some loans
 * with older dates, so that Overdue can be shown without waiting for another day.
 * The loan dates are worked out from today's date, so there is always an overdue loan.
 *
 * Demo logins (fictional):  alex_demo / Demo_pass1   and   priya_demo / Demo_pass1
 */
public class SeedDemoData {
    private static final String DEMO_FILE = "data/demo.db";
    private static final String DEMO_PASSWORD = "Demo_pass1";

    public static void main(String[] args) {
        // Never overwrite an existing demo database
        if (Files.exists(Paths.get(DEMO_FILE))) {
            System.out.println(DEMO_FILE + " already exists, so nothing was changed.");
            System.out.println("Delete that file first if you want to make the demo data again.");
            return;
        }

        // Only the demo file is used here, never data/loandesk.db
        Database.useTestDatabase("jdbc:sqlite:" + DEMO_FILE);

        try {
            UserDao userDao = new UserDaoImpl();
            LoanDao loanDao = new LoanDaoImpl();
            userDao.setup();
            loanDao.setup();

            User alex = userDao.createUser("alex_demo", DEMO_PASSWORD, "Alex", "Demo");
            User priya = userDao.createUser("priya_demo", DEMO_PASSWORD, "Priya", "Demo");

            LocalDate today = LocalDate.now();

            // Alex: one overdue, one due today, one on loan, one returned late, one returned early
            saveLoan(loanDao, alex, "DRILL-01", "Cordless drill", "Sam", Category.TOOLS,
                    today.minusDays(20), today.minusDays(10), null, "");
            saveLoan(loanDao, alex, "CAM-02", "Camera", "Priya", Category.ELECTRONICS,
                    today.minusDays(5), today, null, "Due back today");
            saveLoan(loanDao, alex, "TENT-03", "Family tent", "Jo", Category.OUTDOOR,
                    today.minusDays(3), today.plusDays(11), null, "Borrowed for the club trip.\nPlease return the pegs too.");
            saveLoan(loanDao, alex, "LADDER-04", "Step ladder", "Mia", Category.TOOLS,
                    today.minusDays(30), today.minusDays(23), today.minusDays(20), "Returned late");
            saveLoan(loanDao, alex, "STOVE-05", "Camping stove", "Lee", Category.OUTDOOR,
                    today.minusDays(15), today.plusDays(5), today.minusDays(12), "Returned early");

            // Priya: uses the same item code as Alex, which is allowed for a different user
            saveLoan(loanDao, priya, "DRILL-01", "Hammer drill", "Tom", Category.TOOLS,
                    today.minusDays(12), today.minusDays(2), null, "");
            saveLoan(loanDao, priya, "KAYAK-02", "Kayak", "Ben", Category.OUTDOOR,
                    today.minusDays(1), today.plusDays(6), null, "");

            System.out.println("Demo database made: " + DEMO_FILE);
            System.out.println("Log in with alex_demo or priya_demo, password " + DEMO_PASSWORD);
        } catch (SQLException e) {
            System.out.println("Could not make the demo data: " + e.getMessage());
            System.out.println("Delete " + DEMO_FILE + " and run this again.");
        }
    }

    // Saves one loan with the dates given, using the same DAO as the real application
    private static void saveLoan(LoanDao loanDao, User owner, String itemCode, String itemName,
                                 String borrowerName, Category category, LocalDate loanDate, LocalDate dueDate,
                                 LocalDate returnDate, String note) throws SQLException {
        Loan loan = new Loan(0, owner.getUserId(), itemCode, itemName, borrowerName, category,
                loanDate, dueDate, returnDate, note);
        loanDao.addLoan(loan);
    }
}
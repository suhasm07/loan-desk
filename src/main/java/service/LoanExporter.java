package service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

import model.Loan;
import model.User;

/**
 * Builds the text for the export file and writes it to disk.
 * There is no screen code in here, so it can be tested on its own.
 */
public class LoanExporter {
    private static final String NEW_LINE = System.lineSeparator();

    // Makes the whole export text: a header, then every loan in the order it was given
    public static String buildReport(User user, List<Loan> loans, LocalDate today) {
        StringBuilder report = new StringBuilder();
        report.append("LoanDesk - loan export").append(NEW_LINE);
        report.append("======================").append(NEW_LINE);
        report.append("Username: ").append(user.getUsername()).append(NEW_LINE);
        report.append("Name: ").append(user.getFullName()).append(NEW_LINE);
        report.append("Export date: ").append(today).append(NEW_LINE);
        report.append(NEW_LINE);

        if (loans.isEmpty()) {
            report.append("There are no loan records.").append(NEW_LINE);
            return report.toString();
        }

        report.append("Total loans: ").append(loans.size()).append(NEW_LINE);
        report.append(NEW_LINE);

        for (Loan loan : loans) {
            addLoanToReport(report, loan, today);
        }
        return report.toString();
    }

    // Adds one loan to the report
    private static void addLoanToReport(StringBuilder report, Loan loan, LocalDate today) {
        // A dash is used when there is no return date or no note
        String returnDateText = "-";
        if (loan.getReturnDate() != null) {
            returnDateText = loan.getReturnDate().toString();
        }

        String noteText = "-";
        if (loan.getNote() != null && !loan.getNote().trim().isEmpty()) {
            // Keep the line breaks of the note, and line the next lines up under the first one
            noteText = loan.getNote().replace("\r\n", "\n").replace("\r", "\n")
                    .replace("\n", NEW_LINE + "              ");
        }

        report.append("Loan ID:      ").append(loan.getLoanId()).append(NEW_LINE);
        report.append("Item code:    ").append(loan.getItemCode()).append(NEW_LINE);
        report.append("Item name:    ").append(loan.getItemName()).append(NEW_LINE);
        report.append("Borrower:     ").append(loan.getBorrowerName()).append(NEW_LINE);
        report.append("Category:     ").append(loan.getCategory().getLabel()).append(NEW_LINE);
        report.append("Loan date:    ").append(loan.getLoanDate()).append(NEW_LINE);
        report.append("Due date:     ").append(loan.getDueDate()).append(NEW_LINE);
        report.append("Return date:  ").append(returnDateText).append(NEW_LINE);
        report.append("Status:       ").append(loan.getStatus(today).getLabel()).append(NEW_LINE);
        report.append("Note:         ").append(noteText).append(NEW_LINE);
        report.append("--------------------------------------------").append(NEW_LINE);
    }

    // Writes the text to the chosen file as UTF-8. The file is closed by Files.writeString.
    public static void writeToFile(File file, String reportText) throws IOException {
        Files.writeString(file.toPath(), reportText, StandardCharsets.UTF_8);
    }
}
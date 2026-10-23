import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import service.Validator;

public class ValidatorTest {

    // Scenario 1 (valid): spaces and capital letters in a username do not matter
    @Test
    void usernameIsTrimmedAndNotCaseSensitive() {
        assertNull(Validator.validateUsername("  Alex_1  "));
        assertEquals("alex_1", Validator.cleanUsername("  Alex_1  "));
        assertEquals(Validator.cleanUsername("Alex_1"), Validator.cleanUsername("alex_1"));
    }

    // Scenario 2 (invalid): only letters, digits and underscores are allowed
    @Test
    void usernameWithSpaceOrSymbolIsRejected() {
        assertNotNull(Validator.validateUsername("alex 1"));
        assertNotNull(Validator.validateUsername("alex-1"));
        assertNotNull(Validator.validateUsername("alex!"));
    }

    // Scenario 3 (boundary): a username must be 3 to 20 characters
    @Test
    void usernameLengthBoundaries() {
        assertNotNull(Validator.validateUsername("ab"));
        assertNull(Validator.validateUsername("abc"));
        assertNull(Validator.validateUsername("a".repeat(20)));
        assertNotNull(Validator.validateUsername("a".repeat(21)));
    }

    // Scenario 4 (invalid): a password needs all four kinds of characters
    @Test
    void passwordMissingARequiredTypeIsRejected() {
        assertNull(Validator.validatePassword("Abcdef1!"));
        assertNotNull(Validator.validatePassword("abcdef1!"));   // no uppercase letter
        assertNotNull(Validator.validatePassword("ABCDEF1!"));   // no lowercase letter
        assertNotNull(Validator.validatePassword("Abcdefg!"));   // no digit
        assertNotNull(Validator.validatePassword("Abcdefg1"));   // no symbol
    }

    // Scenario 5 (boundary): an item code can be at most 20 characters
    @Test
    void itemCodeLengthBoundary() {
        assertNull(Validator.validateItemCode("A".repeat(20)));
        assertNotNull(Validator.validateItemCode("A".repeat(21)));
        assertNotNull(Validator.validateItemCode("   "));
    }

    // Scenario 6 (boundary): a due date can be the loan date but not the day before
    @Test
    void dueDateCanBeTheLoanDateButNotEarlier() {
        LocalDate loanDate = LocalDate.of(2026, 10, 12);

        assertNull(Validator.validateDueDate(LocalDate.of(2026, 10, 12), loanDate));
        assertNotNull(Validator.validateDueDate(LocalDate.of(2026, 10, 11), loanDate));
    }
}
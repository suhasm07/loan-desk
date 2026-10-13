package service;

import java.time.LocalDate;

import model.Category;

public class Validator {
    // All the symbols that are allowed in a password
    private static final String ALLOWED_SYMBOLS = "!@#$%^&*?._-";

    // Trims and converts the username into lower case
    public static String cleanUsername(String username) {
        if (username == null) {
            return "";
        }
        return username.trim().toLowerCase();
    }

    // Returns an error message if invalid, or null if the username is valid
    public static String validateUsername(String username) {
        if (username == null) {
            return "Username cannot be empty";
        }

        String trimmedName = username.trim();

        if (trimmedName.isEmpty()) {
            return "Username cannot be empty";
        }

        if (trimmedName.length() < 3 || trimmedName.length() > 20) {
            return "Username must be between 3-20 characters";
        }

        for (int i = 0; i < trimmedName.length(); i++) {
            char ch = trimmedName.charAt(i);
            boolean isLetter = (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
            boolean isDigit = ch >= '0' && ch <= '9';
            boolean isUnderscore = ch == '_';
            if (!isLetter && !isDigit && !isUnderscore) {
                return "Username can only contain letters, digits and underscores";
            }
        }
        return null;
    }

    // Validates the password. Passwords are NOT trimmed and spaces are rejected.
    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty";
        }

        // This ensures that all the criteria is met.
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;

        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);

            if (Character.isWhitespace(ch)) {
                return "Password cannot contain spaces";
            }

            if (ch >= 'A' && ch <= 'Z') {
                hasUpper = true;
            } else if (ch >= 'a' && ch <= 'z') {
                hasLower = true;
            } else if (ch >= '0' && ch <= '9') {
                hasDigit = true;
            } else if (ALLOWED_SYMBOLS.indexOf(ch) >= 0) {
                hasSymbol = true;
            } else {
                return "Password can only use letters, digits and these symbols: " + ALLOWED_SYMBOLS;
            }
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        if (!hasUpper) {
            return "Password needs an uppercase letter";
        }
        if (!hasLower) {
            return "Password needs a lowercase letter";
        }
        if (!hasDigit) {
            return "Password needs a digit";
        }
        if (!hasSymbol) {
            return "Password needs a symbol: " + ALLOWED_SYMBOLS;
        }
        return null;
    }

    // Validates both first name and last name. fieldLabel is "First name" or "Last name"
    public static String validateName(String name, String field) {
        if (name == null) {
            return field + " cannot be empty";
        }
        String trimmedName = name.trim();
        if (trimmedName.isEmpty()) {
            return field + " cannot be empty";
        }
        if (trimmedName.length() > 50) {
            return field + " must be 1-50 characters";
        }

        // return an error message if any part of the name is not a character other than a space, hyphen or an apostrophe
        for (int i = 0; i < trimmedName.length(); i++) {
            char ch = trimmedName.charAt(i);
            if (!Character.isLetter(ch) && ch != ' ' && ch != '-' && ch != '\'') {
                return field + " can only contain letters, spaces, hyphens and apostrophes";
            }
        }
        return null;
    }


    // Trim item code and convert it into uppercase
    public static String cleanItemCode(String itemCode) {
        if (itemCode == null) {
            return "";
        }
        return itemCode.trim().toUpperCase();
    }

    // validate item code. Returns an error message if its empty or more than 20 characters or if it contains anything other than a letter, digit or a hyphen
    public static String validateItemCode(String itemCode) {
        if (itemCode == null) {
            return "Item code cannot be empty";
        }

        String trimmedItemCode = itemCode.trim();
        if (trimmedItemCode.isEmpty()) {
            return "Item code cannot be empty";
        }
        if (trimmedItemCode.length() > 20) {
            return "Item code must be 1-20 characters";
        }

        for (int i = 0; i < trimmedItemCode.length(); i++) {
            char ch = trimmedItemCode.charAt(i);
            boolean isLetter = (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
            boolean isDigit = ch >= '0' && ch <= '9';
            boolean isHyphen = ch == '-';
            if (!isLetter && !isDigit && !isHyphen) {
                return "Item code can only contain English letters, digits and hyphens";
            }
        }
        return null;
    }
    // validate item name. Returns error message if its empty or if the length is
    public static String validateItemName(String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) {
            return "Item name cannot be empty";
        }
        if (itemName.trim().length() > 80) {
            return "Item name must be 1-80 characters";
        }
        return null;
    }

    // validates borrower's name
    public static String validateBorrowerName(String borrowerName) {
        // returns an error message if empty
        if (borrowerName == null || borrowerName.trim().isEmpty()) {
            return "Borrower name cannot be empty";
        }
        // returns an error message if the length is greater than 60
        if (borrowerName.trim().length() > 60) {
            return "Borrower name must be 1-60 characters";
        }
        return null;
    }

    // validate category
    public static String validateCategory(Category category) {
        // returns an error message if the category is empty
        if (category == null) {
            return "Please select a category";
        }
        return null;
    }

    // The note is optional. Spaces and line breaks count towards the 250 limit.
    public static String validateNote(String note) {
        if (note == null) {
            return null;
        }
        if (note.length() > 250) {
            return "Note cannot be more than 250 characters";
        }
        return null;
    }

    // Due date must be selected and must be the same as loan date or later
    public static String validateDueDate(LocalDate dueDate, LocalDate loanDate) {
        if (dueDate == null) {
            return "Please select a due date";
        }
        // returns am error message if due date is before loan date
        if (dueDate.isBefore(loanDate)) {
            return "Due date cannot be before the loan date";
        }
        return null;
    }
}
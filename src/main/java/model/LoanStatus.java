package model;

public enum LoanStatus {
    ON_LOAN("On loan"),
    OVERDUE("Overdue"),
    RETURNED("Returned");

    private final String label;

    private LoanStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
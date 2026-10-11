package model;

public enum Category {
    TOOLS("Tools"),
    ELECTRONICS("Electronics"),
    OUTDOOR("Outdoor"),
    OTHER("Other");

    private final String label;

    private Category(String label) {
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

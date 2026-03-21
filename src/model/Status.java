package model;

public enum Status {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private final String label;

    Status(String label) { this.label = label; }

    @Override
    public String toString() { return label; }
}

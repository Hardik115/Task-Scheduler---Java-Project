package model;

public enum Priority {
    LOW(1, "Low"),
    MEDIUM(2, "Medium"),
    HIGH(3, "High"),
    CRITICAL(4, "Critical");

    private final int score;
    private final String label;

    Priority(int score, String label) {
        this.score = score;
        this.label = label;
    }

    public int getScore() { return score; }
    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}

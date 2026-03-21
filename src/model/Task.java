package model;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task implements Comparable<Task>, Serializable {
    private static final long serialVersionUID = 1L;
    private static int counter = 1;

    /** Called after loading from disk to prevent ID collisions. */
    public static void setCounter(int next) { counter = next; }

    private final int id;
    private String name;
    private String description;
    private LocalDateTime deadline;
    private Priority priority;
    private Status status;
    private final LocalDateTime createdAt;

    public Task(String name, String description, LocalDateTime deadline, Priority priority) {
        this.id = counter++;
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.priority = priority;
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public double getEffectiveScore() {
        if (status == Status.COMPLETED) return -1.0;
        return priority.getScore() * getUrgencyMultiplier();
    }

    private double getUrgencyMultiplier() {
        LocalDateTime now = LocalDateTime.now();
        if (deadline.isBefore(now)) return 10.0;
        long mins = Duration.between(now, deadline).toMinutes();
        if (mins < 60)   return 8.0;
        if (mins < 360)  return 5.0;
        if (mins < 1440) return 3.0;
        if (mins < 4320) return 2.0;
        return 1.0;
    }

    public String getUrgencyLabel() {
        if (status == Status.COMPLETED) return "Done";
        LocalDateTime now = LocalDateTime.now();
        if (deadline.isBefore(now)) return "OVERDUE";
        long mins = Duration.between(now, deadline).toMinutes();
        if (mins < 60)   return "< 1 Hour";
        if (mins < 360)  return "< 6 Hours";
        if (mins < 1440) return "< 24 Hours";
        if (mins < 4320) return "< 3 Days";
        return "On Track";
    }

    public boolean isOverdue() {
        return status != Status.COMPLETED && deadline.isBefore(LocalDateTime.now());
    }

    @Override
    public int compareTo(Task other) {
        return Double.compare(other.getEffectiveScore(), this.getEffectiveScore());
    }

    // Getters
    public int getId()               { return id; }
    public String getName()          { return name; }
    public String getDescription()   { return description; }
    public LocalDateTime getDeadline() { return deadline; }
    public Priority getPriority()    { return priority; }
    public Status getStatus()        { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setName(String name)            { this.name = name; }
    public void setDescription(String desc)     { this.description = desc; }
    public void setDeadline(LocalDateTime dl)   { this.deadline = dl; }
    public void setPriority(Priority p)         { this.priority = p; }
    public void setStatus(Status s)             { this.status = s; }

    public String getFormattedDeadline() {
        return deadline.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
    }

    public String getFormattedCreatedAt() {
        return createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
    }
}

package scheduler;

import model.Priority;
import model.Status;
import model.Task;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TaskScheduler {
    private final List<Task> tasks = new ArrayList<>();

    public void save(String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(tasks);
        } catch (IOException e) {
            System.err.println("Warning: could not save tasks — " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public boolean load(String filePath) {
        File f = new File(filePath);
        if (!f.exists()) return false;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            List<Task> loaded = (List<Task>) ois.readObject();
            tasks.clear();
            tasks.addAll(loaded);
            int maxId = tasks.stream().mapToInt(Task::getId).max().orElse(0);
            Task.setCounter(maxId + 1);
            return true;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Warning: could not load tasks — " + e.getMessage());
            return false;
        }
    }

    public Task addTask(String name, String description, LocalDateTime deadline, Priority priority) {
        Task t = new Task(name, description, deadline, priority);
        tasks.add(t);
        return t;
    }

    public boolean removeTask(int id) {
        return tasks.removeIf(t -> t.getId() == id);
    }

    public boolean setStatus(int id, Status newStatus) {
        Optional<Task> found = findById(id);
        found.ifPresent(t -> t.setStatus(newStatus));
        return found.isPresent();
    }

    public boolean updateTask(int id, String name, String desc, LocalDateTime deadline, Priority priority) {
        Optional<Task> found = findById(id);
        found.ifPresent(t -> {
            t.setName(name);
            t.setDescription(desc);
            t.setDeadline(deadline);
            t.setPriority(priority);
        });
        return found.isPresent();
    }

    public Optional<Task> findById(int id) {
        return tasks.stream().filter(t -> t.getId() == id).findFirst();
    }

    public List<Task> getAllTasksSorted() {
        return tasks.stream().sorted().collect(Collectors.toList());
    }

    public List<Task> getTasksByStatus(Status status) {
        return tasks.stream()
                .filter(t -> t.getStatus() == status)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Task> search(String keyword) {
        String kw = keyword.toLowerCase().trim();
        if (kw.isEmpty()) return getAllTasksSorted();
        return tasks.stream()
                .filter(t -> t.getName().toLowerCase().contains(kw)
                        || t.getDescription().toLowerCase().contains(kw))
                .sorted()
                .collect(Collectors.toList());
    }

    public Optional<Task> getNextTask() {
        return tasks.stream()
                .filter(t -> t.getStatus() != Status.COMPLETED)
                .min((a, b) -> Double.compare(b.getEffectiveScore(), a.getEffectiveScore()));
    }

    public int  getTotalCount()      { return tasks.size(); }
    public long getPendingCount()    { return tasks.stream().filter(t -> t.getStatus() == Status.PENDING).count(); }
    public long getInProgressCount() { return tasks.stream().filter(t -> t.getStatus() == Status.IN_PROGRESS).count(); }
    public long getCompletedCount()  { return tasks.stream().filter(t -> t.getStatus() == Status.COMPLETED).count(); }
    public long getOverdueCount()    {
        LocalDateTime now = LocalDateTime.now();
        return tasks.stream()
                .filter(t -> t.getStatus() != Status.COMPLETED && t.getDeadline().isBefore(now))
                .count();
    }
}

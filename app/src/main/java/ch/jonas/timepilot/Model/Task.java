package ch.jonas.timepilot.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private String Task;
    private String description;
    private LocalDateTime dueTime;
    private int expectedDurationMinutes;
    private List<String> goals;
    private boolean started;
    private boolean Completed;

    public Task(String Task, String description, LocalDateTime dueTime) {
        this(Task, description, dueTime, 0);
    }

    public Task(String Task, String description, LocalDateTime dueTime, int expectedDurationMinutes) {
        this.Task = Task;
        this.description = description;
        this.dueTime = dueTime;
        this.expectedDurationMinutes = expectedDurationMinutes;
        this.goals = new ArrayList<>();
        this.started = false;
        this.Completed = false;
    }

    public String getTask() {
        return Task;
    }

    public void setTask(String Task) {
        this.Task = Task;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDueTime() {
        return dueTime;
    }

    public void setDueTime(LocalDateTime dueTime) {
        this.dueTime = dueTime;
    }

    public int getExpectedDurationMinutes() {
        return expectedDurationMinutes;
    }

    public void setExpectedDurationMinutes(int expectedDurationMinutes) {
        this.expectedDurationMinutes = expectedDurationMinutes;
    }

    public List<String> getGoals() {
        if (goals == null) {
            goals = new ArrayList<>();
        }
        return goals;
    }

    public void setGoals(List<String> goals) {
        this.goals = goals == null ? new ArrayList<>() : new ArrayList<>(goals);
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isCompleted() {
        return Completed;
    }

    public void setCompleted(boolean Completed) {
        this.Completed = Completed;
    }
}

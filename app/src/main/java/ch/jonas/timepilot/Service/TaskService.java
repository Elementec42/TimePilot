package ch.jonas.timepilot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import ch.jonas.timepilot.model.Task;

public class TaskService {
    private final List<Task> tasks = new ArrayList<>();

    public Task addTask(Task task) {
        tasks.add(Objects.requireNonNull(task, "task must not be null"));
        return task;
    }

    public boolean removeTask(Task task) {
        return tasks.remove(task);
    }

    public void editTask(Task task, Consumer<Task> changes) {
        requireManagedTask(task);
        Objects.requireNonNull(changes, "changes must not be null").accept(task);
    }

    public List<Task> listTasks() {
        return List.copyOf(tasks);
    }

    public List<Task> getOpenTasks() {
        return tasks.stream()
                .filter(task -> !task.isCompleted())
                .toList();
    }

    private void requireManagedTask(Task task) {
        Objects.requireNonNull(task, "task must not be null");
        if (!tasks.contains(task)) {
            throw new IllegalArgumentException("task is not managed by this service");
        }
    }
}

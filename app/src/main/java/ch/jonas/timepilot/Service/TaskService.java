package ch.jonas.timepilot.service;

import java.util.ArrayList;
import java.util.List;

import ch.jonas.timepilot.model.Task;

public class TaskService {
    private final List<Task> tasks = new ArrayList<>();

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
    }

    public List<Task> getAllTasks() {
        return tasks;
    }

    public List<Task> getOpenTasks() {
        return tasks.stream()
                .filter(task -> !task.isCompleted())
                .toList();
    }
    
}
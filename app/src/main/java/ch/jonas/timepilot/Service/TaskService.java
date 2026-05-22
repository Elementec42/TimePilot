package ch.jonas.timepilot.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import ch.jonas.timepilot.model.Task;

public class TaskService {
    private static final Path DEFAULT_TASK_FILE = Path.of("data", "tasks.json");

    private final JsonListStorage<Task> storage = new JsonListStorage<>(Task.class);
    private final Path taskFile;
    private final List<Task> tasks;

    public TaskService() {
        this(DEFAULT_TASK_FILE);
    }

    public TaskService(Path taskFile) {
        this.taskFile = taskFile;
        this.tasks = new ArrayList<>(storage.loadList(taskFile));
    }

    public void addTask(Task task) {
        tasks.add(task);
        saveTasks();
    }

    public void removeTask(Task task) {
        tasks.remove(task);
        saveTasks();
    }

    public List<Task> getAllTasks() {
        return tasks;
    }

    public List<Task> getOpenTasks() {
        return tasks.stream()
                .filter(task -> !task.isCompleted())
                .toList();
    }

    public void saveTasks() {
        storage.saveList(tasks, taskFile);
    }

    public void loadTasks() {
        tasks.clear();
        tasks.addAll(storage.loadList(taskFile));
    }
}

package ch.jonas.timepilot.Model;

import java.time.LocalDateTime;
public class Task {

    private String Task;
    private String description;
    private LocalDateTime dueTime;
    private boolean Completed;

    public Task(String Task, String description, LocalDateTime dueTime) {
        this.Task = Task;
        this.description = description;
        this.dueTime = dueTime;
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
    public boolean isCompleted() {
        return Completed;
    }
    public void setCompleted(boolean Completed) {
        this.Completed = Completed;
    }
}
    


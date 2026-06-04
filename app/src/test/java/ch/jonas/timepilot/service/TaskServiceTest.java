package ch.jonas.timepilot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import ch.jonas.timepilot.model.Task;

class TaskServiceTest {
    @Test
    void addEditRemoveAndListTasks() {
        TaskService service = new TaskService();
        Task task = new Task("Draft report", "", LocalDate.of(2026, 6, 15), Duration.ofHours(2));

        service.addTask(task);
        service.editTask(task, managedTask -> managedTask.setTitle("Finish report"));

        assertEquals("Finish report", service.listTasks().getFirst().getTitle());
        assertTrue(service.removeTask(task));
        assertTrue(service.listTasks().isEmpty());
    }

    @Test
    void listTasksDoesNotExposeInternalList() {
        TaskService service = new TaskService();
        service.addTask(new Task("Read", "", LocalDate.of(2026, 6, 15), Duration.ofMinutes(30)));

        assertThrows(UnsupportedOperationException.class, () -> service.listTasks().clear());
    }

    @Test
    void openTasksExcludeCompletedTasks() {
        TaskService service = new TaskService();
        Task completedTask = new Task("Done", "", LocalDate.of(2026, 6, 15), Duration.ofMinutes(15));
        Task openTask = new Task("Open", "", LocalDate.of(2026, 6, 16), Duration.ofMinutes(15));

        completedTask.markCompleted();
        service.addTask(completedTask);
        service.addTask(openTask);

        assertEquals(1, service.getOpenTasks().size());
        assertEquals("Open", service.getOpenTasks().getFirst().getTitle());
    }
}

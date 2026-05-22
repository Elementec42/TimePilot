package ch.jonas.timepilot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.jonas.timepilot.model.Task;

class JsonListStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void savesAndLoadsGenericTaskListAsJson() throws Exception {
        Path file = tempDir.resolve("tasks.json");
        JsonListStorage<Task> storage = new JsonListStorage<>(Task.class);
        Task task = new Task("Learn JSON", "Persist generic lists", LocalDateTime.of(2026, 5, 22, 9, 30));

        storage.saveList(List.of(task), file);
        List<Task> loadedTasks = storage.loadList(file);

        assertEquals(1, loadedTasks.size());
        assertEquals("Learn JSON", loadedTasks.getFirst().getTask());
        assertEquals("Persist generic lists", loadedTasks.getFirst().getDescription());
        assertEquals(LocalDateTime.of(2026, 5, 22, 9, 30), loadedTasks.getFirst().getDueTime());
        assertFalse(loadedTasks.getFirst().isCompleted());
        assertFalse(Files.readString(file).isBlank());
    }
}

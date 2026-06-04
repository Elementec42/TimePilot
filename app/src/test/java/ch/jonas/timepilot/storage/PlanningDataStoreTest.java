package ch.jonas.timepilot.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.jonas.timepilot.model.Deadline;
import ch.jonas.timepilot.model.Exam;
import ch.jonas.timepilot.model.ExamPriority;
import ch.jonas.timepilot.model.StudySession;
import ch.jonas.timepilot.model.Task;

class PlanningDataStoreTest {
    @TempDir
    Path tempDirectory;

    @Test
    void loadReturnsEmptyDataWhenFileDoesNotExist() {
        PlanningDataStore store = new PlanningDataStore(tempDirectory.resolve("missing.json"));

        PlanningData loadedData = store.load();

        assertTrue(loadedData.getTasks().isEmpty());
        assertTrue(loadedData.getDeadlines().isEmpty());
        assertTrue(loadedData.getExams().isEmpty());
        assertTrue(loadedData.getStudySessions().isEmpty());
    }

    @Test
    void saveAndLoadRoundTripsPlanningData() throws Exception {
        Path dataFile = tempDirectory.resolve("nested").resolve("planning-data.json");
        PlanningDataStore store = new PlanningDataStore(dataFile);

        Task task = new Task("Draft report", "Outline first", LocalDate.of(2026, 6, 15), Duration.ofHours(2));
        task.markCompleted();
        Deadline deadline = new Deadline(
                "Essay",
                "Submit PDF",
                LocalDate.of(2026, 6, 20),
                "History",
                Duration.ofHours(4));
        Exam exam = new Exam(
                "Final",
                "Math",
                LocalDate.of(2026, 7, 1),
                Duration.ofHours(10),
                ExamPriority.HIGH);
        StudySession session = new StudySession(
                "Review",
                "Physics",
                LocalDateTime.of(2026, 6, 5, 9, 0),
                LocalDateTime.of(2026, 6, 5, 10, 30),
                "Mechanics");

        store.save(new PlanningData(List.of(task), List.of(deadline), List.of(exam), List.of(session)));

        PlanningData loadedData = store.load();

        assertTrue(Files.exists(dataFile));
        assertEquals("Draft report", loadedData.getTasks().getFirst().getTitle());
        assertTrue(loadedData.getTasks().getFirst().isCompleted());
        assertEquals(Duration.ofHours(4), loadedData.getDeadlines().getFirst().getEstimatedWorkDuration());
        assertEquals(ExamPriority.HIGH, loadedData.getExams().getFirst().getPriority());
        assertEquals(LocalDateTime.of(2026, 6, 5, 10, 30),
                loadedData.getStudySessions().getFirst().getEndDateTime());
    }

    @Test
    void savedJsonUsesReadableFields() throws Exception {
        Path dataFile = tempDirectory.resolve("planning-data.json");
        PlanningDataStore store = new PlanningDataStore(dataFile);

        store.save(new PlanningData(
                List.of(new Task("Read", "", LocalDate.of(2026, 6, 15), Duration.ofMinutes(45))),
                List.of(),
                List.of(),
                List.of()));

        String json = Files.readString(dataFile);

        assertTrue(json.contains("\"dueDate\": \"2026-06-15\""));
        assertTrue(json.contains("\"estimatedWorkMinutes\": 45"));
        assertFalse(json.contains("PT45M"));
    }

    @Test
    void invalidJsonRaisesStorageException() throws Exception {
        Path dataFile = tempDirectory.resolve("planning-data.json");
        Files.writeString(dataFile, "{ invalid json");
        PlanningDataStore store = new PlanningDataStore(dataFile);

        assertThrows(StorageException.class, store::load);
    }
}

package ch.jonas.timepilot.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class DomainModelTest {
    @Test
    void taskCanBeCompletedAndReopened() {
        Task task = new Task("Read chapter", "", LocalDate.of(2026, 6, 10), Duration.ofMinutes(45));

        task.markCompleted();
        assertTrue(task.isCompleted());

        task.reopen();
        assertFalse(task.isCompleted());
    }

    @Test
    void emptyTitlesAreRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new Task(" ", "", LocalDate.of(2026, 6, 10), Duration.ofMinutes(30)));
    }

    @Test
    void negativeDurationsAreRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new Deadline(
                        "Essay",
                        "",
                        LocalDate.of(2026, 6, 12),
                        "History",
                        Duration.ofMinutes(-1)));
    }

    @Test
    void studySessionRequiresEndAfterStart() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 3, 10, 0);

        assertThrows(IllegalArgumentException.class, () ->
                new StudySession("Math", "Algebra", start, start, ""));
    }

    @Test
    void calendarBlockCanLinkToStudySession() {
        StudySession session = new StudySession(
                "Prepare exam",
                "Physics",
                LocalDateTime.of(2026, 6, 4, 14, 0),
                LocalDateTime.of(2026, 6, 4, 16, 0),
                "Focus on mechanics");

        CalendarBlock block = CalendarBlock.forStudySession(session);

        assertEquals("Prepare exam", block.getTitle());
        assertEquals(Duration.ofHours(2), block.getDuration());
        assertTrue(block.getStudySession().isPresent());
        assertTrue(block.getTask().isEmpty());
    }
}

package ch.jonas.timepilot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import ch.jonas.timepilot.model.Deadline;
import ch.jonas.timepilot.model.Exam;
import ch.jonas.timepilot.model.ExamPriority;
import ch.jonas.timepilot.model.StudySession;

class PlanningServicesTest {
    @Test
    void deadlineServiceCanAddEditRemoveAndListDeadlines() {
        DeadlineService service = new DeadlineService();
        Deadline deadline = new Deadline(
                "Essay",
                "",
                LocalDate.of(2026, 6, 20),
                "History",
                Duration.ofHours(4));

        service.addDeadline(deadline);
        service.editDeadline(deadline, managedDeadline -> managedDeadline.setSubjectOrModule("Modern History"));

        assertEquals("Modern History", service.listDeadlines().getFirst().getSubjectOrModule());
        assertTrue(service.removeDeadline(deadline));
        assertTrue(service.listDeadlines().isEmpty());
    }

    @Test
    void examServiceCanAddEditRemoveAndListExams() {
        ExamService service = new ExamService();
        Exam exam = new Exam(
                "Final",
                "Math",
                LocalDate.of(2026, 7, 1),
                Duration.ofHours(10),
                ExamPriority.HIGH);

        service.addExam(exam);
        service.editExam(exam, managedExam -> managedExam.setPriority(ExamPriority.MEDIUM));

        assertEquals(ExamPriority.MEDIUM, service.listExams().getFirst().getPriority());
        assertTrue(service.removeExam(exam));
        assertTrue(service.listExams().isEmpty());
    }

    @Test
    void studySessionServiceCanAddEditRemoveAndListStudySessions() {
        StudySessionService service = new StudySessionService();
        StudySession session = new StudySession(
                "Review",
                "Physics",
                LocalDateTime.of(2026, 6, 5, 9, 0),
                LocalDateTime.of(2026, 6, 5, 10, 0),
                "");

        service.addStudySession(session);
        service.editStudySession(session, managedSession -> managedSession.setNotes("Mechanics"));

        assertEquals("Mechanics", service.listStudySessions().getFirst().getNotes());
        assertTrue(service.removeStudySession(session));
        assertTrue(service.listStudySessions().isEmpty());
    }
}

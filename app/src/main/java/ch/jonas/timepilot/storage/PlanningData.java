package ch.jonas.timepilot.storage;

import java.util.List;
import java.util.Objects;

import ch.jonas.timepilot.model.Deadline;
import ch.jonas.timepilot.model.Exam;
import ch.jonas.timepilot.model.StudySession;
import ch.jonas.timepilot.model.Task;

public class PlanningData {
    private final List<Task> tasks;
    private final List<Deadline> deadlines;
    private final List<Exam> exams;
    private final List<StudySession> studySessions;

    public PlanningData(
            List<Task> tasks,
            List<Deadline> deadlines,
            List<Exam> exams,
            List<StudySession> studySessions) {
        this.tasks = copyList(tasks, "tasks");
        this.deadlines = copyList(deadlines, "deadlines");
        this.exams = copyList(exams, "exams");
        this.studySessions = copyList(studySessions, "studySessions");
    }

    public static PlanningData empty() {
        return new PlanningData(List.of(), List.of(), List.of(), List.of());
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public List<Deadline> getDeadlines() {
        return deadlines;
    }

    public List<Exam> getExams() {
        return exams;
    }

    public List<StudySession> getStudySessions() {
        return studySessions;
    }

    private static <T> List<T> copyList(List<T> values, String fieldName) {
        Objects.requireNonNull(values, fieldName + " must not be null");
        return List.copyOf(values);
    }
}

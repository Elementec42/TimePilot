package ch.jonas.timepilot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import ch.jonas.timepilot.model.Exam;

public class ExamService {
    private final List<Exam> exams = new ArrayList<>();

    public Exam addExam(Exam exam) {
        exams.add(Objects.requireNonNull(exam, "exam must not be null"));
        return exam;
    }

    public boolean removeExam(Exam exam) {
        return exams.remove(exam);
    }

    public void editExam(Exam exam, Consumer<Exam> changes) {
        requireManagedExam(exam);
        Objects.requireNonNull(changes, "changes must not be null").accept(exam);
    }

    public List<Exam> listExams() {
        return List.copyOf(exams);
    }

    private void requireManagedExam(Exam exam) {
        Objects.requireNonNull(exam, "exam must not be null");
        if (!exams.contains(exam)) {
            throw new IllegalArgumentException("exam is not managed by this service");
        }
    }
}

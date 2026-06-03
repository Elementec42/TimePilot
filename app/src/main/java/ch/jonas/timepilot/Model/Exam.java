package ch.jonas.timepilot.model;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

public class Exam {
    private String title;
    private String subjectOrModule;
    private LocalDate examDate;
    private Duration estimatedStudyDuration;
    private ExamPriority priority;

    public Exam(
            String title,
            String subjectOrModule,
            LocalDate examDate,
            Duration estimatedStudyDuration,
            ExamPriority priority) {
        setTitle(title);
        setSubjectOrModule(subjectOrModule);
        setExamDate(examDate);
        setEstimatedStudyDuration(estimatedStudyDuration);
        setPriority(priority);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = ModelValidation.requireText(title, "title");
    }

    public String getSubjectOrModule() {
        return subjectOrModule;
    }

    public void setSubjectOrModule(String subjectOrModule) {
        this.subjectOrModule = ModelValidation.requireText(subjectOrModule, "subjectOrModule");
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = ModelValidation.requireDate(examDate, "examDate");
    }

    public Duration getEstimatedStudyDuration() {
        return estimatedStudyDuration;
    }

    public void setEstimatedStudyDuration(Duration estimatedStudyDuration) {
        this.estimatedStudyDuration = ModelValidation.requireNonNegativeDuration(
                estimatedStudyDuration,
                "estimatedStudyDuration");
    }

    public ExamPriority getPriority() {
        return priority;
    }

    public void setPriority(ExamPriority priority) {
        this.priority = Objects.requireNonNull(priority, "priority must not be null");
    }
}

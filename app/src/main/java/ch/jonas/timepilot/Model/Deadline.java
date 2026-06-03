package ch.jonas.timepilot.model;

import java.time.Duration;
import java.time.LocalDate;

public class Deadline {
    private String title;
    private String description;
    private LocalDate dueDate;
    private String subjectOrModule;
    private Duration estimatedWorkDuration;

    public Deadline(
            String title,
            String description,
            LocalDate dueDate,
            String subjectOrModule,
            Duration estimatedWorkDuration) {
        setTitle(title);
        setDescription(description);
        setDueDate(dueDate);
        setSubjectOrModule(subjectOrModule);
        setEstimatedWorkDuration(estimatedWorkDuration);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = ModelValidation.requireText(title, "title");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = ModelValidation.defaultText(description);
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = ModelValidation.requireDate(dueDate, "dueDate");
    }

    public String getSubjectOrModule() {
        return subjectOrModule;
    }

    public void setSubjectOrModule(String subjectOrModule) {
        this.subjectOrModule = ModelValidation.requireText(subjectOrModule, "subjectOrModule");
    }

    public Duration getEstimatedWorkDuration() {
        return estimatedWorkDuration;
    }

    public void setEstimatedWorkDuration(Duration estimatedWorkDuration) {
        this.estimatedWorkDuration = ModelValidation.requireNonNegativeDuration(
                estimatedWorkDuration,
                "estimatedWorkDuration");
    }
}

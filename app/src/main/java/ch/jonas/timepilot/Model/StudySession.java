package ch.jonas.timepilot.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class StudySession {
    private String title;
    private String subjectOrModule;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String notes;

    public StudySession(
            String title,
            String subjectOrModule,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String notes) {
        setTitle(title);
        setSubjectOrModule(subjectOrModule);
        setTimeRange(startDateTime, endDateTime);
        setNotes(notes);
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

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        ModelValidation.requireValidTimeRange(startDateTime, endDateTime);
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public Duration getDuration() {
        return Duration.between(startDateTime, endDateTime);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = ModelValidation.defaultText(notes);
    }
}

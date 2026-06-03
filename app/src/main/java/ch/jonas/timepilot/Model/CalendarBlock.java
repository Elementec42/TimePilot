package ch.jonas.timepilot.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class CalendarBlock {
    private String title;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Task task;
    private Deadline deadline;
    private Exam exam;
    private StudySession studySession;

    public CalendarBlock(String title, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        setTitle(title);
        setTimeRange(startDateTime, endDateTime);
    }

    public static CalendarBlock forTask(Task task, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        CalendarBlock block = new CalendarBlock(task.getTitle(), startDateTime, endDateTime);
        block.linkTask(task);
        return block;
    }

    public static CalendarBlock forDeadline(
            Deadline deadline,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime) {
        CalendarBlock block = new CalendarBlock(deadline.getTitle(), startDateTime, endDateTime);
        block.linkDeadline(deadline);
        return block;
    }

    public static CalendarBlock forExam(Exam exam, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        CalendarBlock block = new CalendarBlock(exam.getTitle(), startDateTime, endDateTime);
        block.linkExam(exam);
        return block;
    }

    public static CalendarBlock forStudySession(StudySession studySession) {
        CalendarBlock block = new CalendarBlock(
                studySession.getTitle(),
                studySession.getStartDateTime(),
                studySession.getEndDateTime());
        block.linkStudySession(studySession);
        return block;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = ModelValidation.requireText(title, "title");
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

    public Optional<Task> getTask() {
        return Optional.ofNullable(task);
    }

    public Optional<Deadline> getDeadline() {
        return Optional.ofNullable(deadline);
    }

    public Optional<Exam> getExam() {
        return Optional.ofNullable(exam);
    }

    public Optional<StudySession> getStudySession() {
        return Optional.ofNullable(studySession);
    }

    public void linkTask(Task task) {
        clearLinks();
        this.task = ModelValidation.requireObject(task, "task");
    }

    public void linkDeadline(Deadline deadline) {
        clearLinks();
        this.deadline = ModelValidation.requireObject(deadline, "deadline");
    }

    public void linkExam(Exam exam) {
        clearLinks();
        this.exam = ModelValidation.requireObject(exam, "exam");
    }

    public void linkStudySession(StudySession studySession) {
        clearLinks();
        this.studySession = ModelValidation.requireObject(studySession, "studySession");
    }

    public void clearLinks() {
        this.task = null;
        this.deadline = null;
        this.exam = null;
        this.studySession = null;
    }
}

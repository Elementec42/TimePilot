package ch.jonas.timepilot.ui;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

import ch.jonas.timepilot.model.Deadline;
import ch.jonas.timepilot.model.CalendarBlock;
import ch.jonas.timepilot.model.Exam;
import ch.jonas.timepilot.model.ExamPriority;
import ch.jonas.timepilot.model.StudySession;
import ch.jonas.timepilot.model.Task;
import ch.jonas.timepilot.service.CalendarService;
import ch.jonas.timepilot.service.DeadlineService;
import ch.jonas.timepilot.service.ExamService;
import ch.jonas.timepilot.service.StudySessionService;
import ch.jonas.timepilot.service.TaskService;
import ch.jonas.timepilot.storage.PlanningData;
import ch.jonas.timepilot.storage.PlanningDataStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PlanningController {
    private final TaskService taskService = new TaskService();
    private final DeadlineService deadlineService = new DeadlineService();
    private final ExamService examService = new ExamService();
    private final StudySessionService studySessionService = new StudySessionService();
    private final PlanningDataStore dataStore;

    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final ObservableList<Deadline> deadlines = FXCollections.observableArrayList();
    private final ObservableList<Exam> exams = FXCollections.observableArrayList();
    private final ObservableList<StudySession> studySessions = FXCollections.observableArrayList();

    public PlanningController(PlanningDataStore dataStore) {
        this.dataStore = Objects.requireNonNull(dataStore, "dataStore must not be null");
        load();
    }

    public static PlanningController createDefault() {
        Path dataFile = Path.of(System.getProperty("user.home"), ".timepilot", "planning-data.json");
        return new PlanningController(new PlanningDataStore(dataFile));
    }

    public ObservableList<Task> getTasks() {
        return tasks;
    }

    public ObservableList<Deadline> getDeadlines() {
        return deadlines;
    }

    public ObservableList<Exam> getExams() {
        return exams;
    }

    public ObservableList<StudySession> getStudySessions() {
        return studySessions;
    }

    public void addTask(String title, String description, LocalDate dueDate, long estimatedMinutes) {
        taskService.addTask(new Task(title, description, dueDate, Duration.ofMinutes(estimatedMinutes)));
        persistAndRefresh();
    }

    public void updateTask(
            Task task,
            String title,
            String description,
            LocalDate dueDate,
            long estimatedMinutes,
            boolean completed) {
        taskService.editTask(task, managedTask -> {
            managedTask.setTitle(title);
            managedTask.setDescription(description);
            managedTask.setDueDate(dueDate);
            managedTask.setEstimatedWorkDuration(Duration.ofMinutes(estimatedMinutes));
            if (completed) {
                managedTask.markCompleted();
            } else {
                managedTask.reopen();
            }
        });
        persistAndRefresh();
    }

    public void removeTask(Task task) {
        taskService.removeTask(task);
        persistAndRefresh();
    }

    public void setTaskCompleted(Task task, boolean completed) {
        taskService.editTask(task, managedTask -> {
            if (completed) {
                managedTask.markCompleted();
            } else {
                managedTask.reopen();
            }
        });
        persistAndRefresh();
    }

    public void addDeadline(
            String title,
            String subjectOrModule,
            String description,
            LocalDate dueDate,
            long estimatedMinutes) {
        deadlineService.addDeadline(new Deadline(
                title,
                description,
                dueDate,
                subjectOrModule,
                Duration.ofMinutes(estimatedMinutes)));
        persistAndRefresh();
    }

    public void updateDeadline(
            Deadline deadline,
            String title,
            String subjectOrModule,
            String description,
            LocalDate dueDate,
            long estimatedMinutes) {
        deadlineService.editDeadline(deadline, managedDeadline -> {
            managedDeadline.setTitle(title);
            managedDeadline.setSubjectOrModule(subjectOrModule);
            managedDeadline.setDescription(description);
            managedDeadline.setDueDate(dueDate);
            managedDeadline.setEstimatedWorkDuration(Duration.ofMinutes(estimatedMinutes));
        });
        persistAndRefresh();
    }

    public void removeDeadline(Deadline deadline) {
        deadlineService.removeDeadline(deadline);
        persistAndRefresh();
    }

    public void addExam(
            String title,
            String subjectOrModule,
            LocalDate examDate,
            long estimatedMinutes,
            ExamPriority priority) {
        examService.addExam(new Exam(
                title,
                subjectOrModule,
                examDate,
                Duration.ofMinutes(estimatedMinutes),
                priority));
        persistAndRefresh();
    }

    public void updateExam(
            Exam exam,
            String title,
            String subjectOrModule,
            LocalDate examDate,
            long estimatedMinutes,
            ExamPriority priority) {
        examService.editExam(exam, managedExam -> {
            managedExam.setTitle(title);
            managedExam.setSubjectOrModule(subjectOrModule);
            managedExam.setExamDate(examDate);
            managedExam.setEstimatedStudyDuration(Duration.ofMinutes(estimatedMinutes));
            managedExam.setPriority(priority);
        });
        persistAndRefresh();
    }

    public void removeExam(Exam exam) {
        examService.removeExam(exam);
        persistAndRefresh();
    }

    public void addStudySession(
            String title,
            String subjectOrModule,
            LocalDate startDate,
            int startHour,
            int startMinute,
            int endHour,
            int endMinute,
            String notes) {
        StudySession studySession = new StudySession(
                title,
                subjectOrModule,
                startDate.atTime(startHour, startMinute),
                startDate.atTime(endHour, endMinute),
                notes);
        validateStudySessionTime(studySession, null);
        studySessionService.addStudySession(studySession);
        persistAndRefresh();
    }

    public void updateStudySession(
            StudySession studySession,
            String title,
            String subjectOrModule,
            LocalDate startDate,
            int startHour,
            int startMinute,
            int endHour,
            int endMinute,
            String notes) {
        StudySession updatedStudySession = new StudySession(
                title,
                subjectOrModule,
                startDate.atTime(startHour, startMinute),
                startDate.atTime(endHour, endMinute),
                notes);
        validateStudySessionTime(updatedStudySession, studySession);
        studySessionService.editStudySession(studySession, managedStudySession -> {
            managedStudySession.setTitle(title);
            managedStudySession.setSubjectOrModule(subjectOrModule);
            managedStudySession.setTimeRange(
                    startDate.atTime(startHour, startMinute),
                    startDate.atTime(endHour, endMinute));
            managedStudySession.setNotes(notes);
        });
        persistAndRefresh();
    }

    public void removeStudySession(StudySession studySession) {
        studySessionService.removeStudySession(studySession);
        persistAndRefresh();
    }

    private void load() {
        PlanningData data = dataStore.load();
        data.getTasks().forEach(taskService::addTask);
        data.getDeadlines().forEach(deadlineService::addDeadline);
        data.getExams().forEach(examService::addExam);
        data.getStudySessions().forEach(studySessionService::addStudySession);
        refreshLists();
    }

    private void persistAndRefresh() {
        dataStore.save(new PlanningData(
                taskService.listTasks(),
                deadlineService.listDeadlines(),
                examService.listExams(),
                studySessionService.listStudySessions()));
        refreshLists();
    }

    private void refreshLists() {
        tasks.setAll(taskService.listTasks());
        deadlines.setAll(deadlineService.listDeadlines());
        exams.setAll(examService.listExams());
        studySessions.setAll(studySessionService.listStudySessions());
    }

    private void validateStudySessionTime(StudySession candidate, StudySession ignoredStudySession) {
        CalendarService calendarService = new CalendarService();
        studySessionService.listStudySessions().stream()
                .filter(studySession -> studySession != ignoredStudySession)
                .map(CalendarBlock::forStudySession)
                .forEach(calendarService::addBlock);
        calendarService.addBlock(CalendarBlock.forStudySession(candidate));
    }
}

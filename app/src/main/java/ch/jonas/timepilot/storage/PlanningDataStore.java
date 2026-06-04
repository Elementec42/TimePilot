package ch.jonas.timepilot.storage;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.jonas.timepilot.model.Deadline;
import ch.jonas.timepilot.model.Exam;
import ch.jonas.timepilot.model.ExamPriority;
import ch.jonas.timepilot.model.StudySession;
import ch.jonas.timepilot.model.Task;

public class PlanningDataStore {
    private final JsonRepository<PlanningDataJson> repository;

    public PlanningDataStore(Path dataFile) {
        this.repository = new JsonRepository<>(
                dataFile,
                createGson(),
                PlanningDataJson.class);
    }

    public PlanningData load() {
        return repository.load()
                .map(this::toPlanningData)
                .orElseGet(PlanningData::empty);
    }

    public void save(PlanningData data) {
        repository.save(toJson(Objects.requireNonNull(data, "data must not be null")));
    }

    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
    }

    private PlanningDataJson toJson(PlanningData data) {
        PlanningDataJson json = new PlanningDataJson();
        json.tasks = data.getTasks().stream()
                .map(this::toJson)
                .toList();
        json.deadlines = data.getDeadlines().stream()
                .map(this::toJson)
                .toList();
        json.exams = data.getExams().stream()
                .map(this::toJson)
                .toList();
        json.studySessions = data.getStudySessions().stream()
                .map(this::toJson)
                .toList();
        return json;
    }

    private PlanningData toPlanningData(PlanningDataJson json) {
        return new PlanningData(
                safeList(json.tasks).stream().map(this::toTask).toList(),
                safeList(json.deadlines).stream().map(this::toDeadline).toList(),
                safeList(json.exams).stream().map(this::toExam).toList(),
                safeList(json.studySessions).stream().map(this::toStudySession).toList());
    }

    private TaskJson toJson(Task task) {
        TaskJson json = new TaskJson();
        json.title = task.getTitle();
        json.description = task.getDescription();
        json.dueDate = task.getDueDate();
        json.estimatedWorkMinutes = task.getEstimatedWorkDuration().toMinutes();
        json.completed = task.isCompleted();
        return json;
    }

    private DeadlineJson toJson(Deadline deadline) {
        DeadlineJson json = new DeadlineJson();
        json.title = deadline.getTitle();
        json.description = deadline.getDescription();
        json.dueDate = deadline.getDueDate();
        json.subjectOrModule = deadline.getSubjectOrModule();
        json.estimatedWorkMinutes = deadline.getEstimatedWorkDuration().toMinutes();
        return json;
    }

    private ExamJson toJson(Exam exam) {
        ExamJson json = new ExamJson();
        json.title = exam.getTitle();
        json.subjectOrModule = exam.getSubjectOrModule();
        json.examDate = exam.getExamDate();
        json.estimatedStudyMinutes = exam.getEstimatedStudyDuration().toMinutes();
        json.priority = exam.getPriority();
        return json;
    }

    private StudySessionJson toJson(StudySession studySession) {
        StudySessionJson json = new StudySessionJson();
        json.title = studySession.getTitle();
        json.subjectOrModule = studySession.getSubjectOrModule();
        json.startDateTime = studySession.getStartDateTime();
        json.endDateTime = studySession.getEndDateTime();
        json.notes = studySession.getNotes();
        return json;
    }

    private Task toTask(TaskJson json) {
        Task task = new Task(
                json.title,
                json.description,
                json.dueDate,
                Duration.ofMinutes(json.estimatedWorkMinutes));
        if (json.completed) {
            task.markCompleted();
        }
        return task;
    }

    private Deadline toDeadline(DeadlineJson json) {
        return new Deadline(
                json.title,
                json.description,
                json.dueDate,
                json.subjectOrModule,
                Duration.ofMinutes(json.estimatedWorkMinutes));
    }

    private Exam toExam(ExamJson json) {
        return new Exam(
                json.title,
                json.subjectOrModule,
                json.examDate,
                Duration.ofMinutes(json.estimatedStudyMinutes),
                json.priority);
    }

    private StudySession toStudySession(StudySessionJson json) {
        return new StudySession(
                json.title,
                json.subjectOrModule,
                json.startDateTime,
                json.endDateTime,
                json.notes);
    }

    private static <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private static final class PlanningDataJson {
        private List<TaskJson> tasks = new ArrayList<>();
        private List<DeadlineJson> deadlines = new ArrayList<>();
        private List<ExamJson> exams = new ArrayList<>();
        private List<StudySessionJson> studySessions = new ArrayList<>();
    }

    private static final class TaskJson {
        private String title;
        private String description;
        private LocalDate dueDate;
        private long estimatedWorkMinutes;
        private boolean completed;
    }

    private static final class DeadlineJson {
        private String title;
        private String description;
        private LocalDate dueDate;
        private String subjectOrModule;
        private long estimatedWorkMinutes;
    }

    private static final class ExamJson {
        private String title;
        private String subjectOrModule;
        private LocalDate examDate;
        private long estimatedStudyMinutes;
        private ExamPriority priority;
    }

    private static final class StudySessionJson {
        private String title;
        private String subjectOrModule;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private String notes;
    }
}

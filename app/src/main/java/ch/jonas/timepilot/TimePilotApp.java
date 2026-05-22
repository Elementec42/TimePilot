package ch.jonas.timepilot;

import java.awt.Toolkit;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import ch.jonas.timepilot.model.Task;
import ch.jonas.timepilot.service.TaskService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;
import javafx.util.Duration;

public class TimePilotApp extends Application {
    private static final DateTimeFormatter DUE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter TIME_INPUT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter WEEK_RANGE_FORMATTER = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_HEADER_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter WEEK_DAY_HEADER_FORMATTER = DateTimeFormatter.ofPattern("EEE M/d", Locale.ENGLISH);
    private static final int SCHEDULE_START_HOUR = 6;
    private static final int SCHEDULE_END_HOUR = 22;

    private final TaskService taskService = new TaskService();
    private final ObservableList<Task> visibleTasks = FXCollections.observableArrayList();

    private TableView<Task> taskTable;
    private TextField titleField;
    private ComboBox<String> taskTypeBox;
    private TextArea descriptionField;
    private TextArea goalsField;
    private DatePicker dueDatePicker;
    private TextField timeField;
    private TextField durationField;
    private CheckBox openOnlyCheckBox;
    private Label formTitle;
    private Button addButton;
    private Button saveButton;
    private Button cancelEditButton;
    private Task editingTask;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setTop(createHeader());
        root.setCenter(createTaskTable());
        root.setRight(createTaskForm());
        root.setBottom(createActions());
        root.setStyle("-fx-background-color: #f6f7fb;");

        refreshTasks();

        Scene scene = new Scene(root, 1060, 680);
        stage.setTitle("TimePilot");
        stage.setMinWidth(920);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    private VBox createHeader() {
        Label title = new Label("TimePilot");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: #172033;");

        Label subtitle = new Label("Tasks");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #5d667a;");

        VBox header = new VBox(4, title, subtitle);
        header.setPadding(new Insets(0, 0, 18, 0));
        return header;
    }

    private TableView<Task> createTaskTable() {
        taskTable = new TableView<>(visibleTasks);
        taskTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        taskTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        taskTable.setPlaceholder(new Label("No tasks"));

        TableColumn<Task, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getTaskType()));
        typeColumn.setMinWidth(95);

        TableColumn<Task, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getTask()));
        titleColumn.setMinWidth(150);

        TableColumn<Task, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getDescription()));
        descriptionColumn.setMinWidth(240);

        TableColumn<Task, String> goalsColumn = new TableColumn<>("Goals");
        goalsColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(formatGoals(cell.getValue().getGoals())));
        goalsColumn.setMinWidth(160);

        TableColumn<Task, String> dueTimeColumn = new TableColumn<>("Due");
        dueTimeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(formatDueTime(cell.getValue().getDueTime())));
        dueTimeColumn.setMinWidth(130);

        TableColumn<Task, String> durationColumn = new TableColumn<>("Duration");
        durationColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(formatDuration(cell.getValue().getExpectedDurationMinutes())));
        durationColumn.setMinWidth(95);

        TableColumn<Task, Button> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(createStatusButton(cell.getValue())));
        statusColumn.setMinWidth(95);

        taskTable.getColumns().add(typeColumn);
        taskTable.getColumns().add(titleColumn);
        taskTable.getColumns().add(descriptionColumn);
        taskTable.getColumns().add(goalsColumn);
        taskTable.getColumns().add(dueTimeColumn);
        taskTable.getColumns().add(durationColumn);
        taskTable.getColumns().add(statusColumn);
        BorderPane.setMargin(taskTable, new Insets(0, 18, 0, 0));
        return taskTable;
    }

    private VBox createTaskForm() {
        formTitle = new Label("New Task");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #172033;");

        taskTypeBox = new ComboBox<>();
        taskTypeBox.getItems().addAll("ToDo", "Study Plan");
        taskTypeBox.setValue("ToDo");
        taskTypeBox.setMaxWidth(Double.MAX_VALUE);

        titleField = new TextField();
        titleField.setPromptText("Title");
        titleField.setPrefColumnCount(22);

        descriptionField = new TextArea();
        descriptionField.setPromptText("Description");
        descriptionField.setPrefRowCount(4);
        descriptionField.setWrapText(true);

        goalsField = new TextArea();
        goalsField.setPromptText("Goals / subtopics, one per line");
        goalsField.setPrefRowCount(4);
        goalsField.setWrapText(true);

        dueDatePicker = new DatePicker(LocalDate.now());
        dueDatePicker.setMaxWidth(Double.MAX_VALUE);

        timeField = new TextField(LocalTime.now().format(TIME_INPUT_FORMATTER));
        timeField.setPromptText("HH:mm");
        timeField.setMaxWidth(Double.MAX_VALUE);

        durationField = new TextField();
        durationField.setPromptText("Minutes");
        durationField.setMaxWidth(Double.MAX_VALUE);

        GridPane timeGrid = new GridPane();
        timeGrid.setHgap(10);
        timeGrid.setVgap(8);
        timeGrid.add(new Label("Date"), 0, 0);
        timeGrid.add(dueDatePicker, 1, 0);
        timeGrid.add(new Label("Time"), 0, 1);
        timeGrid.add(timeField, 1, 1);
        timeGrid.add(new Label("Work duration"), 0, 2);
        timeGrid.add(durationField, 1, 2);
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(95);
        ColumnConstraints inputColumn = new ColumnConstraints();
        inputColumn.setHgrow(Priority.ALWAYS);
        timeGrid.getColumnConstraints().add(labelColumn);
        timeGrid.getColumnConstraints().add(inputColumn);

        addButton = new Button("Add");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setDefaultButton(true);
        addButton.setOnAction(event -> addTask());

        saveButton = new Button("Save Changes");
        saveButton.setOnAction(event -> saveEditedTask());

        cancelEditButton = new Button("Cancel");
        cancelEditButton.setOnAction(event -> cancelEditingTask());

        HBox formButtons = new HBox(10, addButton, saveButton, cancelEditButton);
        setEditMode(false);

        VBox form = new VBox(12, formTitle, taskTypeBox, titleField, descriptionField, goalsField, timeGrid, formButtons);
        form.setPadding(new Insets(16));
        form.setPrefWidth(320);
        form.setMaxWidth(340);
        form.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d8dee9; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        return form;
    }

    private HBox createActions() {
        openOnlyCheckBox = new CheckBox("Open tasks only");
        openOnlyCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> refreshTasks());

        Button todayButton = new Button("Today Plan");
        todayButton.setOnAction(event -> openTodayPlanWindow());

        Button calendarButton = new Button("Calendar");
        calendarButton.setOnAction(event -> openCalendarWindow());

        Button timerButton = new Button("Timer");
        timerButton.setOnAction(event -> openTimerWindow());

        Button editButton = new Button("Edit");
        editButton.setOnAction(event -> editSelectedTask());

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(event -> deleteSelectedTask());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10, openOnlyCheckBox, spacer, todayButton, calendarButton, timerButton, editButton, deleteButton);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(18, 0, 0, 0));
        return actions;
    }

    private void addTask() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showWarning("Please enter a title.");
            return;
        }

        LocalDate dueDate = dueDatePicker.getValue();
        if (dueDate == null) {
            showWarning("Please select a date.");
            return;
        }

        LocalTime time = parseTime();
        if (time == null) {
            return;
        }

        Integer durationMinutes = parseDurationMinutes();
        if (durationMinutes == null) {
            return;
        }

        Task task = new Task(title, descriptionField.getText().trim(), LocalDateTime.of(dueDate, time), durationMinutes);
        task.setTaskType(taskTypeBox.getValue());
        task.setGoals(parseGoals());
        taskService.addTask(task);
        clearForm();
        refreshTasks();
        taskTable.getSelectionModel().select(task);
    }


    private void editSelectedTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            return;
        }

        editingTask = selectedTask;
        taskTypeBox.setValue(selectedTask.getTaskType());
        titleField.setText(selectedTask.getTask());
        descriptionField.setText(selectedTask.getDescription());
        goalsField.setText(String.join(System.lineSeparator(), selectedTask.getGoals()));

        if (selectedTask.getDueTime() != null) {
            dueDatePicker.setValue(selectedTask.getDueTime().toLocalDate());
            timeField.setText(selectedTask.getDueTime().toLocalTime().format(TIME_INPUT_FORMATTER));
        }
        durationField.setText(String.valueOf(selectedTask.getExpectedDurationMinutes()));
        setEditMode(true);
    }

    private void saveEditedTask() {
        if (editingTask == null) {
            return;
        }

        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showWarning("Please enter a title.");
            return;
        }

        LocalDate dueDate = dueDatePicker.getValue();
        if (dueDate == null) {
            showWarning("Please select a date.");
            return;
        }

        LocalTime time = parseTime();
        if (time == null) {
            return;
        }

        Integer durationMinutes = parseDurationMinutes();
        if (durationMinutes == null) {
            return;
        }

        Task savedTask = editingTask;
        savedTask.setTask(title);
        savedTask.setTaskType(taskTypeBox.getValue());
        savedTask.setDescription(descriptionField.getText().trim());
        savedTask.setGoals(parseGoals());
        savedTask.setDueTime(LocalDateTime.of(dueDate, time));
        savedTask.setExpectedDurationMinutes(durationMinutes);

        taskService.saveTasks();
        editingTask = null;
        clearForm();
        setEditMode(false);
        refreshTasks();
        taskTable.getSelectionModel().select(savedTask);
    }

    private void cancelEditingTask() {
        editingTask = null;
        clearForm();
        setEditMode(false);
    }

    private void setEditMode(boolean editing) {
        if (formTitle != null) {
            formTitle.setText(editing ? "Edit Task" : "New Task");
        }
        if (addButton != null) {
            addButton.setVisible(!editing);
            addButton.setManaged(!editing);
        }
        if (saveButton != null) {
            saveButton.setVisible(editing);
            saveButton.setManaged(editing);
        }
        if (cancelEditButton != null) {
            cancelEditButton.setVisible(editing);
            cancelEditButton.setManaged(editing);
        }
    }

    private LocalTime parseTime() {
        try {
            return LocalTime.parse(timeField.getText().trim(), TIME_INPUT_FORMATTER);
        } catch (DateTimeParseException exception) {
            showWarning("Please enter the time as HH:mm.");
            return null;
        }
    }

    private Integer parseDurationMinutes() {
        String value = durationField.getText().trim();
        if (value.isEmpty()) {
            showWarning("Please enter the expected work duration in minutes.");
            return null;
        }

        try {
            int minutes = Integer.parseInt(value);
            if (minutes <= 0) {
                showWarning("Work duration must be greater than 0 minutes.");
                return null;
            }
            return minutes;
        } catch (NumberFormatException exception) {
            showWarning("Work duration must be a whole number of minutes.");
            return null;
        }
    }

    private Button createStatusButton(Task task) {
        Button statusButton = new Button(statusText(task));
        statusButton.setMaxWidth(Double.MAX_VALUE);
        statusButton.setStyle(statusStyle(task));
        statusButton.setOnAction(event -> {
            advanceTaskStatus(task);
            taskService.saveTasks();
            refreshTasks();
            taskTable.getSelectionModel().select(task);
        });
        return statusButton;
    }

    private void advanceTaskStatus(Task task) {
        if (task.isCompleted()) {
            task.setCompleted(false);
            task.setStarted(false);
        } else if (task.isStarted()) {
            task.setCompleted(true);
            task.setStarted(true);
        } else {
            task.setStarted(true);
            task.setCompleted(false);
        }
    }

    private String statusText(Task task) {
        if (task.isCompleted()) {
            return "Done";
        }
        if (task.isStarted()) {
            return "Started";
        }
        return "Open";
    }

    private String statusStyle(Task task) {
        if (task.isCompleted()) {
            return "-fx-background-color: #2f9e44; -fx-text-fill: white; -fx-font-weight: 700;";
        }
        if (task.isStarted()) {
            return "-fx-background-color: #f2c94c; -fx-text-fill: #172033; -fx-font-weight: 700;";
        }
        return "-fx-background-color: #d64545; -fx-text-fill: white; -fx-font-weight: 700;";
    }
    private void toggleSelectedTaskCompleted() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            return;
        }

        selectedTask.setCompleted(!selectedTask.isCompleted());
        taskService.saveTasks();
        refreshTasks();
        taskTable.getSelectionModel().select(selectedTask);
    }

    private void deleteSelectedTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            return;
        }

        taskService.removeTask(selectedTask);
        refreshTasks();
    }


    private void openTodayPlanWindow() {
        LocalDate today = LocalDate.now();
        Stage todayStage = new Stage();

        Label titleLabel = new Label("Today Plan");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #172033;");

        Label dateLabel = new Label(today.format(DATE_HEADER_FORMATTER));
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #5d667a;");

        VBox scheduledTasks = createTodayTaskSection("Scheduled Today", taskService.getAllTasks().stream()
                .filter(task -> task.getDueTime() != null)
                .filter(task -> task.getDueTime().toLocalDate().equals(today))
                .sorted((first, second) -> first.getDueTime().compareTo(second.getDueTime()))
                .toList());

        VBox overdueTasks = createTodayTaskSection("Overdue Open Tasks", taskService.getAllTasks().stream()
                .filter(task -> !task.isCompleted())
                .filter(task -> task.getDueTime() != null)
                .filter(task -> task.getDueTime().toLocalDate().isBefore(today))
                .sorted((first, second) -> first.getDueTime().compareTo(second.getDueTime()))
                .toList());

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> todayStage.close());

        VBox content = new VBox(16, titleLabel, dateLabel, scheduledTasks, overdueTasks, closeButton);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f6f7fb;");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        todayStage.setTitle("TimePilot Today Plan");
        todayStage.setMinWidth(560);
        todayStage.setMinHeight(520);
        todayStage.setScene(new Scene(scrollPane, 680, 620));
        todayStage.show();
    }

    private VBox createTodayTaskSection(String title, List<Task> tasks) {
        Label sectionTitle = new Label(title);
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #172033;");

        VBox taskList = new VBox(8);
        if (tasks.isEmpty()) {
            Label emptyLabel = new Label("No tasks");
            emptyLabel.setStyle("-fx-text-fill: #7b8495;");
            taskList.getChildren().add(emptyLabel);
        } else {
            for (Task task : tasks) {
                taskList.getChildren().add(createTodayTaskRow(task));
            }
        }

        VBox section = new VBox(10, sectionTitle, taskList);
        section.setPadding(new Insets(14));
        section.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d8dee9; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        return section;
    }

    private VBox createTodayTaskRow(Task task) {
        Label titleLabel = new Label(formatTodayTaskTitle(task));
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #172033;");

        Label detailsLabel = new Label(task.getTaskType() + " | " + formatDuration(task.getExpectedDurationMinutes()) + " | " + (task.isCompleted() ? "Done" : task.isStarted() ? "Started" : "Open"));
        detailsLabel.setWrapText(true);
        detailsLabel.setStyle("-fx-text-fill: #5d667a;");

        String goals = formatGoals(task.getGoals());
        VBox row = goals.isEmpty()
                ? new VBox(3, titleLabel, detailsLabel)
                : new VBox(3, titleLabel, detailsLabel, createTodayGoalsLabel(goals));
        row.setPadding(new Insets(8));
        row.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #edf1f7; -fx-background-radius: 6px; -fx-border-radius: 6px;");
        return row;
    }

    private Label createTodayGoalsLabel(String goals) {
        Label goalsLabel = new Label(goals);
        goalsLabel.setWrapText(true);
        goalsLabel.setStyle("-fx-text-fill: #3a4252;");
        return goalsLabel;
    }

    private String formatTodayTaskTitle(Task task) {
        if (task.getDueTime() == null) {
            return task.getTask();
        }
        return task.getDueTime().format(DUE_TIME_FORMATTER) + " - " + task.getTask();
    }

    private void openTimerWindow() {
        Stage timerStage = new Stage();

        Label titleLabel = new Label("Focus Timer");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #172033;");

        Label modeLabel = new Label("Work");
        modeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #3a4252;");

        Label timeLabel = new Label(formatTimerTime(25 * 60));
        timeLabel.setAlignment(Pos.CENTER);
        timeLabel.setMaxWidth(Double.MAX_VALUE);
        timeLabel.setStyle("-fx-font-size: 56px; -fx-font-weight: 700; -fx-text-fill: #172033;");

        TextField workMinutesField = new TextField("25");
        workMinutesField.setPromptText("Minutes");
        TextField breakMinutesField = new TextField("5");
        breakMinutesField.setPromptText("Minutes");

        GridPane settingsGrid = new GridPane();
        settingsGrid.setHgap(10);
        settingsGrid.setVgap(8);
        settingsGrid.add(new Label("Work minutes"), 0, 0);
        settingsGrid.add(workMinutesField, 1, 0);
        settingsGrid.add(new Label("Break minutes"), 0, 1);
        settingsGrid.add(breakMinutesField, 1, 1);
        ColumnConstraints settingsLabelColumn = new ColumnConstraints();
        settingsLabelColumn.setMinWidth(110);
        ColumnConstraints settingsInputColumn = new ColumnConstraints();
        settingsInputColumn.setHgrow(Priority.ALWAYS);
        settingsGrid.getColumnConstraints().add(settingsLabelColumn);
        settingsGrid.getColumnConstraints().add(settingsInputColumn);

        int[] remainingSeconds = { 25 * 60 };
        boolean[] workMode = { true };
        Button startPauseButton = new Button("Start");
        Button resetButton = new Button("Reset");
        Button switchModeButton = new Button("Switch to Break");
        Button closeButton = new Button("Close");

        Runnable updateDisplay = () -> timeLabel.setText(formatTimerTime(remainingSeconds[0]));
        java.util.function.Supplier<Integer> selectedDuration = () -> parseTimerMinutes(
                workMode[0] ? workMinutesField : breakMinutesField,
                workMode[0] ? "work" : "break");

        Timeline timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), event -> {
            if (remainingSeconds[0] > 0) {
                remainingSeconds[0]--;
            }
            updateDisplay.run();

            if (remainingSeconds[0] == 0) {
                timeline.stop();
                startPauseButton.setText("Start");
                showTimerFinished(workMode[0] ? "Work time finished." : "Break finished.");
            }
        }));

        startPauseButton.setOnAction(event -> {
            if (timeline.getStatus() == Animation.Status.RUNNING) {
                timeline.stop();
                startPauseButton.setText("Start");
                return;
            }

            if (remainingSeconds[0] <= 0) {
                Integer minutes = selectedDuration.get();
                if (minutes == null) {
                    return;
                }
                remainingSeconds[0] = minutes * 60;
                updateDisplay.run();
            }

            timeline.play();
            startPauseButton.setText("Pause");
        });

        resetButton.setOnAction(event -> {
            timeline.stop();
            startPauseButton.setText("Start");
            Integer minutes = selectedDuration.get();
            if (minutes == null) {
                return;
            }
            remainingSeconds[0] = minutes * 60;
            updateDisplay.run();
        });

        switchModeButton.setOnAction(event -> {
            timeline.stop();
            startPauseButton.setText("Start");
            workMode[0] = !workMode[0];
            modeLabel.setText(workMode[0] ? "Work" : "Break");
            switchModeButton.setText(workMode[0] ? "Switch to Break" : "Switch to Work");
            Integer minutes = selectedDuration.get();
            remainingSeconds[0] = minutes == null ? 0 : minutes * 60;
            updateDisplay.run();
        });

        closeButton.setOnAction(event -> timerStage.close());

        HBox controls = new HBox(10, startPauseButton, resetButton, switchModeButton, closeButton);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(16, titleLabel, modeLabel, timeLabel, settingsGrid, controls);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f6f7fb;");

        timerStage.setOnCloseRequest(event -> timeline.stop());
        timerStage.setTitle("TimePilot Timer");
        timerStage.setMinWidth(380);
        timerStage.setMinHeight(320);
        timerStage.setScene(new Scene(root, 420, 340));
        timerStage.show();
    }

    private Integer parseTimerMinutes(TextField field, String label) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            showWarning("Please enter " + label + " minutes.");
            return null;
        }

        try {
            int minutes = Integer.parseInt(value);
            if (minutes <= 0) {
                showWarning("Timer minutes must be greater than 0.");
                return null;
            }
            return minutes;
        } catch (NumberFormatException exception) {
            showWarning("Timer minutes must be a whole number.");
            return null;
        }
    }

    private String formatTimerTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds);
    }

    private void showTimerFinished(String message) {
        playTimerAlertSound();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("TimePilot Timer");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void playTimerAlertSound() {
        Timeline alertSound = new Timeline(
                new KeyFrame(Duration.ZERO, event -> Toolkit.getDefaultToolkit().beep()),
                new KeyFrame(Duration.millis(350), event -> Toolkit.getDefaultToolkit().beep()),
                new KeyFrame(Duration.millis(700), event -> Toolkit.getDefaultToolkit().beep()));
        alertSound.play();
    }
    private void openCalendarWindow() {
        Stage calendarStage = new Stage();
        LocalDate currentDate = LocalDate.now();
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setMinSize(0, 0);
        root.setStyle("-fx-background-color: #f6f7fb;");

        renderCalendar(root, currentDate, "month", calendarStage);

        Scene scene = new Scene(root, 1200, 800);
        calendarStage.setTitle("TimePilot Calendar");
        calendarStage.setMinWidth(640);
        calendarStage.setMinHeight(420);
        calendarStage.setScene(scene);
        calendarStage.show();
    }

    private void renderCalendar(BorderPane root, LocalDate date, String view, Stage stage) {
        boolean weekView = "week".equals(view);
        root.setPadding(weekView ? new Insets(10) : new Insets(20));

        Label periodLabel = new Label(formatCalendarPeriod(date, view));
        periodLabel.setMinSize(0, 0);
        periodLabel.setMaxWidth(Double.MAX_VALUE);
        periodLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        periodLabel.setStyle(weekView ? "-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #172033;" : "-fx-font-size: 26px; -fx-font-weight: 700; -fx-text-fill: #172033;");

        Button previousButton = new Button("Previous");
        previousButton.setOnAction(event -> renderCalendar(root, shiftCalendarDate(date, view, -1), view, stage));

        Button todayButton = new Button("Today");
        todayButton.setOnAction(event -> renderCalendar(root, LocalDate.now(), view, stage));

        Button nextButton = new Button("Next");
        nextButton.setOnAction(event -> renderCalendar(root, shiftCalendarDate(date, view, 1), view, stage));

        Button monthButton = new Button("Month");
        monthButton.setDisable("month".equals(view));
        monthButton.setOnAction(event -> renderCalendar(root, date, "month", stage));

        Button weekButton = new Button("Week");
        weekButton.setDisable("week".equals(view));
        weekButton.setOnAction(event -> renderCalendar(root, date, "week", stage));

        Button dayButton = new Button("Day");
        dayButton.setDisable("day".equals(view));
        dayButton.setOnAction(event -> renderCalendar(root, date, "day", stage));

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> stage.close());

        FlowPane navigation = new FlowPane(8, 6, previousButton, todayButton, nextButton, monthButton, weekButton, dayButton, closeButton);
        navigation.setAlignment(Pos.CENTER_LEFT);
        navigation.setMinSize(0, 0);
        navigation.setMaxWidth(Double.MAX_VALUE);

        VBox top = new VBox(weekView ? 6 : 10, periodLabel, navigation);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(0, 0, weekView ? 8 : 18, 0));
        top.setMinSize(0, 0);
        root.setTop(top);

        if ("day".equals(view)) {
            root.setCenter(createDayScheduler(date));
        } else if ("week".equals(view)) {
            root.setCenter(createWeekCalendarGrid(date));
        } else {
            root.setCenter(createMonthCalendarGrid(YearMonth.from(date)));
        }
    }

    private String formatCalendarPeriod(LocalDate date, String view) {
        if ("day".equals(view)) {
            return date.format(DATE_HEADER_FORMATTER);
        }
        if ("week".equals(view)) {
            return formatWeekRange(date);
        }
        return YearMonth.from(date).format(MONTH_FORMATTER);
    }

    private LocalDate shiftCalendarDate(LocalDate date, String view, int amount) {
        if ("day".equals(view)) {
            return date.plusDays(amount);
        }
        if ("week".equals(view)) {
            return date.plusWeeks(amount);
        }
        return date.plusMonths(amount);
    }

    private GridPane createMonthCalendarGrid(YearMonth month) {
        GridPane calendarGrid = createCalendarBaseGrid(6);
        addWeekdayHeader(calendarGrid);

        Map<LocalDate, List<Task>> tasksByDate = getTasksByDate();
        LocalDate firstVisibleDate = month.atDay(1).minusDays(month.atDay(1).getDayOfWeek().getValue() - 1L);
        for (int index = 0; index < 42; index++) {
            LocalDate date = firstVisibleDate.plusDays(index);
            VBox dayCell = createCalendarDayCell(date, YearMonth.from(date).equals(month), tasksByDate.getOrDefault(date, List.of()));
            calendarGrid.add(dayCell, index % 7, (index / 7) + 1);
        }

        return calendarGrid;
    }

    private Pane createWeekCalendarGrid(LocalDate date) {
        LocalDate weekStart = startOfWeek(date);
        Canvas canvas = new Canvas();
        Pane canvasPane = new Pane(canvas);
        canvasPane.setMinSize(0, 0);
        canvasPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        canvasPane.setStyle("-fx-background-color: #ffffff;");

        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> drawWeekCalendar(canvas, date, weekStart));
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> drawWeekCalendar(canvas, date, weekStart));
        drawWeekCalendar(canvas, date, weekStart);

        return canvasPane;
    }

    private void drawWeekCalendar(Canvas canvas, LocalDate selectedDate, LocalDate weekStart) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.clearRect(0, 0, width, height);
        graphics.setFill(Color.web("#ffffff"));
        graphics.fillRect(0, 0, width, height);

        double timeColumnWidth = clamp(width * 0.08, 34, 58);
        double headerHeight = clamp(height * 0.07, 20, 34);
        double bodyHeight = Math.max(1, height - headerHeight);
        double dayWidth = Math.max(1, (width - timeColumnWidth) / 7.0);
        double hourHeight = bodyHeight / (SCHEDULE_END_HOUR - SCHEDULE_START_HOUR);
        double smallFont = clamp(Math.min(dayWidth, hourHeight) * 0.28, 7, 10);
        double headerFont = clamp(dayWidth * 0.08, 8, 12);

        graphics.setFill(Color.web("#edf1f7"));
        graphics.fillRect(0, 0, width, headerHeight);
        graphics.setStroke(Color.web("#d8dee9"));
        graphics.setLineWidth(1);
        graphics.strokeRect(0.5, 0.5, width - 1, height - 1);

        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setTextBaseline(VPos.CENTER);
        graphics.setFont(Font.font("System", FontWeight.BOLD, headerFont));
        graphics.setFill(Color.web("#3a4252"));
        graphics.fillText("Time", timeColumnWidth / 2, headerHeight / 2);

        for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
            LocalDate day = weekStart.plusDays(dayIndex);
            double x = timeColumnWidth + (dayIndex * dayWidth);
            if (day.equals(selectedDate)) {
                graphics.setFill(Color.web("#dfeaff"));
                graphics.fillRect(x, 0, dayWidth, headerHeight);
            }
            graphics.setStroke(Color.web("#d8dee9"));
            graphics.strokeLine(x, 0, x, height);
            graphics.setFill(Color.web(day.equals(selectedDate) ? "#172033" : "#3a4252"));
            graphics.fillText(day.format(WEEK_DAY_HEADER_FORMATTER), x + dayWidth / 2, headerHeight / 2);
        }

        graphics.setStroke(Color.web("#edf1f7"));
        graphics.setFont(Font.font("System", FontWeight.BOLD, smallFont));
        graphics.setTextAlign(TextAlignment.RIGHT);
        graphics.setTextBaseline(VPos.TOP);
        for (int hour = SCHEDULE_START_HOUR; hour <= SCHEDULE_END_HOUR; hour++) {
            double y = headerHeight + ((hour - SCHEDULE_START_HOUR) * hourHeight);
            graphics.setStroke(Color.web(hour == SCHEDULE_START_HOUR || hour == SCHEDULE_END_HOUR ? "#c9d2df" : "#edf1f7"));
            graphics.strokeLine(0, y, width, y);
            graphics.setFill(Color.web("#3a4252"));
            graphics.fillText(String.format(Locale.ENGLISH, "%02d:00", hour), timeColumnWidth - 4, Math.min(y + 2, height - smallFont - 1));
        }

        Map<LocalDate, List<Task>> tasksByDay = taskService.getAllTasks().stream()
                .filter(task -> task.getDueTime() != null)
                .filter(task -> !task.getDueTime().toLocalDate().isBefore(weekStart))
                .filter(task -> !task.getDueTime().toLocalDate().isAfter(weekStart.plusDays(6)))
                .filter(this::isVisibleInWeekPlanner)
                .sorted(Comparator.comparing(Task::getDueTime))
                .collect(Collectors.groupingBy(task -> task.getDueTime().toLocalDate()));

        for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
            LocalDate day = weekStart.plusDays(dayIndex);
            List<WeekTaskPlacement> placements = calculateWeekTaskPlacements(tasksByDay.getOrDefault(day, List.of()));
            for (WeekTaskPlacement placement : placements) {
                drawWeekTask(graphics, placement, weekStart, timeColumnWidth, headerHeight, dayWidth, hourHeight, height);
            }
        }
    }

    private boolean isVisibleInWeekPlanner(Task task) {
        LocalTime time = task.getDueTime().toLocalTime();
        return !time.isBefore(LocalTime.of(SCHEDULE_START_HOUR, 0)) && !time.isAfter(LocalTime.of(SCHEDULE_END_HOUR, 0));
    }

    private List<WeekTaskPlacement> calculateWeekTaskPlacements(List<Task> tasks) {
        List<Task> sortedTasks = tasks.stream()
                .sorted(Comparator.comparing(Task::getDueTime))
                .toList();
        List<WeekTaskPlacement> placements = new ArrayList<>();
        List<Task> overlapGroup = new ArrayList<>();
        int groupEndMinute = -1;

        for (Task task : sortedTasks) {
            int startMinute = startMinuteOfDay(task);
            int endMinute = endMinuteOfDay(task);
            if (!overlapGroup.isEmpty() && startMinute >= groupEndMinute) {
                placements.addAll(placeOverlapGroup(overlapGroup));
                overlapGroup.clear();
                groupEndMinute = -1;
            }
            overlapGroup.add(task);
            groupEndMinute = Math.max(groupEndMinute, endMinute);
        }

        if (!overlapGroup.isEmpty()) {
            placements.addAll(placeOverlapGroup(overlapGroup));
        }
        return placements;
    }

    private List<WeekTaskPlacement> placeOverlapGroup(List<Task> tasks) {
        List<WeekTaskPlacement> placements = new ArrayList<>();
        List<Integer> laneEndMinutes = new ArrayList<>();

        for (Task task : tasks) {
            int startMinute = startMinuteOfDay(task);
            int endMinute = endMinuteOfDay(task);
            int lane = firstAvailableLane(laneEndMinutes, startMinute);
            if (lane == laneEndMinutes.size()) {
                laneEndMinutes.add(endMinute);
            } else {
                laneEndMinutes.set(lane, endMinute);
            }
            placements.add(new WeekTaskPlacement(task, lane));
        }

        int laneCount = Math.max(1, laneEndMinutes.size());
        boolean overlaps = laneCount > 1;
        placements.forEach(placement -> placement.setOverlapInfo(laneCount, overlaps));
        return placements;
    }

    private int firstAvailableLane(List<Integer> laneEndMinutes, int startMinute) {
        for (int lane = 0; lane < laneEndMinutes.size(); lane++) {
            if (laneEndMinutes.get(lane) <= startMinute) {
                return lane;
            }
        }
        return laneEndMinutes.size();
    }

    private void drawWeekTask(GraphicsContext graphics, WeekTaskPlacement placement, LocalDate weekStart, double timeColumnWidth, double headerHeight, double dayWidth, double hourHeight, double height) {
        Task task = placement.task();
        LocalDate taskDate = task.getDueTime().toLocalDate();
        LocalTime taskTime = task.getDueTime().toLocalTime();
        int dayIndex = (int) (taskDate.toEpochDay() - weekStart.toEpochDay());
        double startHour = taskTime.getHour() + (taskTime.getMinute() / 60.0);
        double laneGap = placement.overlaps() ? 2 : 0;
        double availableWidth = Math.max(2, dayWidth - 4);
        double laneWidth = Math.max(2, (availableWidth - ((placement.laneCount() - 1) * laneGap)) / placement.laneCount());
        double x = timeColumnWidth + (dayIndex * dayWidth) + 2 + (placement.lane() * (laneWidth + laneGap));
        double y = headerHeight + ((startHour - SCHEDULE_START_HOUR) * hourHeight);
        double durationMinutes = Math.max(15, task.getExpectedDurationMinutes());
        double taskHeight = Math.max(7, (durationMinutes / 60.0) * hourHeight);
        taskHeight = Math.min(taskHeight, Math.max(4, height - y - 2));
        if (taskHeight <= 0) {
            return;
        }

        graphics.setFill(task.isCompleted() ? Color.web("#d9f2df") : task.isStarted() ? Color.web("#fff1b8") : Color.web("#e7f0ff"));
        graphics.fillRoundRect(x, y + 1, laneWidth, taskHeight, 5, 5);
        graphics.setStroke(placement.overlaps() ? Color.web("#d64545") : task.isCompleted() ? Color.web("#2f9e44") : task.isStarted() ? Color.web("#d39e00") : Color.web("#7da9f8"));
        graphics.setLineWidth(placement.overlaps() ? 2 : 1);
        graphics.strokeRoundRect(x, y + 1, laneWidth, taskHeight, 5, 5);
        graphics.setLineWidth(1);

        if (placement.overlaps() && laneWidth >= 8) {
            graphics.setFill(Color.web("#d64545"));
            graphics.fillRoundRect(x + laneWidth - 5, y + 3, 3, Math.max(3, taskHeight - 5), 2, 2);
        }

        if (taskHeight >= 10 && laneWidth >= 16) {
            double fontSize = clamp(Math.min(taskHeight * 0.48, laneWidth * 0.10), 6, 10);
            graphics.setFont(Font.font("System", FontWeight.NORMAL, fontSize));
            graphics.setFill(Color.web("#172033"));
            graphics.setTextAlign(TextAlignment.LEFT);
            graphics.setTextBaseline(VPos.TOP);
            String taskText = (placement.overlaps() ? "! " : "") + taskTime.format(TIME_INPUT_FORMATTER) + " " + task.getTask();
            graphics.fillText(shortenForWidth(taskText, laneWidth - 8, fontSize), x + 3, y + 3);
        }
    }

    private int startMinuteOfDay(Task task) {
        LocalTime time = task.getDueTime().toLocalTime();
        return time.getHour() * 60 + time.getMinute();
    }

    private int endMinuteOfDay(Task task) {
        return startMinuteOfDay(task) + Math.max(15, task.getExpectedDurationMinutes());
    }

    private static class WeekTaskPlacement {
        private final Task task;
        private final int lane;
        private int laneCount = 1;
        private boolean overlaps;

        private WeekTaskPlacement(Task task, int lane) {
            this.task = task;
            this.lane = lane;
        }

        private Task task() {
            return task;
        }

        private int lane() {
            return lane;
        }

        private int laneCount() {
            return laneCount;
        }

        private boolean overlaps() {
            return overlaps;
        }

        private void setOverlapInfo(int laneCount, boolean overlaps) {
            this.laneCount = laneCount;
            this.overlaps = overlaps;
        }
    }

    private String shortenForWidth(String text, double width, double fontSize) {
        int maxCharacters = (int) Math.max(1, width / Math.max(1, fontSize * 0.55));
        if (text.length() <= maxCharacters) {
            return text;
        }
        if (maxCharacters <= 1) {
            return ".";
        }
        return text.substring(0, maxCharacters - 1) + ".";
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    private ScrollPane createDayScheduler(LocalDate date) {
        VBox schedule = new VBox(8);
        schedule.setStyle("-fx-background-color: #d8dee9;");

        Map<Integer, List<Task>> tasksByHour = taskService.getAllTasks().stream()
                .filter(task -> task.getDueTime() != null)
                .filter(task -> task.getDueTime().toLocalDate().equals(date))
                .sorted((first, second) -> first.getDueTime().compareTo(second.getDueTime()))
                .collect(Collectors.groupingBy(task -> task.getDueTime().getHour()));

        for (int hour = SCHEDULE_START_HOUR; hour <= SCHEDULE_END_HOUR; hour++) {
            schedule.getChildren().add(createHourSlot(hour, tasksByHour.getOrDefault(hour, List.of())));
        }

        ScrollPane scrollPane = new ScrollPane(schedule);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scrollPane;
    }

    private HBox createHourSlot(int hour, List<Task> tasks) {
        Label hourLabel = new Label(String.format(Locale.ENGLISH, "%02d:00", hour));
        hourLabel.setMinWidth(70);
        hourLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #3a4252;");

        VBox taskList = new VBox(6);
        taskList.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(taskList, Priority.ALWAYS);

        if (tasks.isEmpty()) {
            Label emptyLabel = new Label("No tasks scheduled");
            emptyLabel.setStyle("-fx-text-fill: #7b8495;");
            taskList.getChildren().add(emptyLabel);
        } else {
            for (Task task : tasks) {
                Label taskLabel = new Label(formatCalendarTask(task));
                taskLabel.setWrapText(true);
                taskLabel.setMaxWidth(Double.MAX_VALUE);
                taskLabel.setStyle("-fx-background-color: #e7f0ff; -fx-background-radius: 6px; -fx-padding: 6 8; -fx-text-fill: #172033;");
                taskList.getChildren().add(taskLabel);
            }
        }

        HBox slot = new HBox(12, hourLabel, taskList);
        slot.setAlignment(Pos.TOP_LEFT);
        slot.setPadding(new Insets(10));
        slot.setMinHeight(72);
        slot.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d8dee9;");
        return slot;
    }

    private GridPane createCalendarBaseGrid(int dayRows) {
        GridPane calendarGrid = new GridPane();
        calendarGrid.setHgap(8);
        calendarGrid.setVgap(8);
        calendarGrid.setStyle("-fx-background-color: #d8dee9;");

        for (int column = 0; column < 7; column++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(100.0 / 7.0);
            columnConstraints.setHgrow(Priority.ALWAYS);
            calendarGrid.getColumnConstraints().add(columnConstraints);
        }

        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(36);
        calendarGrid.getRowConstraints().add(headerRow);

        for (int row = 0; row < dayRows; row++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100.0 / dayRows);
            rowConstraints.setVgrow(Priority.ALWAYS);
            calendarGrid.getRowConstraints().add(rowConstraints);
        }

        return calendarGrid;
    }

    private void addWeekdayHeader(GridPane calendarGrid) {
        String[] weekdays = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        for (int column = 0; column < weekdays.length; column++) {
            Label label = new Label(weekdays[column]);
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER);
            label.setStyle("-fx-background-color: #edf1f7; -fx-font-weight: 700; -fx-text-fill: #3a4252;");
            calendarGrid.add(label, column, 0);
        }
    }

    private Map<LocalDate, List<Task>> getTasksByDate() {
        return taskService.getAllTasks().stream()
                .filter(task -> task.getDueTime() != null)
                .collect(Collectors.groupingBy(task -> task.getDueTime().toLocalDate()));
    }

    private VBox createCalendarDayCell(LocalDate date, boolean currentPeriod, List<Task> tasks) {
        Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dateLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #172033;");

        VBox taskList = new VBox(4);
        for (Task task : tasks) {
            Label taskLabel = new Label(formatCalendarTask(task));
            taskLabel.setWrapText(true);
            taskLabel.setMaxWidth(Double.MAX_VALUE);
            taskLabel.setStyle("-fx-background-color: #e7f0ff; -fx-background-radius: 6px; -fx-padding: 4 6; -fx-text-fill: #172033;");
            taskList.getChildren().add(taskLabel);
        }

        ScrollPane scrollPane = new ScrollPane(taskList);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox dayCell = new VBox(8, dateLabel, scrollPane);
        dayCell.setPadding(new Insets(8));
        dayCell.setMinHeight(100);
        dayCell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        dayCell.setStyle(dateStyle(date, currentPeriod));
        return dayCell;
    }

    private String dateStyle(LocalDate date, boolean currentPeriod) {
        String background = currentPeriod ? "#ffffff" : "#eef1f6";
        String border = date.equals(LocalDate.now()) ? "#2f6fed" : "#d8dee9";
        return "-fx-background-color: " + background + "; -fx-border-color: " + border + "; -fx-border-width: 1.5;";
    }

    private LocalDate startOfWeek(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1L);
    }

    private String formatWeekRange(LocalDate date) {
        LocalDate weekStart = startOfWeek(date);
        LocalDate weekEnd = weekStart.plusDays(6);
        return weekStart.format(WEEK_RANGE_FORMATTER) + " - " + weekEnd.format(WEEK_RANGE_FORMATTER) + ", " + weekEnd.getYear();
    }
    private String formatCalendarTask(Task task) {
        String time = task.getDueTime() == null ? "" : task.getDueTime().toLocalTime().format(TIME_INPUT_FORMATTER) + " ";
        return time + task.getTask() + " (" + formatDuration(task.getExpectedDurationMinutes()) + ")";
    }


    private String compactCalendarTask(Task task) {
        String time = task.getDueTime() == null ? "" : task.getDueTime().toLocalTime().format(TIME_INPUT_FORMATTER) + " ";
        return time + task.getTask();
    }
    private List<String> parseGoals() {
        return goalsField.getText().lines()
                .map(String::trim)
                .filter(goal -> !goal.isEmpty())
                .toList();
    }

    private String formatGoals(List<String> goals) {
        if (goals == null || goals.isEmpty()) {
            return "";
        }
        return String.join(", ", goals);
    }

    private void refreshTasks() {
        List<Task> tasks = openOnlyCheckBox != null && openOnlyCheckBox.isSelected()
                ? taskService.getOpenTasks()
                : taskService.getAllTasks();
        visibleTasks.setAll(tasks);
    }

    private void clearForm() {
        taskTypeBox.setValue("ToDo");
        titleField.clear();
        descriptionField.clear();
        goalsField.clear();
        dueDatePicker.setValue(LocalDate.now());
        timeField.setText(LocalTime.now().format(TIME_INPUT_FORMATTER));
        durationField.clear();
    }

    private String formatDueTime(LocalDateTime dueTime) {
        return dueTime == null ? "" : dueTime.format(DUE_TIME_FORMATTER);
    }

    private String formatDuration(int minutes) {
        if (minutes <= 0) {
            return "0 min";
        }

        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        if (hours == 0) {
            return minutes + " min";
        }
        if (remainingMinutes == 0) {
            return hours + " h";
        }
        return hours + " h " + remainingMinutes + " min";
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("TimePilot");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}





























package ch.jonas.timepilot;

import java.awt.Toolkit;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class TimePilotApp extends Application {
    private static final DateTimeFormatter DUE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter TIME_INPUT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter WEEK_RANGE_FORMATTER = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_HEADER_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter WEEK_DAY_HEADER_FORMATTER = DateTimeFormatter.ofPattern("EEE M/d", Locale.ENGLISH);

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

        TableColumn<Task, CheckBox> startedColumn = new TableColumn<>("Started");
        startedColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(createStartedCheckBox(cell.getValue())));
        startedColumn.setMinWidth(80);

        TableColumn<Task, CheckBox> completedColumn = new TableColumn<>("Done");
        completedColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(createCompletedCheckBox(cell.getValue())));
        completedColumn.setMinWidth(70);

        taskTable.getColumns().add(typeColumn);
        taskTable.getColumns().add(titleColumn);
        taskTable.getColumns().add(descriptionColumn);
        taskTable.getColumns().add(goalsColumn);
        taskTable.getColumns().add(dueTimeColumn);
        taskTable.getColumns().add(durationColumn);
        taskTable.getColumns().add(startedColumn);
        taskTable.getColumns().add(completedColumn);
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

        Button toggleCompletedButton = new Button("Toggle Done");
        toggleCompletedButton.setOnAction(event -> toggleSelectedTaskCompleted());

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(event -> deleteSelectedTask());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10, openOnlyCheckBox, spacer, todayButton, calendarButton, timerButton, editButton, toggleCompletedButton, deleteButton);
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

    private CheckBox createStartedCheckBox(Task task) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(task.isStarted());
        checkBox.setOnAction(event -> {
            task.setStarted(checkBox.isSelected());
            taskService.saveTasks();
        });
        return checkBox;
    }

    private CheckBox createCompletedCheckBox(Task task) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(task.isCompleted());
        checkBox.setOnAction(event -> {
            task.setCompleted(checkBox.isSelected());
            taskService.saveTasks();
            refreshTasks();
            taskTable.getSelectionModel().select(task);
        });
        return checkBox;
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
        root.setStyle("-fx-background-color: #f6f7fb;");

        renderCalendar(root, currentDate, "month", calendarStage);

        Scene scene = new Scene(root, 1200, 800);
        calendarStage.setTitle("TimePilot Calendar");
        calendarStage.setMinWidth(980);
        calendarStage.setMinHeight(680);
        calendarStage.setScene(scene);
        calendarStage.show();
    }

    private void renderCalendar(BorderPane root, LocalDate date, String view, Stage stage) {
        Label periodLabel = new Label(formatCalendarPeriod(date, view));
        periodLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: 700; -fx-text-fill: #172033;");

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

        HBox navigation = new HBox(10, previousButton, todayButton, nextButton, monthButton, weekButton, dayButton, closeButton);
        HBox top = new HBox(18, periodLabel, navigation);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(0, 0, 18, 0));
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

    private ScrollPane createWeekCalendarGrid(LocalDate date) {
        LocalDate weekStart = startOfWeek(date);
        GridPane weekGrid = new GridPane();
        weekGrid.setHgap(1);
        weekGrid.setVgap(1);
        weekGrid.setStyle("-fx-background-color: #d8dee9;");

        ColumnConstraints timeColumn = new ColumnConstraints();
        timeColumn.setMinWidth(76);
        timeColumn.setPrefWidth(76);
        weekGrid.getColumnConstraints().add(timeColumn);

        for (int column = 0; column < 7; column++) {
            ColumnConstraints dayColumn = new ColumnConstraints();
            dayColumn.setPercentWidth(100.0 / 7.0);
            dayColumn.setHgrow(Priority.ALWAYS);
            weekGrid.getColumnConstraints().add(dayColumn);
        }

        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(42);
        weekGrid.getRowConstraints().add(headerRow);

        Label cornerLabel = new Label("Time");
        cornerLabel.setMaxWidth(Double.MAX_VALUE);
        cornerLabel.setAlignment(Pos.CENTER);
        cornerLabel.setStyle("-fx-background-color: #edf1f7; -fx-font-weight: 700; -fx-text-fill: #3a4252;");
        weekGrid.add(cornerLabel, 0, 0);

        for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
            LocalDate day = weekStart.plusDays(dayIndex);
            Label dayLabel = new Label(day.format(WEEK_DAY_HEADER_FORMATTER));
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setStyle(date.equals(day) ? "-fx-background-color: #dfeaff; -fx-font-weight: 700; -fx-text-fill: #172033;" : "-fx-background-color: #edf1f7; -fx-font-weight: 700; -fx-text-fill: #3a4252;");
            weekGrid.add(dayLabel, dayIndex + 1, 0);
        }

        Map<LocalDate, Map<Integer, List<Task>>> tasksByDayAndHour = taskService.getAllTasks().stream()
                .filter(task -> task.getDueTime() != null)
                .filter(task -> !task.getDueTime().toLocalDate().isBefore(weekStart))
                .filter(task -> !task.getDueTime().toLocalDate().isAfter(weekStart.plusDays(6)))
                .sorted((first, second) -> first.getDueTime().compareTo(second.getDueTime()))
                .collect(Collectors.groupingBy(
                        task -> task.getDueTime().toLocalDate(),
                        Collectors.groupingBy(task -> task.getDueTime().getHour())));

        for (int hour = 0; hour < 24; hour++) {
            RowConstraints hourRow = new RowConstraints();
            hourRow.setMinHeight(72);
            hourRow.setVgrow(Priority.ALWAYS);
            weekGrid.getRowConstraints().add(hourRow);

            Label hourLabel = new Label(String.format(Locale.ENGLISH, "%02d:00", hour));
            hourLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            hourLabel.setAlignment(Pos.TOP_CENTER);
            hourLabel.setPadding(new Insets(10, 4, 0, 4));
            hourLabel.setStyle("-fx-background-color: #f8fafc; -fx-font-weight: 700; -fx-text-fill: #3a4252;");
            weekGrid.add(hourLabel, 0, hour + 1);

            for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
                LocalDate day = weekStart.plusDays(dayIndex);
                List<Task> hourTasks = tasksByDayAndHour
                        .getOrDefault(day, Map.of())
                        .getOrDefault(hour, List.of());
                weekGrid.add(createWeekHourCell(day, hourTasks), dayIndex + 1, hour + 1);
            }
        }

        ScrollPane scrollPane = new ScrollPane(weekGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scrollPane;
    }

    private VBox createWeekHourCell(LocalDate date, List<Task> tasks) {
        VBox taskList = new VBox(5);
        taskList.setPadding(new Insets(6));
        taskList.setMinHeight(72);
        taskList.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        taskList.setStyle(date.equals(LocalDate.now()) ? "-fx-background-color: #fbfdff; -fx-border-color: #b8ccff;" : "-fx-background-color: #ffffff; -fx-border-color: #edf1f7;");

        for (Task task : tasks) {
            Label taskLabel = new Label(formatCalendarTask(task));
            taskLabel.setWrapText(true);
            taskLabel.setMaxWidth(Double.MAX_VALUE);
            taskLabel.setStyle("-fx-background-color: #e7f0ff; -fx-background-radius: 6px; -fx-padding: 5 7; -fx-text-fill: #172033;");
            taskList.getChildren().add(taskLabel);
        }

        return taskList;
    }
    private ScrollPane createDayScheduler(LocalDate date) {
        VBox schedule = new VBox(8);
        schedule.setStyle("-fx-background-color: #d8dee9;");

        Map<Integer, List<Task>> tasksByHour = taskService.getAllTasks().stream()
                .filter(task -> task.getDueTime() != null)
                .filter(task -> task.getDueTime().toLocalDate().equals(date))
                .sorted((first, second) -> first.getDueTime().compareTo(second.getDueTime()))
                .collect(Collectors.groupingBy(task -> task.getDueTime().getHour()));

        for (int hour = 0; hour < 24; hour++) {
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

















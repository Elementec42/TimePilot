package ch.jonas.timepilot;

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

public class TimePilotApp extends Application {
    private static final DateTimeFormatter DUE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter TIME_INPUT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter WEEK_RANGE_FORMATTER = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);

    private final TaskService taskService = new TaskService();
    private final ObservableList<Task> visibleTasks = FXCollections.observableArrayList();

    private TableView<Task> taskTable;
    private TextField titleField;
    private TextArea descriptionField;
    private DatePicker dueDatePicker;
    private TextField timeField;
    private TextField durationField;
    private CheckBox openOnlyCheckBox;

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

        TableColumn<Task, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getTask()));
        titleColumn.setMinWidth(150);

        TableColumn<Task, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getDescription()));
        descriptionColumn.setMinWidth(240);

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

        taskTable.getColumns().add(titleColumn);
        taskTable.getColumns().add(descriptionColumn);
        taskTable.getColumns().add(dueTimeColumn);
        taskTable.getColumns().add(durationColumn);
        taskTable.getColumns().add(startedColumn);
        taskTable.getColumns().add(completedColumn);
        BorderPane.setMargin(taskTable, new Insets(0, 18, 0, 0));
        return taskTable;
    }

    private VBox createTaskForm() {
        Label formTitle = new Label("New Task");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #172033;");

        titleField = new TextField();
        titleField.setPromptText("Title");
        titleField.setPrefColumnCount(22);

        descriptionField = new TextArea();
        descriptionField.setPromptText("Description");
        descriptionField.setPrefRowCount(5);
        descriptionField.setWrapText(true);

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

        Button addButton = new Button("Add");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setDefaultButton(true);
        addButton.setOnAction(event -> addTask());

        VBox form = new VBox(12, formTitle, titleField, descriptionField, timeGrid, addButton);
        form.setPadding(new Insets(16));
        form.setPrefWidth(320);
        form.setMaxWidth(340);
        form.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d8dee9; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        return form;
    }

    private HBox createActions() {
        openOnlyCheckBox = new CheckBox("Open tasks only");
        openOnlyCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> refreshTasks());

        Button calendarButton = new Button("Calendar");
        calendarButton.setOnAction(event -> openCalendarWindow());

        Button toggleCompletedButton = new Button("Toggle Done");
        toggleCompletedButton.setOnAction(event -> toggleSelectedTaskCompleted());

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(event -> deleteSelectedTask());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10, openOnlyCheckBox, spacer, calendarButton, toggleCompletedButton, deleteButton);
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
        taskService.addTask(task);
        clearForm();
        refreshTasks();
        taskTable.getSelectionModel().select(task);
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

    private void openCalendarWindow() {
        Stage calendarStage = new Stage();
        LocalDate currentDate = LocalDate.now();
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f6f7fb;");

        renderCalendar(root, currentDate, false, calendarStage);

        Scene scene = new Scene(root, 1200, 800);
        calendarStage.setTitle("TimePilot Calendar");
        calendarStage.setMinWidth(980);
        calendarStage.setMinHeight(680);
        calendarStage.setScene(scene);
        calendarStage.show();
    }

    private void renderCalendar(BorderPane root, LocalDate date, boolean weekView, Stage stage) {
        Label periodLabel = new Label(weekView ? formatWeekRange(date) : YearMonth.from(date).format(MONTH_FORMATTER));
        periodLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: 700; -fx-text-fill: #172033;");

        Button previousButton = new Button("Previous");
        previousButton.setOnAction(event -> renderCalendar(root, weekView ? date.minusWeeks(1) : date.minusMonths(1), weekView, stage));

        Button todayButton = new Button("Today");
        todayButton.setOnAction(event -> renderCalendar(root, LocalDate.now(), weekView, stage));

        Button nextButton = new Button("Next");
        nextButton.setOnAction(event -> renderCalendar(root, weekView ? date.plusWeeks(1) : date.plusMonths(1), weekView, stage));

        Button monthButton = new Button("Month");
        monthButton.setDisable(!weekView);
        monthButton.setOnAction(event -> renderCalendar(root, date, false, stage));

        Button weekButton = new Button("Week");
        weekButton.setDisable(weekView);
        weekButton.setOnAction(event -> renderCalendar(root, date, true, stage));

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> stage.close());

        HBox navigation = new HBox(10, previousButton, todayButton, nextButton, monthButton, weekButton, closeButton);
        HBox top = new HBox(18, periodLabel, navigation);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(0, 0, 18, 0));
        root.setTop(top);
        root.setCenter(weekView ? createWeekCalendarGrid(date) : createMonthCalendarGrid(YearMonth.from(date)));
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

    private GridPane createWeekCalendarGrid(LocalDate date) {
        GridPane calendarGrid = createCalendarBaseGrid(1);
        addWeekdayHeader(calendarGrid);

        Map<LocalDate, List<Task>> tasksByDate = getTasksByDate();
        LocalDate weekStart = startOfWeek(date);
        for (int index = 0; index < 7; index++) {
            LocalDate day = weekStart.plusDays(index);
            VBox dayCell = createCalendarDayCell(day, true, tasksByDate.getOrDefault(day, List.of()));
            calendarGrid.add(dayCell, index, 1);
        }

        return calendarGrid;
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

    private void refreshTasks() {
        List<Task> tasks = openOnlyCheckBox != null && openOnlyCheckBox.isSelected()
                ? taskService.getOpenTasks()
                : taskService.getAllTasks();
        visibleTasks.setAll(tasks);
    }

    private void clearForm() {
        titleField.clear();
        descriptionField.clear();
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

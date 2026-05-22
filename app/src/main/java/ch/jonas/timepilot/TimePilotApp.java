package ch.jonas.timepilot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import ch.jonas.timepilot.model.Task;
import ch.jonas.timepilot.service.TaskService;
import javafx.application.Application;
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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TimePilotApp extends Application {
    private static final DateTimeFormatter DUE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final TaskService taskService = new TaskService();
    private final ObservableList<Task> visibleTasks = FXCollections.observableArrayList();

    private TableView<Task> taskTable;
    private TextField titleField;
    private TextArea descriptionField;
    private DatePicker dueDatePicker;
    private Spinner<Integer> hourSpinner;
    private Spinner<Integer> minuteSpinner;
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

        Scene scene = new Scene(root, 980, 640);
        stage.setTitle("TimePilot");
        stage.setMinWidth(860);
        stage.setMinHeight(560);
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
        titleColumn.setMinWidth(160);

        TableColumn<Task, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getDescription()));
        descriptionColumn.setMinWidth(260);

        TableColumn<Task, String> dueTimeColumn = new TableColumn<>("Due");
        dueTimeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(formatDueTime(cell.getValue().getDueTime())));
        dueTimeColumn.setMinWidth(130);

        TableColumn<Task, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().isCompleted() ? "Done" : "Open"));
        statusColumn.setMinWidth(90);

        taskTable.getColumns().add(titleColumn);
        taskTable.getColumns().add(descriptionColumn);
        taskTable.getColumns().add(dueTimeColumn);
        taskTable.getColumns().add(statusColumn);
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

        hourSpinner = new Spinner<>(0, 23, LocalTime.now().getHour());
        hourSpinner.setEditable(true);
        hourSpinner.setPrefWidth(86);

        minuteSpinner = new Spinner<>(0, 59, 0);
        minuteSpinner.setEditable(true);
        minuteSpinner.setPrefWidth(86);

        GridPane timeGrid = new GridPane();
        timeGrid.setHgap(10);
        timeGrid.setVgap(8);
        timeGrid.add(new Label("Date"), 0, 0);
        timeGrid.add(dueDatePicker, 1, 0, 2, 1);
        timeGrid.add(new Label("Time"), 0, 1);
        timeGrid.add(hourSpinner, 1, 1);
        timeGrid.add(minuteSpinner, 2, 1);

        Button addButton = new Button("Add");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setDefaultButton(true);
        addButton.setOnAction(event -> addTask());

        VBox form = new VBox(12, formTitle, titleField, descriptionField, timeGrid, addButton);
        form.setPadding(new Insets(16));
        form.setPrefWidth(300);
        form.setMaxWidth(320);
        form.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d8dee9; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        return form;
    }

    private HBox createActions() {
        openOnlyCheckBox = new CheckBox("Open tasks only");
        openOnlyCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> refreshTasks());

        Button toggleCompletedButton = new Button("Toggle Status");
        toggleCompletedButton.setOnAction(event -> toggleSelectedTaskCompleted());

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(event -> deleteSelectedTask());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10, openOnlyCheckBox, spacer, toggleCompletedButton, deleteButton);
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

        LocalDateTime dueTime = LocalDateTime.of(
                dueDate,
                LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue()));

        Task task = new Task(title, descriptionField.getText().trim(), dueTime);
        taskService.addTask(task);
        clearForm();
        refreshTasks();
        taskTable.getSelectionModel().select(task);
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
        hourSpinner.getValueFactory().setValue(LocalTime.now().getHour());
        minuteSpinner.getValueFactory().setValue(0);
    }

    private String formatDueTime(LocalDateTime dueTime) {
        return dueTime == null ? "" : dueTime.format(DUE_TIME_FORMATTER);
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
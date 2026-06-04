package ch.jonas.timepilot.ui;

import java.time.Duration;
import java.time.LocalDate;

import ch.jonas.timepilot.model.Task;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class TaskView {
    private final PlanningController controller;
    private final TextField titleField = new TextField();
    private final TextArea descriptionArea = new TextArea();
    private final DatePicker dueDatePicker = new DatePicker(LocalDate.now());
    private final Spinner<Integer> estimatedMinutesSpinner = new Spinner<>();
    private final CheckBox completedCheckBox = new CheckBox("Completed");
    private final Label messageLabel = new Label();
    private final TableView<Task> table = new TableView<>();
    private Task selectedTask;

    public TaskView(PlanningController controller) {
        this.controller = controller;
    }

    public Node create() {
        configureTable();
        configureFormFields();

        VBox form = createForm();
        VBox tablePanel = createTablePanel();
        HBox layout = new HBox(18, form, tablePanel);
        HBox.setHgrow(tablePanel, Priority.ALWAYS);
        return layout;
    }

    private VBox createForm() {
        Button saveButton = new Button("Save task");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setStyle(UiStyles.primaryButton());
        saveButton.setOnAction(event -> saveTask());

        Button newButton = new Button("New");
        newButton.setMaxWidth(Double.MAX_VALUE);
        newButton.setStyle(UiStyles.secondaryButton());
        newButton.setOnAction(event -> clearForm());

        Button deleteButton = new Button("Delete");
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setStyle(UiStyles.dangerButton());
        deleteButton.setOnAction(event -> deleteSelectedTask());

        Button toggleCompletedButton = new Button("Toggle completed");
        toggleCompletedButton.setMaxWidth(Double.MAX_VALUE);
        toggleCompletedButton.setStyle(UiStyles.secondaryButton());
        toggleCompletedButton.setOnAction(event -> toggleSelectedTaskCompleted());

        HBox actions = new HBox(8, saveButton, newButton);
        HBox secondaryActions = new HBox(8, toggleCompletedButton, deleteButton);

        VBox form = new VBox(12,
                createHeading("Task details"),
                createFields(),
                completedCheckBox,
                actions,
                secondaryActions,
                messageLabel);
        form.setPrefWidth(330);
        form.setPadding(new Insets(18));
        form.setStyle(UiStyles.panel());
        messageLabel.setWrapText(true);
        return form;
    }

    private GridPane createFields() {
        GridPane fields = new GridPane();
        fields.setHgap(10);
        fields.setVgap(10);
        addField(fields, "Title", titleField, 0);
        addField(fields, "Description", descriptionArea, 1);
        addField(fields, "Due date", dueDatePicker, 2);
        addField(fields, "Estimated minutes", estimatedMinutesSpinner, 3);
        return fields;
    }

    private VBox createTablePanel() {
        VBox panel = new VBox(10, createHeading("Tasks"), table);
        panel.setPadding(new Insets(18));
        panel.setStyle(UiStyles.panel());
        VBox.setVgrow(table, Priority.ALWAYS);
        return panel;
    }

    private void configureFormFields() {
        descriptionArea.setPrefRowCount(4);
        estimatedMinutesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100_000, 60, 15));
    }

    private void configureTable() {
        table.setItems(controller.getTasks());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Task, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTitle()));

        TableColumn<Task, String> dueDateColumn = new TableColumn<>("Due date");
        dueDateColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDueDate().toString()));

        TableColumn<Task, String> durationColumn = new TableColumn<>("Minutes");
        durationColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                String.valueOf(data.getValue().getEstimatedWorkDuration().toMinutes())));

        TableColumn<Task, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().isCompleted() ? "Completed" : "Open"));

        table.getColumns().setAll(titleColumn, dueDateColumn, durationColumn, statusColumn);
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldTask, newTask) -> showTask(newTask));
    }

    private void saveTask() {
        try {
            if (selectedTask == null) {
                controller.addTask(
                        titleField.getText(),
                        descriptionArea.getText(),
                        requireDate(dueDatePicker),
                        estimatedMinutesSpinner.getValue());
            } else {
                controller.updateTask(
                        selectedTask,
                        titleField.getText(),
                        descriptionArea.getText(),
                        requireDate(dueDatePicker),
                        estimatedMinutesSpinner.getValue(),
                        completedCheckBox.isSelected());
            }
            showMessage("Saved.", false);
            clearForm();
        } catch (RuntimeException exception) {
            showMessage(exception.getMessage(), true);
        }
    }

    private void deleteSelectedTask() {
        if (selectedTask == null) {
            showMessage("Select a task first.", true);
            return;
        }
        controller.removeTask(selectedTask);
        clearForm();
        showMessage("Deleted.", false);
    }

    private void toggleSelectedTaskCompleted() {
        if (selectedTask == null) {
            showMessage("Select a task first.", true);
            return;
        }
        controller.setTaskCompleted(selectedTask, !selectedTask.isCompleted());
        clearForm();
        showMessage("Updated.", false);
    }

    private void showTask(Task task) {
        selectedTask = task;
        if (task == null) {
            return;
        }
        titleField.setText(task.getTitle());
        descriptionArea.setText(task.getDescription());
        dueDatePicker.setValue(task.getDueDate());
        estimatedMinutesSpinner.getValueFactory().setValue((int) task.getEstimatedWorkDuration().toMinutes());
        completedCheckBox.setSelected(task.isCompleted());
        showMessage("", false);
    }

    private void clearForm() {
        selectedTask = null;
        table.getSelectionModel().clearSelection();
        titleField.clear();
        descriptionArea.clear();
        dueDatePicker.setValue(LocalDate.now());
        estimatedMinutesSpinner.getValueFactory().setValue((int) Duration.ofHours(1).toMinutes());
        completedCheckBox.setSelected(false);
    }

    private void addField(GridPane fields, String label, Node field, int row) {
        fields.add(UiStyles.formLabel(label), 0, row);
        fields.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private Label createHeading(String text) {
        Label heading = new Label(text);
        heading.setStyle("-fx-text-fill: " + UiStyles.TEXT_PRIMARY + "; -fx-font-size: 18px; -fx-font-weight: 700;");
        return heading;
    }

    private LocalDate requireDate(DatePicker datePicker) {
        if (datePicker.getValue() == null) {
            throw new IllegalArgumentException("Date is required.");
        }
        return datePicker.getValue();
    }

    private void showMessage(String message, boolean error) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: " + (error ? "#c72f2f" : UiStyles.TEXT_MUTED) + ";");
    }
}

package ch.jonas.timepilot.ui;

import java.time.LocalDate;

import ch.jonas.timepilot.model.Deadline;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
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

public class DeadlineView {
    private final PlanningController controller;
    private final TextField titleField = new TextField();
    private final TextField subjectField = new TextField();
    private final TextArea descriptionArea = new TextArea();
    private final DatePicker dueDatePicker = new DatePicker(LocalDate.now());
    private final Spinner<Integer> estimatedMinutesSpinner = new Spinner<>();
    private final Label messageLabel = new Label();
    private final TableView<Deadline> table = new TableView<>();
    private Deadline selectedDeadline;

    public DeadlineView(PlanningController controller) {
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
        Button saveButton = new Button("Save deadline");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setStyle(UiStyles.primaryButton());
        saveButton.setOnAction(event -> saveDeadline());

        Button newButton = new Button("New");
        newButton.setMaxWidth(Double.MAX_VALUE);
        newButton.setStyle(UiStyles.secondaryButton());
        newButton.setOnAction(event -> clearForm());

        Button deleteButton = new Button("Delete");
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setStyle(UiStyles.dangerButton());
        deleteButton.setOnAction(event -> deleteSelectedDeadline());

        VBox form = new VBox(12,
                createHeading("Deadline details"),
                createFields(),
                new HBox(8, saveButton, newButton),
                deleteButton,
                messageLabel);
        form.setPrefWidth(340);
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
        addField(fields, "Subject", subjectField, 1);
        addField(fields, "Description", descriptionArea, 2);
        addField(fields, "Due date", dueDatePicker, 3);
        addField(fields, "Work minutes", estimatedMinutesSpinner, 4);
        return fields;
    }

    private VBox createTablePanel() {
        VBox panel = new VBox(10, createHeading("Deadlines"), table);
        panel.setPadding(new Insets(18));
        panel.setStyle(UiStyles.panel());
        VBox.setVgrow(table, Priority.ALWAYS);
        return panel;
    }

    private void configureFormFields() {
        descriptionArea.setPrefRowCount(4);
        estimatedMinutesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100_000, 120, 15));
    }

    private void configureTable() {
        table.setItems(controller.getDeadlines());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Deadline, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTitle()));

        TableColumn<Deadline, String> subjectColumn = new TableColumn<>("Subject");
        subjectColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getSubjectOrModule()));

        TableColumn<Deadline, String> dueDateColumn = new TableColumn<>("Due date");
        dueDateColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDueDate().toString()));

        TableColumn<Deadline, String> minutesColumn = new TableColumn<>("Minutes");
        minutesColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                String.valueOf(data.getValue().getEstimatedWorkDuration().toMinutes())));

        table.getColumns().setAll(titleColumn, subjectColumn, dueDateColumn, minutesColumn);
        table.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldDeadline, newDeadline) -> showDeadline(newDeadline));
    }

    private void saveDeadline() {
        try {
            if (selectedDeadline == null) {
                controller.addDeadline(
                        titleField.getText(),
                        subjectField.getText(),
                        descriptionArea.getText(),
                        requireDate(dueDatePicker),
                        estimatedMinutesSpinner.getValue());
            } else {
                controller.updateDeadline(
                        selectedDeadline,
                        titleField.getText(),
                        subjectField.getText(),
                        descriptionArea.getText(),
                        requireDate(dueDatePicker),
                        estimatedMinutesSpinner.getValue());
            }
            clearForm();
            showMessage("Saved.", false);
        } catch (RuntimeException exception) {
            showMessage(exception.getMessage(), true);
        }
    }

    private void deleteSelectedDeadline() {
        if (selectedDeadline == null) {
            showMessage("Select a deadline first.", true);
            return;
        }
        controller.removeDeadline(selectedDeadline);
        clearForm();
        showMessage("Deleted.", false);
    }

    private void showDeadline(Deadline deadline) {
        selectedDeadline = deadline;
        if (deadline == null) {
            return;
        }
        titleField.setText(deadline.getTitle());
        subjectField.setText(deadline.getSubjectOrModule());
        descriptionArea.setText(deadline.getDescription());
        dueDatePicker.setValue(deadline.getDueDate());
        estimatedMinutesSpinner.getValueFactory().setValue((int) deadline.getEstimatedWorkDuration().toMinutes());
        showMessage("", false);
    }

    private void clearForm() {
        selectedDeadline = null;
        table.getSelectionModel().clearSelection();
        titleField.clear();
        subjectField.clear();
        descriptionArea.clear();
        dueDatePicker.setValue(LocalDate.now());
        estimatedMinutesSpinner.getValueFactory().setValue(120);
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

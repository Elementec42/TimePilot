package ch.jonas.timepilot.ui;

import java.time.LocalDate;

import ch.jonas.timepilot.model.Exam;
import ch.jonas.timepilot.model.ExamPriority;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ExamView {
    private final PlanningController controller;
    private final TextField titleField = new TextField();
    private final TextField subjectField = new TextField();
    private final DatePicker examDatePicker = new DatePicker(LocalDate.now());
    private final Spinner<Integer> estimatedMinutesSpinner = new Spinner<>();
    private final ComboBox<ExamPriority> priorityComboBox = new ComboBox<>();
    private final Label messageLabel = new Label();
    private final TableView<Exam> table = new TableView<>();
    private Exam selectedExam;

    public ExamView(PlanningController controller) {
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
        Button saveButton = new Button("Save exam");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setStyle(UiStyles.primaryButton());
        saveButton.setOnAction(event -> saveExam());

        Button newButton = new Button("New");
        newButton.setMaxWidth(Double.MAX_VALUE);
        newButton.setStyle(UiStyles.secondaryButton());
        newButton.setOnAction(event -> clearForm());

        Button deleteButton = new Button("Delete");
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setStyle(UiStyles.dangerButton());
        deleteButton.setOnAction(event -> deleteSelectedExam());

        VBox form = new VBox(12,
                createHeading("Exam details"),
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
        addField(fields, "Exam date", examDatePicker, 2);
        addField(fields, "Study minutes", estimatedMinutesSpinner, 3);
        addField(fields, "Priority", priorityComboBox, 4);
        return fields;
    }

    private VBox createTablePanel() {
        VBox panel = new VBox(10, createHeading("Exams"), table);
        panel.setPadding(new Insets(18));
        panel.setStyle(UiStyles.panel());
        VBox.setVgrow(table, Priority.ALWAYS);
        return panel;
    }

    private void configureFormFields() {
        estimatedMinutesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100_000, 180, 15));
        priorityComboBox.getItems().setAll(ExamPriority.values());
        priorityComboBox.setValue(ExamPriority.MEDIUM);
        priorityComboBox.setMaxWidth(Double.MAX_VALUE);
    }

    private void configureTable() {
        table.setItems(controller.getExams());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Exam, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTitle()));

        TableColumn<Exam, String> subjectColumn = new TableColumn<>("Subject");
        subjectColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getSubjectOrModule()));

        TableColumn<Exam, String> dateColumn = new TableColumn<>("Exam date");
        dateColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getExamDate().toString()));

        TableColumn<Exam, String> minutesColumn = new TableColumn<>("Minutes");
        minutesColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                String.valueOf(data.getValue().getEstimatedStudyDuration().toMinutes())));

        TableColumn<Exam, String> priorityColumn = new TableColumn<>("Priority");
        priorityColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getPriority().name()));

        table.getColumns().setAll(titleColumn, subjectColumn, dateColumn, minutesColumn, priorityColumn);
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldExam, newExam) -> showExam(newExam));
    }

    private void saveExam() {
        try {
            if (selectedExam == null) {
                controller.addExam(
                        titleField.getText(),
                        subjectField.getText(),
                        requireDate(examDatePicker),
                        estimatedMinutesSpinner.getValue(),
                        requirePriority());
            } else {
                controller.updateExam(
                        selectedExam,
                        titleField.getText(),
                        subjectField.getText(),
                        requireDate(examDatePicker),
                        estimatedMinutesSpinner.getValue(),
                        requirePriority());
            }
            clearForm();
            showMessage("Saved.", false);
        } catch (RuntimeException exception) {
            showMessage(exception.getMessage(), true);
        }
    }

    private void deleteSelectedExam() {
        if (selectedExam == null) {
            showMessage("Select an exam first.", true);
            return;
        }
        controller.removeExam(selectedExam);
        clearForm();
        showMessage("Deleted.", false);
    }

    private void showExam(Exam exam) {
        selectedExam = exam;
        if (exam == null) {
            return;
        }
        titleField.setText(exam.getTitle());
        subjectField.setText(exam.getSubjectOrModule());
        examDatePicker.setValue(exam.getExamDate());
        estimatedMinutesSpinner.getValueFactory().setValue((int) exam.getEstimatedStudyDuration().toMinutes());
        priorityComboBox.setValue(exam.getPriority());
        showMessage("", false);
    }

    private void clearForm() {
        selectedExam = null;
        table.getSelectionModel().clearSelection();
        titleField.clear();
        subjectField.clear();
        examDatePicker.setValue(LocalDate.now());
        estimatedMinutesSpinner.getValueFactory().setValue(180);
        priorityComboBox.setValue(ExamPriority.MEDIUM);
    }

    private void addField(GridPane fields, String label, Node field, int row) {
        fields.add(new Label(label), 0, row);
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

    private ExamPriority requirePriority() {
        if (priorityComboBox.getValue() == null) {
            throw new IllegalArgumentException("Priority is required.");
        }
        return priorityComboBox.getValue();
    }

    private void showMessage(String message, boolean error) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: " + (error ? "#c72f2f" : UiStyles.TEXT_MUTED) + ";");
    }
}

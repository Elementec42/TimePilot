package ch.jonas.timepilot.ui;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import ch.jonas.timepilot.model.StudySession;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

public class CalendarView {
    private static final int START_HOUR = 7;
    private static final int END_HOUR = 21;
    private static final double HOUR_HEIGHT = 64;
    private static final double MINUTE_HEIGHT = HOUR_HEIGHT / 60.0;
    private static final double DAY_WIDTH = 132;
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("EEE dd.MM.");

    private final PlanningController controller;
    private final GridPane calendarGrid = new GridPane();
    private final Label weekLabel = new Label();
    private final TextField titleField = new TextField();
    private final TextField subjectField = new TextField();
    private final DatePicker datePicker = new DatePicker(LocalDate.now());
    private final Spinner<Integer> startHourSpinner = new Spinner<>();
    private final Spinner<Integer> startMinuteSpinner = new Spinner<>();
    private final Spinner<Integer> endHourSpinner = new Spinner<>();
    private final Spinner<Integer> endMinuteSpinner = new Spinner<>();
    private final TextArea notesArea = new TextArea();
    private final Label messageLabel = new Label();
    private final List<Pane> dayPanes = new ArrayList<>();

    private LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    private StudySession selectedStudySession;

    public CalendarView(PlanningController controller) {
        this.controller = controller;
    }

    public Node create() {
        configureFormFields();
        controller.getStudySessions().addListener((ListChangeListener<StudySession>) change -> renderCalendar());

        BorderPane layout = new BorderPane();
        layout.setLeft(createForm());
        layout.setCenter(createCalendarPanel());
        renderCalendar();
        return layout;
    }

    private Node createForm() {
        Button saveButton = new Button("Save session");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setStyle(UiStyles.primaryButton());
        saveButton.setOnAction(event -> saveStudySession());

        Button newButton = new Button("New");
        newButton.setMaxWidth(Double.MAX_VALUE);
        newButton.setStyle(UiStyles.secondaryButton());
        newButton.setOnAction(event -> clearForm());

        Button deleteButton = new Button("Delete");
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setStyle(UiStyles.dangerButton());
        deleteButton.setOnAction(event -> deleteSelectedStudySession());

        HBox startTime = new HBox(6, startHourSpinner, new Label(":"), startMinuteSpinner);
        startTime.setAlignment(Pos.CENTER_LEFT);
        HBox endTime = new HBox(6, endHourSpinner, new Label(":"), endMinuteSpinner);
        endTime.setAlignment(Pos.CENTER_LEFT);

        GridPane fields = new GridPane();
        fields.setHgap(10);
        fields.setVgap(10);
        addField(fields, "Title", titleField, 0);
        addField(fields, "Subject", subjectField, 1);
        addField(fields, "Date", datePicker, 2);
        addField(fields, "Start", startTime, 3);
        addField(fields, "End", endTime, 4);
        addField(fields, "Notes", notesArea, 5);

        VBox form = new VBox(12,
                createHeading("Study session"),
                fields,
                new HBox(8, saveButton, newButton),
                deleteButton,
                messageLabel);
        form.setPrefWidth(330);
        form.setPadding(new Insets(18));
        form.setStyle(UiStyles.panel());
        messageLabel.setWrapText(true);
        return form;
    }

    private Node createCalendarPanel() {
        Button previousWeekButton = new Button("Previous");
        previousWeekButton.setStyle(UiStyles.secondaryButton());
        previousWeekButton.setOnAction(event -> changeWeek(-1));

        Button todayButton = new Button("Today");
        todayButton.setStyle(UiStyles.secondaryButton());
        todayButton.setOnAction(event -> {
            weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            renderCalendar();
        });

        Button nextWeekButton = new Button("Next");
        nextWeekButton.setStyle(UiStyles.secondaryButton());
        nextWeekButton.setOnAction(event -> changeWeek(1));

        weekLabel.setStyle("-fx-text-fill: " + UiStyles.TEXT_PRIMARY + "; -fx-font-size: 18px; -fx-font-weight: 700;");
        HBox header = new HBox(10, previousWeekButton, todayButton, nextWeekButton, weekLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scrollPane = new ScrollPane(calendarGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox panel = new VBox(12, header, scrollPane);
        panel.setPadding(new Insets(0, 0, 0, 18));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return panel;
    }

    private void renderCalendar() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();
        dayPanes.clear();

        weekLabel.setText(weekStart + " - " + weekStart.plusDays(6));

        ColumnConstraints timeColumn = new ColumnConstraints(64);
        calendarGrid.getColumnConstraints().add(timeColumn);
        for (int day = 0; day < 7; day++) {
            ColumnConstraints dayColumn = new ColumnConstraints(DAY_WIDTH);
            dayColumn.setHgrow(Priority.ALWAYS);
            calendarGrid.getColumnConstraints().add(dayColumn);
        }

        RowConstraints headerRow = new RowConstraints(34);
        calendarGrid.getRowConstraints().add(headerRow);
        for (int hour = START_HOUR; hour < END_HOUR; hour++) {
            calendarGrid.getRowConstraints().add(new RowConstraints(HOUR_HEIGHT));
        }

        calendarGrid.add(createGridHeader(""), 0, 0);
        for (int day = 0; day < 7; day++) {
            LocalDate date = weekStart.plusDays(day);
            calendarGrid.add(createGridHeader(date.format(DAY_FORMATTER)), day + 1, 0);
        }

        for (int hour = START_HOUR; hour < END_HOUR; hour++) {
            calendarGrid.add(createTimeLabel(hour), 0, hour - START_HOUR + 1);
        }

        for (int day = 0; day < 7; day++) {
            LocalDate date = weekStart.plusDays(day);
            Pane dayPane = createDayPane(date);
            dayPanes.add(dayPane);
            calendarGrid.add(dayPane, day + 1, 1, 1, END_HOUR - START_HOUR);
        }

        controller.getStudySessions().stream()
                .filter(this::isVisibleInCurrentWeek)
                .forEach(this::renderStudySessionBlock);
    }

    private Pane createDayPane(LocalDate date) {
        Pane pane = new Pane();
        pane.setMinHeight((END_HOUR - START_HOUR) * HOUR_HEIGHT);
        pane.setPrefHeight((END_HOUR - START_HOUR) * HOUR_HEIGHT);
        pane.setPrefWidth(DAY_WIDTH);
        pane.setStyle("-fx-background-color: white; -fx-border-color: " + UiStyles.BORDER + ";");
        pane.setOnMouseClicked(event -> prefillFromClick(date, event));

        for (int hour = START_HOUR + 1; hour < END_HOUR; hour++) {
            Pane line = new Pane();
            line.setLayoutY((hour - START_HOUR) * HOUR_HEIGHT);
            line.setPrefHeight(1);
            line.setPrefWidth(DAY_WIDTH);
            line.setStyle("-fx-background-color: #edf0f5;");
            pane.getChildren().add(line);
        }
        return pane;
    }

    private void renderStudySessionBlock(StudySession studySession) {
        int dayIndex = (int) Duration.between(
                weekStart.atStartOfDay(),
                studySession.getStartDateTime().toLocalDate().atStartOfDay()).toDays();
        if (dayIndex < 0 || dayIndex >= dayPanes.size()) {
            return;
        }

        LocalDateTime visibleStart = studySession.getStartDateTime();
        LocalDateTime visibleEnd = studySession.getEndDateTime();
        LocalDate date = visibleStart.toLocalDate();
        LocalDateTime dayStart = date.atTime(START_HOUR, 0);
        LocalDateTime dayEnd = date.atTime(END_HOUR, 0);
        if (!visibleEnd.isAfter(dayStart) || !visibleStart.isBefore(dayEnd)) {
            return;
        }

        double top = Math.max(0, Duration.between(dayStart, visibleStart).toMinutes() * MINUTE_HEIGHT);
        double height = Math.max(28, Duration.between(visibleStart, visibleEnd).toMinutes() * MINUTE_HEIGHT);
        double visibleHeight = Math.max(24, Math.min(height - 4, dayPanes.get(dayIndex).getPrefHeight() - top - 4));

        Label block = new Label(studySession.getTitle() + "\n" + studySession.getSubjectOrModule());
        block.setWrapText(true);
        block.setPadding(new Insets(6));
        block.setLayoutX(6);
        block.setLayoutY(top + 2);
        block.setPrefWidth(DAY_WIDTH - 12);
        block.setPrefHeight(visibleHeight);
        block.setStyle("-fx-background-color: #2f6fd6; -fx-background-radius: 6;"
                + " -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: 700;");
        block.setOnMouseClicked(event -> {
            event.consume();
            showStudySession(studySession);
        });
        dayPanes.get(dayIndex).getChildren().add(block);
    }

    private void saveStudySession() {
        try {
            LocalDate date = requireDate();
            if (selectedStudySession == null) {
                controller.addStudySession(
                        titleField.getText(),
                        subjectField.getText(),
                        date,
                        startHourSpinner.getValue(),
                        startMinuteSpinner.getValue(),
                        endHourSpinner.getValue(),
                        endMinuteSpinner.getValue(),
                        notesArea.getText());
            } else {
                controller.updateStudySession(
                        selectedStudySession,
                        titleField.getText(),
                        subjectField.getText(),
                        date,
                        startHourSpinner.getValue(),
                        startMinuteSpinner.getValue(),
                        endHourSpinner.getValue(),
                        endMinuteSpinner.getValue(),
                        notesArea.getText());
            }
            clearForm();
            showMessage("Saved.", false);
        } catch (RuntimeException exception) {
            showMessage(exception.getMessage(), true);
        }
    }

    private void deleteSelectedStudySession() {
        if (selectedStudySession == null) {
            showMessage("Select a session first.", true);
            return;
        }
        controller.removeStudySession(selectedStudySession);
        clearForm();
        showMessage("Deleted.", false);
    }

    private void prefillFromClick(LocalDate date, MouseEvent event) {
        LocalTime startTime = timeFromY(event.getY());
        LocalTime endTime = startTime.plusHours(1);
        if (endTime.isAfter(LocalTime.of(END_HOUR, 0))) {
            endTime = LocalTime.of(END_HOUR, 0);
        }

        selectedStudySession = null;
        datePicker.setValue(date);
        startHourSpinner.getValueFactory().setValue(startTime.getHour());
        startMinuteSpinner.getValueFactory().setValue(startTime.getMinute());
        endHourSpinner.getValueFactory().setValue(endTime.getHour());
        endMinuteSpinner.getValueFactory().setValue(endTime.getMinute());
        showMessage("", false);
    }

    private LocalTime timeFromY(double y) {
        int minutesFromStart = Math.max(0, (int) Math.round(y / MINUTE_HEIGHT / 15.0) * 15);
        int maxMinutes = (END_HOUR - START_HOUR) * 60 - 15;
        minutesFromStart = Math.min(minutesFromStart, maxMinutes);
        return LocalTime.of(START_HOUR, 0).plusMinutes(minutesFromStart);
    }

    private void showStudySession(StudySession studySession) {
        selectedStudySession = studySession;
        titleField.setText(studySession.getTitle());
        subjectField.setText(studySession.getSubjectOrModule());
        datePicker.setValue(studySession.getStartDateTime().toLocalDate());
        startHourSpinner.getValueFactory().setValue(studySession.getStartDateTime().getHour());
        startMinuteSpinner.getValueFactory().setValue(studySession.getStartDateTime().getMinute());
        endHourSpinner.getValueFactory().setValue(studySession.getEndDateTime().getHour());
        endMinuteSpinner.getValueFactory().setValue(studySession.getEndDateTime().getMinute());
        notesArea.setText(studySession.getNotes());
        showMessage("", false);
    }

    private void clearForm() {
        selectedStudySession = null;
        titleField.clear();
        subjectField.clear();
        datePicker.setValue(LocalDate.now());
        startHourSpinner.getValueFactory().setValue(9);
        startMinuteSpinner.getValueFactory().setValue(0);
        endHourSpinner.getValueFactory().setValue(10);
        endMinuteSpinner.getValueFactory().setValue(0);
        notesArea.clear();
    }

    private void configureFormFields() {
        startHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        startMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 45, 0, 15));
        endHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 10));
        endMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 45, 0, 15));
        notesArea.setPrefRowCount(4);
    }

    private boolean isVisibleInCurrentWeek(StudySession studySession) {
        LocalDate sessionDate = studySession.getStartDateTime().toLocalDate();
        return !sessionDate.isBefore(weekStart) && !sessionDate.isAfter(weekStart.plusDays(6));
    }

    private void changeWeek(int weeks) {
        weekStart = weekStart.plusWeeks(weeks);
        renderCalendar();
    }

    private Label createGridHeader(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-background-color: #eef2f7; -fx-border-color: " + UiStyles.BORDER + ";"
                + " -fx-text-fill: " + UiStyles.TEXT_PRIMARY + "; -fx-font-weight: 700;");
        return label;
    }

    private Label createTimeLabel(int hour) {
        Label label = new Label(String.format("%02d:00", hour));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.setAlignment(Pos.TOP_RIGHT);
        label.setPadding(new Insets(4, 8, 0, 0));
        label.setStyle("-fx-background-color: #f8fafc; -fx-border-color: " + UiStyles.BORDER + ";"
                + " -fx-text-fill: " + UiStyles.TEXT_MUTED + ";");
        return label;
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

    private LocalDate requireDate() {
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

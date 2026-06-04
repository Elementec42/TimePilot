package ch.jonas.timepilot.ui;

import java.time.format.DateTimeFormatter;

import ch.jonas.timepilot.model.StudySession;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class CalendarOverviewView {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PlanningController controller;

    public CalendarOverviewView(PlanningController controller) {
        this.controller = controller;
    }

    public Node create() {
        TableView<StudySession> table = new TableView<>(controller.getStudySessions());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<StudySession, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTitle()));

        TableColumn<StudySession, String> subjectColumn = new TableColumn<>("Subject");
        subjectColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getSubjectOrModule()));

        TableColumn<StudySession, String> startColumn = new TableColumn<>("Start");
        startColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getStartDateTime().format(DATE_TIME_FORMATTER)));

        TableColumn<StudySession, String> endColumn = new TableColumn<>("End");
        endColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getEndDateTime().format(DATE_TIME_FORMATTER)));

        table.getColumns().setAll(titleColumn, subjectColumn, startColumn, endColumn);

        Label heading = new Label("Calendar");
        heading.setStyle("-fx-text-fill: " + UiStyles.TEXT_PRIMARY + "; -fx-font-size: 18px; -fx-font-weight: 700;");

        VBox panel = new VBox(10, heading, table);
        panel.setPadding(new Insets(18));
        panel.setStyle(UiStyles.panel());
        VBox.setVgrow(table, Priority.ALWAYS);
        return panel;
    }
}

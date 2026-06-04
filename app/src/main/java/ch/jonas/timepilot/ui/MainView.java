package ch.jonas.timepilot.ui;

import java.util.List;
import java.util.Objects;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainView {
    private static final String BACKGROUND = "#f6f7f9";
    private static final String BORDER = "#d8dde6";
    private static final String SIDEBAR = "#202733";
    private static final String TEXT_MUTED = "#657083";
    private static final String TEXT_PRIMARY = "#1e2430";

    private final BorderPane root = new BorderPane();
    private final StackPane contentPane = new StackPane();
    private final Label titleLabel = new Label();
    private final Label subtitleLabel = new Label();
    private final PlanningController planningController;

    public MainView(PlanningController planningController) {
        this.planningController = Objects.requireNonNull(planningController, "planningController must not be null");
    }

    public BorderPane create() {
        root.setStyle("-fx-background-color: " + BACKGROUND + ";");
        root.setLeft(createSidebar());
        root.setCenter(createMainArea());
        showSection(Section.TASKS);
        return root;
    }

    private Node createSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(22, 16, 16, 16));
        sidebar.setStyle("-fx-background-color: " + SIDEBAR + ";");

        Label appName = new Label("TimePilot");
        appName.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: 700;");

        Label appSubtitle = new Label("Student planning");
        appSubtitle.setStyle("-fx-text-fill: #b8c2d2; -fx-font-size: 12px;");

        ToggleGroup navigationGroup = new ToggleGroup();
        List<ToggleButton> buttons = List.of(
                createNavigationButton("Tasks", Section.TASKS, navigationGroup),
                createNavigationButton("Deadlines", Section.DEADLINES, navigationGroup),
                createNavigationButton("Exams", Section.EXAMS, navigationGroup),
                createNavigationButton("Calendar", Section.CALENDAR, navigationGroup));
        buttons.getFirst().setSelected(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label status = new Label("Manual planning mode");
        status.setWrapText(true);
        status.setStyle("-fx-text-fill: #b8c2d2; -fx-font-size: 12px;");

        sidebar.getChildren().add(appName);
        sidebar.getChildren().add(appSubtitle);
        sidebar.getChildren().add(new Separator());
        sidebar.getChildren().addAll(buttons);
        sidebar.getChildren().add(spacer);
        sidebar.getChildren().add(status);
        return sidebar;
    }

    private ToggleButton createNavigationButton(String text, Section section, ToggleGroup navigationGroup) {
        ToggleButton button = new ToggleButton(text);
        button.setToggleGroup(navigationGroup);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPadding(new Insets(10, 12, 10, 12));
        button.setStyle("-fx-background-radius: 6; -fx-background-color: transparent; -fx-text-fill: white;"
                + " -fx-font-size: 14px;");
        button.setOnAction(event -> showSection(section));
        button.selectedProperty().addListener((observable, wasSelected, isSelected) -> {
            if (isSelected) {
                button.setStyle("-fx-background-radius: 6; -fx-background-color: #3d82f6; -fx-text-fill: white;"
                        + " -fx-font-size: 14px; -fx-font-weight: 700;");
            } else {
                button.setStyle("-fx-background-radius: 6; -fx-background-color: transparent; -fx-text-fill: white;"
                        + " -fx-font-size: 14px;");
            }
        });
        return button;
    }

    private Node createMainArea() {
        BorderPane mainArea = new BorderPane();
        mainArea.setPadding(new Insets(26, 30, 30, 30));

        VBox header = new VBox(4);
        titleLabel.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 28px; -fx-font-weight: 700;");
        subtitleLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 14px;");
        header.getChildren().addAll(titleLabel, subtitleLabel);

        contentPane.setPadding(new Insets(22, 0, 0, 0));
        mainArea.setTop(header);
        mainArea.setCenter(contentPane);
        return mainArea;
    }

    private void showSection(Section section) {
        titleLabel.setText(section.title);
        subtitleLabel.setText(section.subtitle);
        contentPane.getChildren().setAll(createSectionContent(section));
    }

    private Node createSectionContent(Section section) {
        return switch (section) {
            case TASKS -> new TaskView(planningController).create();
            case DEADLINES -> new DeadlineView(planningController).create();
            case EXAMS -> new ExamView(planningController).create();
            case CALENDAR -> new CalendarView(planningController).create();
        };
    }

    private Node createListPlaceholder(String heading, String description, List<String> columns) {
        VBox panel = createPanel();

        Label headingLabel = createPanelHeading(heading);
        Label descriptionLabel = createDescription(description);
        HBox columnHeader = new HBox(12);
        columnHeader.setPadding(new Insets(14, 0, 8, 0));

        for (String column : columns) {
            Label columnLabel = new Label(column);
            columnLabel.setMinWidth(160);
            columnLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px; -fx-font-weight: 700;");
            columnHeader.getChildren().add(columnLabel);
        }

        StackPane emptyState = new StackPane(new Label("No entries yet"));
        emptyState.setMinHeight(240);
        emptyState.setStyle("-fx-border-color: " + BORDER + "; -fx-border-radius: 6;"
                + " -fx-background-color: white; -fx-background-radius: 6;");

        panel.getChildren().addAll(headingLabel, descriptionLabel, columnHeader, emptyState);
        return panel;
    }

    private Node createCalendarPlaceholder() {
        VBox panel = createPanel();
        panel.getChildren().addAll(
                createPanelHeading("Weekly calendar"),
                createDescription("A manual weekly planning grid will appear here."));

        HBox days = new HBox(1);
        days.setPadding(new Insets(16, 0, 0, 0));
        List<String> dayNames = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
        for (String dayName : dayNames) {
            VBox dayColumn = new VBox(8);
            dayColumn.setMinHeight(360);
            dayColumn.setAlignment(Pos.TOP_CENTER);
            dayColumn.setPadding(new Insets(12));
            dayColumn.setStyle("-fx-background-color: white; -fx-border-color: " + BORDER + ";");

            Label dayLabel = new Label(dayName);
            dayLabel.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-weight: 700;");
            dayColumn.getChildren().add(dayLabel);
            HBox.setHgrow(dayColumn, Priority.ALWAYS);
            days.getChildren().add(dayColumn);
        }

        panel.getChildren().add(days);
        return panel;
    }

    private VBox createPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 8;"
                + " -fx-border-color: " + BORDER + "; -fx-border-radius: 8;");
        return panel;
    }

    private Label createPanelHeading(String text) {
        Label heading = new Label(text);
        heading.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 18px; -fx-font-weight: 700;");
        return heading;
    }

    private Label createDescription(String text) {
        Label description = new Label(text);
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px;");
        return description;
    }

    private enum Section {
        TASKS("Tasks", "Manage student work items and completion state."),
        DEADLINES("Deadlines", "Track assignments, due dates, and required work time."),
        EXAMS("Exams", "Plan exam dates, priorities, and estimated study time."),
        CALENDAR("Calendar", "Manually arrange study and work blocks.");

        private final String title;
        private final String subtitle;

        Section(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }
    }
}

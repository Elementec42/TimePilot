package ch.jonas.timepilot.ui;

import javafx.scene.control.Label;

final class UiStyles {
    static final String BORDER = "#d8dde6";
    static final String TEXT_MUTED = "#657083";
    static final String TEXT_PRIMARY = "#1e2430";

    private UiStyles() {
    }

    static String panel() {
        return "-fx-background-color: white; -fx-background-radius: 8;"
                + " -fx-border-color: " + BORDER + "; -fx-border-radius: 8;";
    }

    static String primaryButton() {
        return "-fx-background-color: #2f6fd6; -fx-text-fill: white; -fx-background-radius: 6;"
                + " -fx-font-weight: 700;";
    }

    static String secondaryButton() {
        return "-fx-background-color: #eef2f7; -fx-text-fill: " + TEXT_PRIMARY + "; -fx-background-radius: 6;";
    }

    static String dangerButton() {
        return "-fx-background-color: #d93f3f; -fx-text-fill: white; -fx-background-radius: 6;"
                + " -fx-font-weight: 700;";
    }

    static Label formLabel(String text) {
        Label label = new Label(text);
        label.setMinWidth(104);
        label.setPrefWidth(104);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: " + TEXT_PRIMARY + ";");
        return label;
    }
}

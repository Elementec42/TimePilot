package ch.jonas.timepilot;

import ch.jonas.timepilot.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    public String getApplicationName() {
        return "TimePilot";
    }

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new MainView().create(), 1100, 720);
        stage.setTitle(getApplicationName());
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

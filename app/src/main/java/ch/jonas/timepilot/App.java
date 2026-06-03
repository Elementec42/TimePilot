package ch.jonas.timepilot;

public class App {
    public String getApplicationName() {
        return "TimePilot";
    }

    public static void main(String[] args) {
        System.out.println(new App().getApplicationName() + " started.");
    }
}

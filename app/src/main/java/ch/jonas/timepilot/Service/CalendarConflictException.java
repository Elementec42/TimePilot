package ch.jonas.timepilot.service;

public class CalendarConflictException extends IllegalArgumentException {
    public CalendarConflictException(String message) {
        super(message);
    }
}

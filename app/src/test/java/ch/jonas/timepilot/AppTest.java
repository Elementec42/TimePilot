package ch.jonas.timepilot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AppTest {
    @Test
    void applicationHasName() {
        App app = new App();

        assertEquals("TimePilot", app.getApplicationName());
    }
}

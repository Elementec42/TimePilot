package ch.jonas.timepilot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import ch.jonas.timepilot.model.CalendarBlock;

class CalendarServiceTest {
    @Test
    void addBlockRejectsOverlaps() {
        CalendarService service = new CalendarService();
        service.addBlock("Math", dateTime(9, 0), dateTime(10, 0));

        CalendarBlock overlappingBlock = new CalendarBlock("Physics", dateTime(9, 30), dateTime(10, 30));

        assertThrows(CalendarConflictException.class, () -> service.addBlock(overlappingBlock));
        assertEquals(1, service.listBlocks().size());
    }

    @Test
    void adjacentBlocksDoNotOverlap() {
        CalendarService service = new CalendarService();
        CalendarBlock first = service.addBlock("Math", dateTime(9, 0), dateTime(10, 0));
        CalendarBlock second = new CalendarBlock("Physics", dateTime(10, 0), dateTime(11, 0));

        assertFalse(service.overlaps(first, second));
        service.addBlock(second);
        assertEquals(2, service.listBlocks().size());
    }

    @Test
    void changingBlockTimeRejectsOverlapAndRestoresOriginalTime() {
        CalendarService service = new CalendarService();
        CalendarBlock first = service.addBlock("Math", dateTime(9, 0), dateTime(10, 0));
        service.addBlock("Physics", dateTime(11, 0), dateTime(12, 0));

        assertThrows(CalendarConflictException.class, () ->
                service.changeBlockTime(first, dateTime(11, 30), dateTime(12, 30)));

        assertEquals(dateTime(9, 0), first.getStartDateTime());
        assertEquals(dateTime(10, 0), first.getEndDateTime());
    }

    @Test
    void hasOverlapDetectsCandidateConflicts() {
        CalendarService service = new CalendarService();
        service.addBlock("Math", dateTime(9, 0), dateTime(10, 0));

        CalendarBlock candidate = new CalendarBlock("Physics", dateTime(9, 15), dateTime(9, 45));

        assertTrue(service.hasOverlap(candidate));
        assertEquals(1, service.findOverlappingBlocks(candidate).size());
    }

    private LocalDateTime dateTime(int hour, int minute) {
        return LocalDateTime.of(2026, 6, 3, hour, minute);
    }
}

package ch.jonas.timepilot.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.jonas.timepilot.model.CalendarBlock;

public class CalendarService {
    private final List<CalendarBlock> blocks = new ArrayList<>();

    public CalendarBlock addBlock(String title, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        CalendarBlock block = new CalendarBlock(title, startDateTime, endDateTime);
        addBlock(block);
        return block;
    }

    public CalendarBlock addBlock(CalendarBlock block) {
        Objects.requireNonNull(block, "block must not be null");
        rejectOverlap(block, null);
        blocks.add(block);
        return block;
    }

    public boolean removeBlock(CalendarBlock block) {
        return blocks.remove(block);
    }

    public void changeBlockTime(
            CalendarBlock block,
            LocalDateTime newStartDateTime,
            LocalDateTime newEndDateTime) {
        requireManagedBlock(block);

        LocalDateTime oldStartDateTime = block.getStartDateTime();
        LocalDateTime oldEndDateTime = block.getEndDateTime();
        block.setTimeRange(newStartDateTime, newEndDateTime);

        try {
            rejectOverlap(block, block);
        } catch (RuntimeException exception) {
            block.setTimeRange(oldStartDateTime, oldEndDateTime);
            throw exception;
        }
    }

    public List<CalendarBlock> listBlocks() {
        return List.copyOf(blocks);
    }

    public boolean hasOverlap(CalendarBlock candidate) {
        return hasOverlap(candidate, null);
    }

    public List<CalendarBlock> findOverlappingBlocks(CalendarBlock candidate) {
        return findOverlappingBlocks(candidate, null);
    }

    public boolean overlaps(CalendarBlock first, CalendarBlock second) {
        Objects.requireNonNull(first, "first must not be null");
        Objects.requireNonNull(second, "second must not be null");

        return first.getStartDateTime().isBefore(second.getEndDateTime())
                && first.getEndDateTime().isAfter(second.getStartDateTime());
    }

    private void rejectOverlap(CalendarBlock candidate, CalendarBlock ignoredBlock) {
        List<CalendarBlock> overlappingBlocks = findOverlappingBlocks(candidate, ignoredBlock);
        if (!overlappingBlocks.isEmpty()) {
            throw new CalendarConflictException("calendar block overlaps with an existing block");
        }
    }

    private boolean hasOverlap(CalendarBlock candidate, CalendarBlock ignoredBlock) {
        return !findOverlappingBlocks(candidate, ignoredBlock).isEmpty();
    }

    private List<CalendarBlock> findOverlappingBlocks(CalendarBlock candidate, CalendarBlock ignoredBlock) {
        Objects.requireNonNull(candidate, "candidate must not be null");
        return blocks.stream()
                .filter(block -> block != ignoredBlock)
                .filter(block -> overlaps(block, candidate))
                .toList();
    }

    private void requireManagedBlock(CalendarBlock block) {
        Objects.requireNonNull(block, "block must not be null");
        if (!blocks.contains(block)) {
            throw new IllegalArgumentException("calendar block is not managed by this service");
        }
    }
}

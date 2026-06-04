package ch.jonas.timepilot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import ch.jonas.timepilot.model.Deadline;

public class DeadlineService {
    private final List<Deadline> deadlines = new ArrayList<>();

    public Deadline addDeadline(Deadline deadline) {
        deadlines.add(Objects.requireNonNull(deadline, "deadline must not be null"));
        return deadline;
    }

    public boolean removeDeadline(Deadline deadline) {
        return deadlines.remove(deadline);
    }

    public void editDeadline(Deadline deadline, Consumer<Deadline> changes) {
        requireManagedDeadline(deadline);
        Objects.requireNonNull(changes, "changes must not be null").accept(deadline);
    }

    public List<Deadline> listDeadlines() {
        return List.copyOf(deadlines);
    }

    private void requireManagedDeadline(Deadline deadline) {
        Objects.requireNonNull(deadline, "deadline must not be null");
        if (!deadlines.contains(deadline)) {
            throw new IllegalArgumentException("deadline is not managed by this service");
        }
    }
}

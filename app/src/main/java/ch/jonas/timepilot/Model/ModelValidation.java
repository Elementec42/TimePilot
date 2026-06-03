package ch.jonas.timepilot.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

final class ModelValidation {
    private ModelValidation() {
    }

    static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }
        return value.trim();
    }

    static String defaultText(String value) {
        return value == null ? "" : value.trim();
    }

    static LocalDate requireDate(LocalDate value, String fieldName) {
        return Objects.requireNonNull(value, fieldName + " must not be null");
    }

    static Duration requireNonNegativeDuration(Duration value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.isNegative()) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return value;
    }

    static void requireValidTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Objects.requireNonNull(startDateTime, "startDateTime must not be null");
        Objects.requireNonNull(endDateTime, "endDateTime must not be null");
        if (!endDateTime.isAfter(startDateTime)) {
            throw new IllegalArgumentException("endDateTime must be after startDateTime");
        }
    }

    static <T> T requireObject(T value, String fieldName) {
        return Objects.requireNonNull(value, fieldName + " must not be null");
    }
}

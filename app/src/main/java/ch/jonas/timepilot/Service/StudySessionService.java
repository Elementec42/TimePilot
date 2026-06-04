package ch.jonas.timepilot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import ch.jonas.timepilot.model.StudySession;

public class StudySessionService {
    private final List<StudySession> studySessions = new ArrayList<>();

    public StudySession addStudySession(StudySession studySession) {
        studySessions.add(Objects.requireNonNull(studySession, "studySession must not be null"));
        return studySession;
    }

    public boolean removeStudySession(StudySession studySession) {
        return studySessions.remove(studySession);
    }

    public void editStudySession(StudySession studySession, Consumer<StudySession> changes) {
        requireManagedStudySession(studySession);
        Objects.requireNonNull(changes, "changes must not be null").accept(studySession);
    }

    public List<StudySession> listStudySessions() {
        return List.copyOf(studySessions);
    }

    private void requireManagedStudySession(StudySession studySession) {
        Objects.requireNonNull(studySession, "studySession must not be null");
        if (!studySessions.contains(studySession)) {
            throw new IllegalArgumentException("studySession is not managed by this service");
        }
    }
}

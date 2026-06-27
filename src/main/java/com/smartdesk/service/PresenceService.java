package com.smartdesk.service;

import com.smartdesk.model.SensorReading;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;


@Service
public class PresenceService {

    public enum PresenceState { MASADA, KISA_MOLA, UZAK }

    private static final double PRESENCE_DISTANCE_CM = 60;
    private static final long SHORT_BREAK_MINUTES = 5;
    private static final long AWAY_MINUTES = 15;
    private static final long BREAK_SUGGESTION_MINUTES = 45;

    private LocalDateTime lastSeenAt;
    private LocalDateTime sessionStartedAt;
    private long totalSessionMinutes = 0;
    private PresenceState currentState = PresenceState.UZAK;

    public void update(SensorReading reading) {
        LocalDateTime now = LocalDateTime.now();
        Double distance = reading != null ? reading.getDistance() : null;

        if (distance != null && distance < PRESENCE_DISTANCE_CM) {
            lastSeenAt = now;
            if (sessionStartedAt == null) {
                sessionStartedAt = now;
            }
            currentState = PresenceState.MASADA;
            return;
        }

        if (lastSeenAt == null) {
            currentState = PresenceState.UZAK;
            return;
        }

        long minutesSinceSeen = Duration.between(lastSeenAt, now).toMinutes();
        if (minutesSinceSeen < SHORT_BREAK_MINUTES) {
            currentState = PresenceState.MASADA;
        } else if (minutesSinceSeen < AWAY_MINUTES) {
            currentState = PresenceState.KISA_MOLA;
        } else {
            currentState = PresenceState.UZAK;
            if (sessionStartedAt != null) {
                totalSessionMinutes += Duration.between(sessionStartedAt, lastSeenAt).toMinutes();
                sessionStartedAt = null;
            }
        }
    }

    public PresenceState getState() {
        return currentState;
    }

    public int getSessionMinutes() {
        if (sessionStartedAt == null) return 0;
        return (int) Duration.between(sessionStartedAt, LocalDateTime.now()).toMinutes();
    }

    public long getTotalMinutes() {
        return totalSessionMinutes;
    }

    public boolean shouldSuggestBreak() {
        if (currentState != PresenceState.MASADA || sessionStartedAt == null) return false;
        return Duration.between(sessionStartedAt, LocalDateTime.now()).toMinutes() >= BREAK_SUGGESTION_MINUTES;
    }
}

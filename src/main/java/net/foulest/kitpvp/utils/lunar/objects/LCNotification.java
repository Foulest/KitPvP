package net.foulest.kitpvp.utils.lunar.objects;


import com.google.common.base.Preconditions;

import java.time.Duration;

public final class LCNotification {

    private final String message;
    private final long durationMs;
    private final Level level;

    public LCNotification(String message, Duration duration) {
        this(message, duration, Level.INFO);
    }

    public LCNotification(String message, Duration duration, Level level) {
        this.message = (String) Preconditions.checkNotNull((Object) message, "message");
        this.durationMs = duration.toMillis();
        this.level = (Level) Preconditions.checkNotNull((Object) level, "level");
    }

    public String getMessage() {
        return message;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public Level getLevel() {
        return level;
    }

    public enum Level {
        INFO,
        ERROR
    }
}

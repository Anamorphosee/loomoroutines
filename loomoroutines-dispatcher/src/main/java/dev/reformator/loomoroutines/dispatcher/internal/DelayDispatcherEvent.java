package dev.reformator.loomoroutines.dispatcher.internal;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;

public final class DelayDispatcherEvent implements DispatcherEvent {
    private final Duration duration;

    public DelayDispatcherEvent(@NotNull Duration duration) {
        Objects.requireNonNull(duration);
        this.duration = duration;
    }

    public @NotNull Duration getDuration() {
        return duration;
    }
}

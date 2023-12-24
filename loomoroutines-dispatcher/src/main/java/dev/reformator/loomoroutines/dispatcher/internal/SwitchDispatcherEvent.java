package dev.reformator.loomoroutines.dispatcher.internal;

import dev.reformator.loomoroutines.dispatcher.Dispatcher;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class SwitchDispatcherEvent implements DispatcherEvent {
    private final Dispatcher dispatcher;

    public SwitchDispatcherEvent(@NotNull Dispatcher dispatcher) {
        Objects.requireNonNull(dispatcher);
        this.dispatcher = dispatcher;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }
}

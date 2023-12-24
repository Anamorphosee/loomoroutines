package dev.reformator.loomoroutines.dispatcher.internal;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public final class AwaitDispatcherEvent implements DispatcherEvent {
    private final Consumer<? super Runnable> subscribeFunc;

    public AwaitDispatcherEvent(@NotNull Consumer<? super Runnable> subscribeFunc) {
        Objects.requireNonNull(subscribeFunc);
        this.subscribeFunc = subscribeFunc;
    }

    public @NotNull Consumer<? super Runnable> getSubscribeFunc() {
        return subscribeFunc;
    }
}

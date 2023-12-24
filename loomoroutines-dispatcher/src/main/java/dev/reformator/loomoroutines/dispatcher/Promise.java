package dev.reformator.loomoroutines.dispatcher;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface Promise<T> {
    enum State {
        RUNNING, COMPLETED, EXCEPTION
    }

    @NotNull State getState();

    @NotNull T join();

    void addListener(@NotNull Consumer<? super PromiseResult<? extends T>> listener);
}

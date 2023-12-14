package dev.reformator.loomoroutines.common;

import org.jetbrains.annotations.NotNull;

public interface RunningCoroutine<T> {
    @NotNull T getCoroutineContext();

    void suspend();
}

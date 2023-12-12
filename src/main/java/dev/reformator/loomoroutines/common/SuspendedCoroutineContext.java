package dev.reformator.loomoroutines.common;

import org.jetbrains.annotations.NotNull;

public interface SuspendedCoroutineContext<T extends Coroutine<T>> {
    @NotNull T getCoroutine();

    void resume();
}

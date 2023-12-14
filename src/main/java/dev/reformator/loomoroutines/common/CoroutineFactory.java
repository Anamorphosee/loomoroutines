package dev.reformator.loomoroutines.common;

import org.jetbrains.annotations.NotNull;

public interface CoroutineFactory {
    <T> SuspendedCoroutine<T> createCoroutine(@NotNull T context, @NotNull Runnable body);
}

package dev.reformator.loomoroutines.common.internal;

import dev.reformator.loomoroutines.common.SuspendedCoroutine;
import org.jetbrains.annotations.NotNull;

public interface CoroutineFactory {
    <T> SuspendedCoroutine<T> createCoroutine(@NotNull T context, @NotNull Runnable body);
}

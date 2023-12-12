package dev.reformator.loomoroutines.internal;

import dev.reformator.loomoroutines.common.ConsumerNotNull;
import dev.reformator.loomoroutines.common.Coroutine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public interface CoroutineScopeResolver {
    void performInCoroutineScope(@NotNull Runnable action);

    @Nullable List<@NotNull Coroutine<?>> getCoroutinesScope();

    default boolean isCoroutineInScope(@NotNull Coroutine<?> coroutine) {
        Objects.requireNonNull(coroutine);
        var coroutines = getCoroutinesScope();
        if (coroutines == null) {
            return false;
        }
        return coroutines.contains(coroutine);
    }
}

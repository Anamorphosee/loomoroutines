package dev.reformator.loomoroutines.common.internal;

import dev.reformator.loomoroutines.common.FinishedCoroutine;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public record FinishedCoroutineImpl<T>(@NotNull T context) implements FinishedCoroutine<T>, Serializable {
    @Override
    public @NotNull T getCoroutineContext() {
        return context();
    }
}

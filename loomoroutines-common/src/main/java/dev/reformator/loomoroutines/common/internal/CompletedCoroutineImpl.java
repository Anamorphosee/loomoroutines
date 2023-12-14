package dev.reformator.loomoroutines.common.internal;

import dev.reformator.loomoroutines.common.CompletedCoroutine;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public record CompletedCoroutineImpl<T>(@NotNull T context) implements CompletedCoroutine<T>, Serializable {
    @Override
    public @NotNull T getCoroutineContext() {
        return context();
    }
}

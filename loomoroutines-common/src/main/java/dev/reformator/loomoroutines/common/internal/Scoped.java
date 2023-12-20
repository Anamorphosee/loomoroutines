package dev.reformator.loomoroutines.common.internal;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface Scoped<T> {
    void performReusable(@NotNull Supplier<? extends T> ifNotSet, @NotNull Runnable action);

    @NotNull T get();
}

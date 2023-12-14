package dev.reformator.loomoroutines.common;

import org.jetbrains.annotations.NotNull;

public non-sealed interface SuspendedCoroutine<T> extends CoroutinePoint<T> {
    @NotNull CoroutinePoint<T> resume();
}

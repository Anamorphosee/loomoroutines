package dev.reformator.loomoroutines.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface CoroutinePoint<T> permits SuspendedCoroutine, FinishedCoroutine {
    @NotNull T getCoroutineContext();

    default @Nullable SuspendedCoroutine<T> ifSuspended() {
        if (this instanceof SuspendedCoroutine<T> suspendedCoroutine) {
            return suspendedCoroutine;
        }
        return null;
    }
}

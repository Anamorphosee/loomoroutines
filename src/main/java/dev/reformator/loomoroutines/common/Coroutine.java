package dev.reformator.loomoroutines.common;

import dev.reformator.loomoroutines.internal.Registry;
import jdk.incubator.concurrent.ScopedValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface Coroutine<T extends Coroutine<T>> {
    void suspend(@NotNull ConsumerNotNull<? super SuspendedCoroutineContext<? extends T>> listener);

    static @NotNull @Unmodifiable List<@NotNull Coroutine<?>> getCoroutinesInScope() {
        var coroutines = Registry.coroutineScopeResolver.getCoroutinesScope();
        if (coroutines == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(coroutines);
    }

    static boolean isCoroutineInScope(@NotNull Coroutine<?> coroutine) {
        return Registry.coroutineScopeResolver.isCoroutineInScope(coroutine);
    }
}

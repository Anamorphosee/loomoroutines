package dev.reformator.loomoroutines.internal;

import dev.reformator.loomoroutines.common.Coroutine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ThreadLocalCoroutineScopeResolver implements CoroutineScopeResolver {
    private ThreadLocal<List<Coroutine<?>>> coroutinesInScope = new ThreadLocal();

    @Override
    public void performInCoroutineScope(@NotNull Runnable action) {
        if (coroutinesInScope.get() == null) {
            try {
                coroutinesInScope.set(new ArrayList<>());
                action.run();
            } finally {
                coroutinesInScope.remove();
            }
        } else {
            action.run();
        }
    }

    @Override
    public @Nullable List<@NotNull Coroutine<?>> getCoroutinesScope() {
        return coroutinesInScope.get();
    }
}

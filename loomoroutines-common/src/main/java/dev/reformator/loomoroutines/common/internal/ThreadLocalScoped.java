package dev.reformator.loomoroutines.common.internal;

import dev.reformator.loomoroutines.common.Scoped;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public class ThreadLocalScoped<T> implements Scoped<T> {
    private static final Object noValue = new Object();

    private final ThreadLocal<T> threadLocal = new ThreadLocal<>() {
        @Override
        @SuppressWarnings("unchecked")
        protected T initialValue() {
            return (T) noValue;
        }
    };

    @Override
    public void performReusable(@NotNull Supplier<? extends T> ifNotSet, @NotNull Runnable action) {
        Objects.requireNonNull(ifNotSet);
        if (threadLocal.get() == noValue) {
            try {
                threadLocal.set(ifNotSet.get());
                action.run();
            } finally {
                threadLocal.remove();
            }
        } else {
            action.run();
        }
    }

    @Override
    public @NotNull T get() {
        var value = threadLocal.get();
        if (value == noValue) {
            threadLocal.remove();
            throw new IllegalStateException("Scoped is not set.");
        }
        return value;
    }
}

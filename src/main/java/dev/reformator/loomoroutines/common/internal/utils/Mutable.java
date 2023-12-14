package dev.reformator.loomoroutines.common.internal.utils;

import org.jetbrains.annotations.NotNull;

public class Mutable<T> {
    private T value;

    public Mutable(@NotNull T value) {
        this.value = value;
    }

    public final @NotNull T get() {
        return value;
    }

    public final void set(@NotNull T value) {
        this.value = value;
    }
}

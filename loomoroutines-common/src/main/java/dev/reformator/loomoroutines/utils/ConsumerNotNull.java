package dev.reformator.loomoroutines.utils;

import org.jetbrains.annotations.NotNull;

public interface ConsumerNotNull<T> {
    void accept(@NotNull T value);
}

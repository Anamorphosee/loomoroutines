package dev.reformator.loomoroutines.common;

import org.jetbrains.annotations.NotNull;

public interface ConsumerNotNull<T> {
    void accept(@NotNull T value);
}

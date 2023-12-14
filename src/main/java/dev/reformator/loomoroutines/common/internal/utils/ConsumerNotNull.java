package dev.reformator.loomoroutines.common.internal.utils;

import org.jetbrains.annotations.NotNull;

public interface ConsumerNotNull<T> {
    void accept(@NotNull T value);
}

package dev.reformator.loomoroutines.internal.utils;

import org.jetbrains.annotations.NotNull;

public class SpecialImpl implements Special {
    @Override
    public void throwException(@NotNull Throwable exception) {
        if (exception instanceof RuntimeException e) {
            throw e;
        }
        if (exception instanceof Error e) {
            throw e;
        }
        throw new RuntimeException(exception);
    }
}

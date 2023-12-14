package dev.reformator.loomoroutines.internal.utils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

public interface Special {
    Special instance = getInstance();

    void throwException(@NotNull Throwable exception);

    private static Special getInstance() {
        try {
            return (Special) Class.forName(
                    "dev.reformator.loomoroutines.internal.utils.SpecialImpl"
            ).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException |
                 ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

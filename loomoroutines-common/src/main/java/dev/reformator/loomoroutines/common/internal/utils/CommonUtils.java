package dev.reformator.loomoroutines.common.internal.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.IntConsumer;

public class CommonUtils {
    private CommonUtils() { }

    public static void repeat(int count, @NotNull IntConsumer action) {
        if (count < 0) {
            throw new IllegalArgumentException("count[" + count + "] cannot be less then 0.");
        }
        Objects.requireNonNull(action);
        for (int i = 0; i < count; i++) {
            action.accept(i);
        }
    }
}

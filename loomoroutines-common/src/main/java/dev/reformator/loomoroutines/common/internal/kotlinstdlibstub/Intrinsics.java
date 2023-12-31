package dev.reformator.loomoroutines.common.internal.kotlinstdlibstub;

import java.util.Objects;

public class Intrinsics {
    private Intrinsics() { }

    public static void checkNotNullParameter(Object parameter, String name) {
        if (parameter == null) {
            throw new NullPointerException("parameter `" + name + "` must not be null.");
        }
    }

    public static void checkNotNullExpressionValue(Object parameter, String expression) {
        if (parameter == null) {
            throw new NullPointerException(expression + " must not be null.");
        }
    }

    public static void checkNotNull(Object object) {
        Objects.requireNonNull(object);
    }

    public static void checkNotNull(Object object, String message) {
        Objects.requireNonNull(object, message);
    }

    public static boolean areEqual(Object first, Object second) {
        return Objects.equals(first, second);
    }
}

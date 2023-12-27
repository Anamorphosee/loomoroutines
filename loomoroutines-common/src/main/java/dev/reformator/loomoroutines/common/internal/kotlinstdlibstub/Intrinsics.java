package dev.reformator.loomoroutines.common.internal.kotlinstdlibstub;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

    public static boolean areEqual(Object first, Object second) {
        return Objects.equals(first, second);
    }
}

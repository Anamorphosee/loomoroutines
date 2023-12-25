package dev.reformator.loomoroutines.common.internal.kotlinstdlibstub;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Intrinsics {
    private Intrinsics() { }

    public static void checkNotNullParameter(Object parameter, String name) {
        Objects.requireNonNull(parameter, name + " must not be null.");
    }

    public static void checkNotNullExpressionValue(Object parameter, String name) {
        checkNotNullParameter(parameter, name);
    }

    public static void checkNotNull(Object object) {
        Objects.requireNonNull(object);
    }

    public static boolean areEqual(Object first, Object second) {
        return Objects.equals(first, second);
    }

    public static <T> List<T> listOf(T element) {
        return Collections.singletonList(element);
    }

    public static <T> List<T> emptyList() {
        return Collections.emptyList();
    }

    public static int getLastIndex(List<?> list) {
        return list.size() - 1;
    }

    public static <T> T removeLast(List<T> list) {
        return list.remove(getLastIndex(list));
    }
}

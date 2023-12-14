package dev.reformator.loomoroutines.common.internal.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class CollectionUtils {
    private CollectionUtils() { }

    public static <T> int indexOf(@NotNull @Unmodifiable List<T> list, @NotNull Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        for (int i = 0; i < list.size(); i++) {
            if (predicate.test(list.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public static <T> @NotNull @Unmodifiable List<T> subList(@NotNull @Unmodifiable List<T> list, int fromIndex) {
        return list.subList(fromIndex, list.size());
    }

    public static <T> T removeLast(@NotNull List<T> list) {
        return list.remove(list.size() - 1);
    }

    public static <T> @Nullable T find(@NotNull @Unmodifiable Iterable<T> collection, @NotNull Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        for (var item: collection) {
            if (predicate.test(item)) {
                return item;
            }
        }
        return null;
    }
}

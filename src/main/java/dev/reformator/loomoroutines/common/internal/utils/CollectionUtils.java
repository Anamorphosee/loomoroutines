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
        var index = 0;
        for (var item: list) {
            if (predicate.test(item)) {
                return index;
            }
            index++;
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

    public static <T> @Nullable T findLast(@NotNull @Unmodifiable List<T> list, @NotNull Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        var iterator = list.listIterator(list.size());
        while (iterator.hasPrevious()) {
            var item = iterator.previous();
            if (predicate.test(item)) {
                return item;
            }
        }
        return null;
    }
}

package dev.reformator.loomoroutines.common.internal.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

public class CommonUtils {
    private CommonUtils() { }

    private static final Predicate<Object> alwaysTruePredicate = it -> true;

    public static void repeat(int count, @NotNull IntConsumer action) {
        if (count < 0) {
            throw new IllegalArgumentException("count[" + count + "] cannot be less then 0.");
        }
        Objects.requireNonNull(action);
        for (int i = 0; i < count; i++) {
            action.accept(i);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> getAlwaysTruePredicate() {
        return (Predicate<T>) alwaysTruePredicate;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static RuntimeException throwUnchecked(@NotNull Throwable exception) {
        throwUncheckedInternal(exception);
        throw new RuntimeException("Something wrong. Not reachable code.");
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwUncheckedInternal(Throwable exception) throws T {
        throw (T) exception;
    }
}

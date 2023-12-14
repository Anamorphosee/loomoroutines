package dev.reformator.loomoroutines.common.internal.utils;

import dev.reformator.loomoroutines.common.RunningCoroutine;
import dev.reformator.loomoroutines.common.SuspendedCoroutine;
import dev.reformator.loomoroutines.common.internal.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class Utils {
    private Utils() { }

    private static final Supplier<List<RunningCoroutine<?>>> newListSupplier = ArrayList::new;

    public static void performInCoroutinesScope(@NotNull Runnable action) {
        Registry.runningCoroutinesScoped.performReusable(newListSupplier, action);
    }

    public static @NotNull @Unmodifiable List<RunningCoroutine<?>> getRunningCoroutines() {
        return Collections.unmodifiableList(getRunningCoroutinesInternal());
    }

    public static @Nullable RunningCoroutine<?> getRunningCoroutineByContext(@Nullable Object context) {
        return CollectionUtils.findLast(
                getRunningCoroutinesInternal(),
                coroutine -> coroutine.getCoroutineContext() == context
        );
    }

    public static <T> @NotNull SuspendedCoroutine<T> createCoroutine(@NotNull T context, @NotNull Runnable body) {
        return Registry.coroutineFactory.createCoroutine(context, body);
    }

    private static List<RunningCoroutine<?>> getRunningCoroutinesInternal() {
        var result = new Mutable<List<RunningCoroutine<?>>>(Collections.emptyList());
        performInCoroutinesScope(() -> result.set(Registry.runningCoroutinesScoped.get()));
        return result.get();
    }
}

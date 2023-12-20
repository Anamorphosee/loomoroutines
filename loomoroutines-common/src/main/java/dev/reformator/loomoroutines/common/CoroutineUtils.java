package dev.reformator.loomoroutines.common;

import dev.reformator.loomoroutines.common.internal.Registry;
import dev.reformator.loomoroutines.common.internal.utils.CollectionUtils;
import dev.reformator.loomoroutines.common.internal.utils.CommonUtils;
import dev.reformator.loomoroutines.common.internal.utils.Mutable;
import dev.reformator.loomoroutines.common.internal.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CoroutineUtils {
    private CoroutineUtils() { }

    private static final List<RunningCoroutine<?>> unmodifiableEmptyList =
            Collections.unmodifiableList(Collections.emptyList());
    private static final Supplier<List<RunningCoroutine<?>>> emptyListSupplier = () -> unmodifiableEmptyList;

    public static @NotNull @Unmodifiable List<RunningCoroutine<?>> getRunningCoroutines() {
        return Collections.unmodifiableList(getRunningCoroutinesInternal());
    }

    public static @Nullable RunningCoroutine<?> getRunningCoroutineByContextPredicate(
            @NotNull Predicate<Object> predicate
    ) {
        Objects.requireNonNull(predicate);
        return CollectionUtils.findLast(
                getRunningCoroutinesInternal(),
                coroutine -> predicate.test(coroutine.getCoroutineContext())
        );
    }

    @SuppressWarnings("unchecked")
    public static <T> @Nullable RunningCoroutine<? extends T> getRunningCoroutineByContext(@NotNull T context) {
        return (RunningCoroutine<? extends T>) getRunningCoroutineByContextPredicate(Predicate.isEqual(context));
    }

    @SuppressWarnings("unchecked")
    public static <T> @Nullable RunningCoroutine<? extends T> getRunningCoroutineByContextPredicate(
            @NotNull Predicate<? super T> predicate,
            @NotNull Class<T> type
    ) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(type);
        return (RunningCoroutine<? extends T>) getRunningCoroutineByContextPredicate(context ->
                type.isInstance(context) && predicate.test((T) context)
        );
    }

    public static <T> @Nullable RunningCoroutine<? extends T> getRunningCoroutineByContextType(@NotNull Class<T> type) {
        return getRunningCoroutineByContextPredicate(CommonUtils.getAlwaysTruePredicate(), type);
    }

    public static <T> @NotNull SuspendedCoroutine<T> createCoroutine(@NotNull T context, @NotNull Runnable body) {
        return Registry.coroutineFactory.createCoroutine(context, body);
    }

    private static List<RunningCoroutine<?>> getRunningCoroutinesInternal() {
        var result = new Mutable<List<RunningCoroutine<?>>>(Collections.emptyList());
        Registry.runningCoroutinesScoped.performReusable(
                emptyListSupplier,
                () -> result.set(Utils.getRunningCoroutines())
        );
        return result.get();
    }
}

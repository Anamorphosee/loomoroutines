package dev.reformator.loomoroutines.dispatcher.utils;

import dev.reformator.loomoroutines.dispatcher.Dispatcher;
import dev.reformator.loomoroutines.dispatcher.Promise;
import dev.reformator.loomoroutines.utils.CoroutineUtils;
import dev.reformator.loomoroutines.common.internal.utils.Mutable;
import dev.reformator.loomoroutines.dispatcher.internal.DispatcherContext;
import dev.reformator.loomoroutines.dispatcher.internal.DispatcherContextImpl;
import dev.reformator.loomoroutines.dispatcher.internal.utils.InternalDispatcherUtils;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DispatcherUtils {
    private DispatcherUtils() { }

    private static final Supplier<Void> nullGetter = () -> null;

    public static void delay(@NotNull Duration duration) {
        Objects.requireNonNull(duration);
        var coroutine = getDispatcherCoroutine();
        coroutine.getCoroutineContext().setDelayLastEvent(duration);
        coroutine.suspend();
    }

    public static void delay(long millis) {
        delay(Duration.ofMillis(millis));
    }

    public static void executeInDispatcher(@NotNull Dispatcher newDispatcher, @NotNull Runnable action) {
        Objects.requireNonNull(newDispatcher);
        Objects.requireNonNull(action);
        var coroutine = getDispatcherCoroutine();
        var oldDispatcher = Objects.requireNonNull(coroutine.getCoroutineContext().getDispatcher());
        if (oldDispatcher == newDispatcher) {
            action.run();
        } else {
            coroutine.getCoroutineContext().setSwitchLastEvent(newDispatcher);
            coroutine.suspend();
            try {
                action.run();
            } finally {
                var newCoroutine = getDispatcherCoroutine();
                newCoroutine.getCoroutineContext().setSwitchLastEvent(oldDispatcher);
                newCoroutine.suspend();
            }
        }
    }

    public static <T> @NotNull T executeInDispatcher(
            @NotNull Dispatcher newDispatcher,
            @NotNull Supplier<? extends T> action
    ) {
        Objects.requireNonNull(action);
        var result = new Mutable<T>();
        executeInDispatcher(newDispatcher, () -> result.set(action.get()));
        return result.get();
    }

    public static void await(@NotNull Consumer<? super Runnable> subscribeFunc) {
        Objects.requireNonNull(subscribeFunc);
        var coroutine = getDispatcherCoroutine();
        coroutine.getCoroutineContext().setAwaitLastEvent(subscribeFunc);
        coroutine.suspend();
    }

    public static <T> @NotNull Promise<T> dispatch(@NotNull Dispatcher dispatcher, @NotNull Supplier<T> action) {
        var context = new DispatcherContextImpl<T>();
        var result = new Mutable<T>();
        var coroutine = CoroutineUtils.createCoroutine(
                context,
                () -> result.set(action.get())
        );
        InternalDispatcherUtils.dispatchCoroutine(dispatcher, coroutine, result::get);
        return context.getPromise();
    }

    public static @NotNull Promise<Void> dispatch(@NotNull Dispatcher dispatcher, @NotNull Runnable action) {
        var context = new DispatcherContextImpl<Void>();
        var coroutine = CoroutineUtils.createCoroutine(context, action);
        InternalDispatcherUtils.dispatchCoroutine(dispatcher, coroutine, nullGetter);
        return context.getPromise();
    }

    @SuppressWarnings("unchecked")
    private static RunningCoroutine<? extends DispatcherContext<?>> getDispatcherCoroutine() {
        var coroutine = CoroutineUtils.getRunningCoroutineByContextType(DispatcherContext.class);
        if (coroutine == null) {
            throw new IllegalStateException("Not in dispatcher context");
        }
        return (RunningCoroutine<? extends DispatcherContext<?>>) coroutine;
    }
}

package dev.reformator.loomoroutines.dispatcher.internal.utils;

import dev.reformator.loomoroutines.common.CompletedCoroutine;
import dev.reformator.loomoroutines.common.NotRunningCoroutine;
import dev.reformator.loomoroutines.common.SuspendedCoroutine;
import dev.reformator.loomoroutines.dispatcher.Dispatcher;
import dev.reformator.loomoroutines.dispatcher.ExceptionalPromiseResult;
import dev.reformator.loomoroutines.dispatcher.SucceedPromiseResult;
import dev.reformator.loomoroutines.dispatcher.internal.AwaitDispatcherEvent;
import dev.reformator.loomoroutines.dispatcher.internal.DelayDispatcherEvent;
import dev.reformator.loomoroutines.dispatcher.internal.DispatcherContext;
import dev.reformator.loomoroutines.dispatcher.internal.SwitchDispatcherEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class InternalDispatcherUtils {
    private InternalDispatcherUtils() { }

    public static <T> void dispatchCoroutine(
            @NotNull Dispatcher dispatcher,
            @NotNull SuspendedCoroutine<? extends DispatcherContext<? super T>> coroutine,
            @NotNull Supplier<? extends T> resultGetter
    ) {
        Objects.requireNonNull(coroutine);
        Objects.requireNonNull(resultGetter);
        if (dispatcher.canExecuteInCurrentThread()) {
            dispatchCoroutineLoop(coroutine, dispatcher, resultGetter);
        } else {
            dispatcher.execute(() -> dispatchCoroutineLoop(coroutine, dispatcher, resultGetter));
        }
    }

    private static <T> void dispatchCoroutineLoop(
            SuspendedCoroutine<? extends DispatcherContext<? super T>> coroutine,
            Dispatcher dispatcher,
            Supplier<? extends T> resultGetter
    ) {
        var context = coroutine.getCoroutineContext();
        context.setDispatcher(dispatcher);
        NotRunningCoroutine<? extends DispatcherContext<? super T>> nextPoint;
        try {
            nextPoint = coroutine.resume();
        } catch (Throwable e) {
            context.setDispatcher(null);
            context.complete(new ExceptionalPromiseResult<>(e));
            return;
        }
        context.setDispatcher(null);
        if (nextPoint instanceof SuspendedCoroutine<? extends DispatcherContext<? super T>> nextCoroutine) {
            var event = context.getLastEvent();
            if (event instanceof DelayDispatcherEvent delayEvent) {
                dispatcher.scheduleExecute(
                        delayEvent.getDuration(),
                        () -> dispatchCoroutineLoop(nextCoroutine, dispatcher, resultGetter)
                );
            } else if (event instanceof SwitchDispatcherEvent switchEvent) {
                var newDispatcher = switchEvent.getDispatcher();
                newDispatcher.execute(() -> dispatchCoroutineLoop(nextCoroutine, newDispatcher, resultGetter));
            } else if (event instanceof AwaitDispatcherEvent awaitEvent) {
                awaitEvent.getSubscribeFunc().accept(new Runnable() {
                    private final AtomicBoolean dirty = new AtomicBoolean(false);

                    @Override
                    public void run() {
                        if (dirty.compareAndSet(false, true)) {
                            dispatcher.execute(() -> dispatchCoroutineLoop(nextCoroutine, dispatcher, resultGetter));
                        } else {
                            throw new IllegalStateException("Awaiter has already called.");
                        }
                    }
                });
            } else {
                throw new IllegalArgumentException("Unknown event: " + event);
            }
        } else if (nextPoint instanceof CompletedCoroutine) {
            context.complete(SucceedPromiseResult.getOf(resultGetter.get()));
        } else {
            throw new IllegalStateException("Unknown point: " + nextPoint);
        }
    }
}

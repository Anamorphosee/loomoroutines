package dev.reformator.loomoroutines.common.internal;

import dev.reformator.loomoroutines.common.CoroutineFactory;
import dev.reformator.loomoroutines.common.CoroutinePoint;
import dev.reformator.loomoroutines.common.RunningCoroutine;
import dev.reformator.loomoroutines.common.SuspendedCoroutine;
import dev.reformator.loomoroutines.common.internal.utils.CollectionUtils;
import dev.reformator.loomoroutines.common.internal.utils.Mutable;
import dev.reformator.loomoroutines.common.internal.utils.CommonUtils;
import dev.reformator.loomoroutines.common.internal.utils.Utils;
import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LoomSuspendedCoroutine<T> implements SuspendedCoroutine<T> {
    public static @NotNull CoroutineFactory factory = new CoroutineFactory() {
        @Override
        public <T> SuspendedCoroutine<T> createCoroutine(@NotNull T context, @NotNull Runnable body) {
            var continuation = new Continuation(new ContinuationScope("loomoroutine"), body);
            return new LoomSuspendedCoroutine<>(continuation, context, Collections.singletonList(new Running<>(continuation, context)));
        }
    };

    private final Continuation continuation;
    private final T context;
    private final List<RunningCoroutine<?>> coroutinesStack;
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    public LoomSuspendedCoroutine(
            @NotNull Continuation continuation,
            @NotNull T context,
            @NotNull @Unmodifiable List<RunningCoroutine<?>> coroutinesStack
    ) {
        this.continuation = Objects.requireNonNull(continuation);
        this.context = Objects.requireNonNull(context);
        this.coroutinesStack = Objects.requireNonNull(coroutinesStack);
    }

    @Override
    public @NotNull T getCoroutineContext() {
        return context;
    }

    @Override
    public @NotNull CoroutinePoint<T> resume() {
        if (dirty.compareAndSet(false, true)) {
            var newCoroutineStack = new Mutable<List<RunningCoroutine<?>>>(Collections.emptyList());
            Utils.performInCoroutinesScope(() -> {
                Registry.runningCoroutinesScoped.get().addAll(coroutinesStack);
                try {
                    continuation.run();
                } finally {
                    var runningCoroutines = Registry.runningCoroutinesScoped.get();
                    var startStackIndex = CollectionUtils.indexOf(
                            runningCoroutines,
                            it -> it instanceof Running<?> running && running.continuation == continuation
                    );
                    newCoroutineStack.set(new ArrayList<>(CollectionUtils.subList(runningCoroutines, startStackIndex)));
                    CommonUtils.repeat(newCoroutineStack.get().size(), i -> CollectionUtils.removeLast(runningCoroutines));
                }
            });

            if (continuation.isDone()) {
                return new FinishedCoroutineImpl<>(context);
            } else {
                return new LoomSuspendedCoroutine<>(continuation, context, newCoroutineStack.get());
            }
        } else {
            throw new IllegalStateException("Suspended point was already resumed.");
        }
    }

    private static final class Running<T> implements RunningCoroutine<T> {
        private final Continuation continuation;
        private final T context;

        public Running(@NotNull Continuation continuation, @NotNull T context) {
            this.continuation = Objects.requireNonNull(continuation);
            this.context = context;
        }

        @Override
        public @NotNull T getCoroutineContext() {
            return context;
        }

        @Override
        public void suspend() {
            Continuation.yield(continuation.getScope());
        }
    }
}

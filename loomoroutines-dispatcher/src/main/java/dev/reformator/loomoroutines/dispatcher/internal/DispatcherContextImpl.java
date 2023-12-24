package dev.reformator.loomoroutines.dispatcher.internal;

import dev.reformator.loomoroutines.utils.CoroutineUtils;
import dev.reformator.loomoroutines.common.RunningCoroutine;
import dev.reformator.loomoroutines.common.internal.utils.CommonUtils;
import dev.reformator.loomoroutines.common.internal.utils.Mutable;
import dev.reformator.loomoroutines.dispatcher.Dispatcher;
import dev.reformator.loomoroutines.dispatcher.Promise;
import dev.reformator.loomoroutines.dispatcher.PromiseResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class DispatcherContextImpl<T> implements DispatcherContext<T>, Promise<T> {
    private sealed interface InnerState<T> {
        @NotNull State getState();
    }

    private static sealed class RunningInnerState<T> implements InnerState<T> {
        private static final RunningInnerState<Object> instance = new RunningInnerState<>();

        @Override
        public @NotNull State getState() {
            return State.RUNNING;
        }

        @SuppressWarnings("unchecked")
        public static <T> RunningInnerState<T> getInstance() {
            return (RunningInnerState<T>) instance;
        }
    }

    private static final class NotEmptyRunningInnerState<T> extends RunningInnerState<T> {
        private final Consumer<? super PromiseResult<? extends T>> listener;
        private final RunningInnerState<T> next;

        public NotEmptyRunningInnerState(
                @NotNull Consumer<? super PromiseResult<? extends T>> listener,
                @NotNull RunningInnerState<T> next
        ) {
            this.listener = listener;
            this.next = next;
        }
    }

    private static final class CompletedInnerState<T> implements InnerState<T> {
        private final PromiseResult<? extends T> result;

        private CompletedInnerState(@NotNull PromiseResult<? extends T> result) {
            this.result = result;
        }

        @Override
        public @NotNull State getState() {
            if (result.isSucceed()) {
                return State.COMPLETED;
            } else {
                return State.EXCEPTION;
            }
        }

        public PromiseResult<? extends T> getResult() {
            return result;
        }
    }

    private final AtomicReference<InnerState<T>> state = new AtomicReference<>(RunningInnerState.getInstance());
    private Dispatcher dispatcher = null;
    private final AtomicReference<DispatcherEvent> event = new AtomicReference<>();

    @Override
    public @NotNull State getState() {
        return state.get().getState();
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull T join() {
        if (state.get() instanceof CompletedInnerState<T> completedState) {
            return completedState.getResult().get();
        }

        var mutual = new Mutable<PromiseResult<? extends T>>();

        var runningCoroutine =
                (RunningCoroutine<? extends DispatcherContext<?>>)
                        CoroutineUtils.getRunningCoroutineByContextType(DispatcherContext.class);
        if (runningCoroutine != null) {
            runningCoroutine.getCoroutineContext().setAwaitLastEvent(awaiter ->
                    addListener(result -> {
                        mutual.set(result);
                        awaiter.run();
                    })
            );
            runningCoroutine.suspend();
        } else {
            var semaphore = new Semaphore(0);

            addListener(result -> {
                mutual.set(result);
                semaphore.release();
            });
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                throw CommonUtils.throwUnchecked(e);
            }
        }

        return mutual.get().get();
    }

    @Override
    public void addListener(@NotNull Consumer<? super PromiseResult<? extends T>> listener) {
        Objects.requireNonNull(listener);
        while (true) {
            var state = this.state.get();
            if (state instanceof RunningInnerState<T> runningState) {
                var nextState = new NotEmptyRunningInnerState<>(listener, runningState);
                if (this.state.compareAndSet(state, nextState)) {
                    return;
                }
            } else if (state instanceof CompletedInnerState<T> completedState) {
                callListener(listener, completedState.getResult());
                return;
            }
        }
    }

    @Override
    public @NotNull Promise<T> getPromise() {
        return this;
    }

    @Override
    public @Nullable Dispatcher getDispatcher() {
        return dispatcher;
    }

    @Override
    public void setDispatcher(@Nullable Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public @NotNull DispatcherEvent getLastEvent() {
        var event = this.event.getAndSet(null);
        if (event == null) {
            throw new IllegalStateException("dispatcher event is not set");
        }
        return event;
    }

    @Override
    public void setAwaitLastEvent(@NotNull Consumer<? super Runnable> subscribeFunc) {
        setEvent(new AwaitDispatcherEvent(subscribeFunc));
    }

    @Override
    public void setDelayLastEvent(@NotNull Duration duration) {
        setEvent(new DelayDispatcherEvent(duration));
    }

    @Override
    public void setSwitchLastEvent(@NotNull Dispatcher newDispatcher) {
        setEvent(new SwitchDispatcherEvent(dispatcher));
    }

    @Override
    public void complete(@NotNull PromiseResult<? extends T> result) {
        Objects.requireNonNull(result);
        while (true) {
            var state = this.state.get();
            if (state instanceof RunningInnerState<T> runningState) {
                if (this.state.compareAndSet(state, new CompletedInnerState<>(result))) {
                    while (runningState instanceof NotEmptyRunningInnerState<T> notEmptyRunningState) {
                        callListener(notEmptyRunningState.listener, result);
                        runningState = notEmptyRunningState.next;
                    }
                    return;
                }
            } else if (state instanceof CompletedInnerState) {
                throw new IllegalStateException("Dispatcher coroutine is already completed.");
            } else {
                throw new IllegalStateException("Unknown state: " + state);
            }
        }
    }

    private static <T> void callListener(
            Consumer<? super PromiseResult<? extends T>> listener,
            PromiseResult<? extends T> result
    ) {
        try {
            listener.accept(result);
        } catch (Throwable e) {
            //TODO
        }
    }

    private void setEvent(DispatcherEvent event) {
        if (!this.event.compareAndSet(null, event)) {
            throw new IllegalStateException("dispatcher event is already set");
        }
    }
}

package dev.reformator.loomoroutines.utils;

import dev.reformator.loomoroutines.common.CompletedCoroutine;
import dev.reformator.loomoroutines.common.NotRunningCoroutine;
import dev.reformator.loomoroutines.common.CoroutineUtils;
import dev.reformator.loomoroutines.common.SuspendedCoroutine;
import dev.reformator.loomoroutines.common.internal.utils.CommonUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class GeneratorIterator<T> implements Iterator<T> {
    public interface Scope<T> {
        void emit(T value);
    }

    private final AtomicReference<BufferState> bufferState = new AtomicReference<>(BufferStateSingletons.NOT_BUFFERED);
    private SuspendedCoroutine<Context<T>> point;

    public GeneratorIterator(@NotNull Consumer<? super Scope<? super T>> generator) {
        Objects.requireNonNull(generator);
        var context = new Context<>(generator);
        point = CoroutineUtils.createCoroutine(context, context);
    }

    @Override
    public boolean hasNext() {
        return buffer();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T next() {
        while (true) {
            if (!buffer()) {
                throw new NoSuchElementException();
            }
            var currentState = bufferState.get();
            if (currentState instanceof BufferedBufferState buffered &&
                    bufferState.compareAndSet(buffered, BufferStateSingletons.NOT_BUFFERED)) {
                return (T) buffered.value();
            }
        }
    }

    private boolean buffer() {
        while (true) {
            var currentState = bufferState.get();
            if (currentState == BufferStateSingletons.BUFFERING) {
                throw new ConcurrentModificationException("Iterator is already modifying in an another thread.");
            }
            if (currentState instanceof BufferedBufferState) {
                return true;
            }
            if (currentState == BufferStateSingletons.FINISHED) {
                return false;
            }
            if (currentState instanceof ExceptionBufferState e) {
                CommonUtils.throwUnchecked(e.exception);
                throw new RuntimeException();
            }
            if (bufferState.compareAndSet(BufferStateSingletons.NOT_BUFFERED, BufferStateSingletons.BUFFERING)) {
                try {
                    var newPoint = point.resume();
                    if (newPoint instanceof SuspendedCoroutine<Context<T>> suspended) {
                        point = suspended;
                        bufferState.set(new BufferedBufferState(point.getCoroutineContext().buffer));
                        point.getCoroutineContext().buffer = null;
                        return true;
                    }
                    if (newPoint instanceof CompletedCoroutine<Context<T>>) {
                        point = null;
                        bufferState.set(BufferStateSingletons.FINISHED);
                        return false;
                    }
                    throw new RuntimeException();
                } catch (Throwable exception) {
                    bufferState.set(new ExceptionBufferState(exception));
                    CommonUtils.throwUnchecked(exception);
                    throw new RuntimeException();
                }
            }
        }
    }

    private static class Context<T> implements Runnable, Scope<T> {
        private final Consumer<? super Scope<? super T>> generator;
        private T buffer;

        public Context(Consumer<? super Scope<? super T>> generator) {
            this.generator = generator;
        }

        @Override
        public void run() {
            generator.accept(this);
        }

        @Override
        public void emit(T value) {
            var coroutine = Objects.requireNonNull(CoroutineUtils.getRunningCoroutineByContext(this));
            buffer = value;
            coroutine.suspend();
        }
    }

    private sealed interface BufferState permits BufferStateSingletons, BufferedBufferState, ExceptionBufferState { }

    private enum BufferStateSingletons implements BufferState {
        NOT_BUFFERED, BUFFERING, FINISHED
    }

    private record BufferedBufferState(Object value) implements BufferState { }

    private record ExceptionBufferState(Throwable exception) implements BufferState { }
}

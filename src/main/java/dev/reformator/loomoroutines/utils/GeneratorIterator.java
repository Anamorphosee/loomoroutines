package dev.reformator.loomoroutines.utils;

import dev.reformator.loomoroutines.common.ConsumerNotNull;
import dev.reformator.loomoroutines.common.SuspendedCoroutineContext;
import dev.reformator.loomoroutines.impl.BaseLoomCoroutine;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class GeneratorIterator<T> extends BaseLoomCoroutine<GeneratorIterator<T>> implements Iterator<T> {
    public interface Scope<T> {
        void emit(T value);
    }

    private T buffer = null;
    private boolean finished = false;
    private boolean buffered = false;
    private SuspendedCoroutineContext<? extends GeneratorIterator<T>> suspendedCoroutineContext = null;

    private GeneratorIterator(@NotNull Runnable body) {
        super(body);
    }

    public static <T> Iterator<T> newInstance(@NotNull ConsumerNotNull<? super Scope<? super T>> generator) {
        class GeneratorTask implements Runnable, Scope<T> {
            private GeneratorIterator<T> iterator = null;

            @Override
            public void run() {
                generator.accept(this);
            }

            @Override
            public void emit(T value) {
                iterator.suspend((context) -> {
                    iterator.buffer = value;
                    iterator.finished = false;
                    iterator.suspendedCoroutineContext = context;
                });
            }
        }
        var task = new GeneratorTask();
        var iterator = new GeneratorIterator<T>(task);
        task.iterator = iterator;
        return iterator;
    }

    @Override
    public boolean hasNext() {
        buffer();
        return !finished;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        buffered = false;
        return buffer;
    }

    private void buffer() {
        if (!buffered) {
            if (suspendedCoroutineContext == null) {
                start();
            } else {
                suspendedCoroutineContext.resume();
            }
            buffered = true;
        }
    }
}

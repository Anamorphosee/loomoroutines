package dev.reformator.loomoroutines.utils;

import dev.reformator.loomoroutines.common.internal.utils.ConsumerNotNull;
import dev.reformator.loomoroutines.common.CoroutinePoint;
import dev.reformator.loomoroutines.common.SuspendedCoroutine;
import dev.reformator.loomoroutines.common.internal.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class GeneratorIterator<T> implements Iterator<T> {
    public interface Scope<T> {
        void emit(T value);
    }

    private boolean buffered = false;
    private CoroutinePoint<Context<T>> point;

    public GeneratorIterator(@NotNull ConsumerNotNull<? super Scope<? super T>> generator) {
        var context = new Context<>(generator);
        point = Utils.createCoroutine(context, context);
    }

    @Override
    public boolean hasNext() {
        buffer();
        return point instanceof SuspendedCoroutine<?>;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        buffered = false;
        return point.getCoroutineContext().buffer;
    }

    private void buffer() {
        if (!buffered) {
            point = ((SuspendedCoroutine<Context<T>>) point).resume();
            buffered = true;
        }
    }

    private static class Context<T> implements Runnable, Scope<T> {
        private final ConsumerNotNull<? super Scope<? super T>> generator;
        private T buffer;

        public Context(ConsumerNotNull<? super Scope<? super T>> generator) {
            this.generator = generator;
        }

        @Override
        public void run() {
            generator.accept(this);
        }

        @Override
        public void emit(T value) {
            buffer = value;
            Objects.requireNonNull(Utils.getRunningCoroutineByContext(this)).suspend();
        }
    }
}

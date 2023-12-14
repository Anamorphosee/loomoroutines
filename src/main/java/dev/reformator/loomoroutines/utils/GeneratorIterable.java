package dev.reformator.loomoroutines.utils;

import dev.reformator.loomoroutines.common.internal.utils.ConsumerNotNull;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;

public class GeneratorIterable<T> implements Iterable<T> {
    private final ConsumerNotNull<? super GeneratorIterator.Scope<? super T>> generator;

    public GeneratorIterable(@NotNull ConsumerNotNull<? super GeneratorIterator.Scope<? super T>> generator) {
        Objects.requireNonNull(generator);
        this.generator = generator;
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new GeneratorIterator(generator);
    }
}

package dev.reformator.loomoroutines.utils;

import dev.reformator.loomoroutines.common.ConsumerNotNull;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;

public class GeneratorIterable<T> implements Iterable<T> {
    private final ConsumerNotNull<? super GeneratorIterator.Scope<? super T>> generator;

    protected GeneratorIterable(@NotNull ConsumerNotNull<? super GeneratorIterator.Scope<? super T>> generator) {
        Objects.requireNonNull(generator);
        this.generator = generator;
    }

    public static <T> GeneratorIterable<T> newInstance(@NotNull ConsumerNotNull<? super GeneratorIterator.Scope<? super T>> generator) {
        return new GeneratorIterable<>(generator);
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return GeneratorIterator.newInstance(generator);
    }
}


package dev.reformator.loomoroutines.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

public class GeneratorIterable<T> implements Iterable<T> {
    private final Consumer<? super GeneratorIterator.Scope<? super T>> generator;

    public GeneratorIterable(@NotNull Consumer<? super GeneratorIterator.Scope<? super T>> generator) {
        Objects.requireNonNull(generator);
        this.generator = generator;
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new GeneratorIterator<>(generator);
    }
}

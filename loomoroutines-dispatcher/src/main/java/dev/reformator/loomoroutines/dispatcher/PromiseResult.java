package dev.reformator.loomoroutines.dispatcher;

import org.jetbrains.annotations.NotNull;

public sealed interface PromiseResult<T> permits SucceedPromiseResult, ExceptionalPromiseResult {
    boolean isSucceed();

    @NotNull T get();
}

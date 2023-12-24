package dev.reformator.loomoroutines.dispatcher;

import dev.reformator.loomoroutines.common.internal.utils.CommonUtils;
import org.jetbrains.annotations.NotNull;

public record ExceptionalPromiseResult<T>(@NotNull Throwable exception) implements PromiseResult<T> {
    @Override
    public boolean isSucceed() {
        return false;
    }

    @Override
    public @NotNull T get() {
        throw CommonUtils.throwUnchecked(exception);
    }
}

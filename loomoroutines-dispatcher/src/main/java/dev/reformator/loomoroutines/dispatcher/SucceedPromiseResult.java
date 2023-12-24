package dev.reformator.loomoroutines.dispatcher;

import org.jetbrains.annotations.NotNull;

public record SucceedPromiseResult<T>(@NotNull T value) implements PromiseResult<T> {
    private static final SucceedPromiseResult<Object> nullValue;

    static {
        //noinspection DataFlowIssue
        nullValue = new SucceedPromiseResult<>(null);
    }

    @Override
    public boolean isSucceed() {
        return true;
    }

    @Override
    public @NotNull T get() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull SucceedPromiseResult<T> getNullValue() {
        return (SucceedPromiseResult<T>) nullValue;
    }

    @SuppressWarnings("ConstantValue")
    public static <T> @NotNull SucceedPromiseResult<T> getOf(@NotNull T value) {
        if (value == null) {
            return getNullValue();
        } else {
            return new SucceedPromiseResult<>(value);
        }
    }
}

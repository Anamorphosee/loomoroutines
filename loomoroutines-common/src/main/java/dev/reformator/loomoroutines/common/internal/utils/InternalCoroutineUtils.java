package dev.reformator.loomoroutines.common.internal.utils;

import dev.reformator.loomoroutines.common.RunningCoroutine;
import dev.reformator.loomoroutines.common.internal.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class InternalCoroutineUtils {
    private InternalCoroutineUtils() { }

    private static final Supplier<List<RunningCoroutine<?>>> newListSupplier = ArrayList::new;

    public static void performInCoroutinesScope(@NotNull Runnable action) {
        Registry.runningCoroutinesScoped.performReusable(newListSupplier, action);
    }

    public static @NotNull List<RunningCoroutine<?>> getRunningCoroutines() {
        return Registry.runningCoroutinesScoped.get();
    }
}

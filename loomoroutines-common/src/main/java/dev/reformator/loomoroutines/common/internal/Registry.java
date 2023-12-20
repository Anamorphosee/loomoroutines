package dev.reformator.loomoroutines.common.internal;

import dev.reformator.loomoroutines.common.RunningCoroutine;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Registry {
    private Registry() { }

    public static final @NotNull Scoped<List<RunningCoroutine<?>>> runningCoroutinesScoped = new ThreadLocalScoped<>();

    public static final @NotNull CoroutineFactory coroutineFactory = LoomSuspendedCoroutine.factory;
}

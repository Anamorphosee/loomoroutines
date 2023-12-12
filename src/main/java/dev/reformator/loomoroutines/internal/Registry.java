package dev.reformator.loomoroutines.internal;

public class Registry {
    public static final CoroutineScopeResolver coroutineScopeResolver = new ThreadLocalCoroutineScopeResolver();

    public static final boolean verboseStack = true;
}

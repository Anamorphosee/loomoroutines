package dev.reformator.loomoroutines.impl;

import dev.reformator.loomoroutines.common.ConsumerNotNull;
import dev.reformator.loomoroutines.common.Coroutine;
import dev.reformator.loomoroutines.common.SuspendedCoroutineContext;
import dev.reformator.loomoroutines.internal.Registry;
import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class BaseLoomCoroutine<T extends BaseLoomCoroutine<T>> implements Coroutine<T> {
    private final Continuation continuation;
    private ConsumerNotNull<? super SuspendedCoroutineContext<? extends T>> listener = null;
    private List<Coroutine<?>> coroutinesStack = Collections.singletonList(this);
    private final AtomicReference<Object> cookie;
    private final Object startCookie;

    public BaseLoomCoroutine(@NotNull Runnable body) {
        continuation = new Continuation(new ContinuationScope(getClass().getName()), body);
        var startCookie = Registry.cookieCheckGenerator.getNewCookieCheck();
        cookie = new AtomicReference<>(startCookie);
        this.startCookie = startCookie;
    }

    public final void start() {
        resume(startCookie);
    }

    @Override
    public final void suspend(@NotNull ConsumerNotNull<? super SuspendedCoroutineContext<? extends T>> listener) {
        Objects.requireNonNull(listener);
        if (!Registry.coroutineScopeResolver.isCoroutineInScope(this)) {
            throw new IllegalStateException("suspend() is called out of continuation scope.");
        }
        this.listener = listener;
        Continuation.yield(continuation.getScope());
    }

    protected void resume(Object cookie) {
        var nextCookie = checkCookie(cookie);
        Registry.coroutineScopeResolver.performInCoroutineScope(() -> {
            //noinspection DataFlowIssue
            Registry.coroutineScopeResolver.getCoroutinesScope().addAll(coroutinesStack);
            try {
                continuation.run();
            } finally {
                var coroutines = Registry.coroutineScopeResolver.getCoroutinesScope();
                var index = coroutines.indexOf(this);
                coroutinesStack = new ArrayList<>(coroutines.subList(index, coroutines.size()));
                for (int i = 0; i < coroutinesStack.size(); i++) {
                    coroutines.remove(coroutines.size() - 1);
                }
            }
        });
        if (listener != null) {
            callListener(nextCookie);
        }
    }

    protected void callListener(Object cookie) {
        var nextCookie = checkCookie(cookie);
        var listener = this.listener;
        this.listener = null;
        listener.accept(new SuspendedCoroutineContext<>() {
            @Override
            public @NotNull T getCoroutine() {
                //noinspection unchecked
                return (T) BaseLoomCoroutine.this;
            }

            @Override
            public void resume() {
                BaseLoomCoroutine.this.resume(nextCookie);
            }
        });
    }

    private Object checkCookie(Object cookie) {
        var nextCookie = Registry.cookieCheckGenerator.getNextCookieCheck(cookie);
        if (!this.cookie.compareAndSet(cookie, nextCookie)) {
            throw new IllegalArgumentException("invalid cookie");
        }
        return nextCookie;
    }
}

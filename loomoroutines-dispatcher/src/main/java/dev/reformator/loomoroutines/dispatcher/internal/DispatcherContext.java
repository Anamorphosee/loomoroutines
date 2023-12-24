package dev.reformator.loomoroutines.dispatcher.internal;

import dev.reformator.loomoroutines.dispatcher.Dispatcher;
import dev.reformator.loomoroutines.dispatcher.Promise;
import dev.reformator.loomoroutines.dispatcher.PromiseResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.function.Consumer;

public interface DispatcherContext<T> {
    @NotNull Promise<T> getPromise();

    @Nullable Dispatcher getDispatcher();

    void setDispatcher(@Nullable Dispatcher dispatcher);

    @NotNull DispatcherEvent getLastEvent();

    void setAwaitLastEvent(@NotNull Consumer<? super Runnable> subscribeFunc);

    void setDelayLastEvent(@NotNull Duration duration);

    void setSwitchLastEvent(@NotNull Dispatcher newDispatcher);

    void complete(@NotNull PromiseResult<? extends T> result);
}

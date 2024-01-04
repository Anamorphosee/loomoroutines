package dev.reformator.loomoroutines.dispatcher.internal

import dev.reformator.loomoroutines.common.internal.Consumer
import dev.reformator.loomoroutines.dispatcher.Dispatcher
import dev.reformator.loomoroutines.dispatcher.Promise
import dev.reformator.loomoroutines.dispatcher.PromiseResult
import dev.reformator.loomoroutines.dispatcher.Notifier
import java.time.Duration

internal interface DispatcherContext<T> {
    val promise: Promise<T>

    var dispatcher: Dispatcher?

    val lastEvent: DispatcherEvent

    fun setAwaitLastEvent(callback: Consumer<Notifier>)

    fun setDelayLastEvent(duration: Duration)

    fun setSwitchLastEvent(newDispatcher: Dispatcher)

    fun complete(result: PromiseResult<T>)
}

internal sealed interface DispatcherEvent

internal class AwaitDispatcherEvent(val callback: Consumer<Notifier>): DispatcherEvent

internal class DelayDispatcherEvent(val duration: Duration): DispatcherEvent

internal class SwitchDispatcherEvent(val newDispatcher: Dispatcher): DispatcherEvent

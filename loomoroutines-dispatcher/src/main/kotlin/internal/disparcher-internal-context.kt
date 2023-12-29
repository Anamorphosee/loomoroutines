package dev.reformator.loomoroutines.dispatcher.internal

import dev.reformator.loomoroutines.common.internal.Consumer
import dev.reformator.loomoroutines.dispatcher.Dispatcher
import dev.reformator.loomoroutines.dispatcher.Promise
import dev.reformator.loomoroutines.dispatcher.PromiseResult
import dev.reformator.loomoroutines.dispatcher.Notifier
import java.time.Duration

interface DispatcherContext<T> {
    val promise: Promise<T>

    var dispatcher: Dispatcher?

    val lastEvent: DispatcherEvent

    fun setAwaitLastEvent(callback: Consumer<Notifier>)

    fun setDelayLastEvent(duration: Duration)

    fun setSwitchLastEvent(newDispatcher: Dispatcher)

    fun complete(result: PromiseResult<T>)
}

sealed interface DispatcherEvent

class AwaitDispatcherEvent(val callback: Consumer<Notifier>): DispatcherEvent

class DelayDispatcherEvent(val duration: Duration): DispatcherEvent

class SwitchDispatcherEvent(val newDispatcher: Dispatcher): DispatcherEvent

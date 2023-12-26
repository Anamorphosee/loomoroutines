package dev.reformator.loomoroutines.dispatcher.internal

import dev.reformator.loomoroutines.common.internal.Action
import dev.reformator.loomoroutines.common.internal.Callback
import dev.reformator.loomoroutines.dispatcher.Dispatcher
import dev.reformator.loomoroutines.dispatcher.Promise
import dev.reformator.loomoroutines.dispatcher.PromiseResult
import java.time.Duration

interface DispatcherContext<T> {
    val promise: Promise<T>

    var dispatcher: Dispatcher?

    val lastEvent: DispatcherEvent

    fun setAwaitLastEvent(callback: Callback<Action>)

    fun setDelayLastEvent(duration: Duration)

    fun setSwitchLastEvent(newDispatcher: Dispatcher)

    fun complete(result: PromiseResult<T>)
}

sealed interface DispatcherEvent

class AwaitDispatcherEvent(val callback: Callback<Action>): DispatcherEvent

class DelayDispatcherEvent(val duration: Duration): DispatcherEvent

class SwitchDispatcherEvent(val newDispatcher: Dispatcher): DispatcherEvent

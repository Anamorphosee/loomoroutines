package dev.reformator.loomoroutines.dispatcher.internal

import dev.reformator.loomoroutines.common.CompletedCoroutine
import dev.reformator.loomoroutines.common.SuspendedCoroutine
import dev.reformator.loomoroutines.common.internal.Action
import dev.reformator.loomoroutines.common.internal.invoke
import dev.reformator.loomoroutines.common.internal.kotlinstdlibstub.ObjectRef
import dev.reformator.loomoroutines.dispatcher.Dispatcher
import dev.reformator.loomoroutines.dispatcher.ExceptionalPromiseResult
import dev.reformator.loomoroutines.dispatcher.SucceedPromiseResult

fun <T> Dispatcher.dispatch(coroutine: SuspendedCoroutine<DispatcherContext<T>>, result: ObjectRef<T>) {
    if (canExecuteInCurrentThread()) {
        dispatchInCurrentThread(coroutine, result)
    } else {
        execute { dispatchInCurrentThread(coroutine, result) }
    }
}

private fun <T> Dispatcher.dispatchInCurrentThread(
    coroutine: SuspendedCoroutine<DispatcherContext<T>>,
    result: ObjectRef<T>
) {
    val context = coroutine.coroutineContext
    context.dispatcher = this
    val nextPoint = try {
        coroutine.resume()
    } catch (e: Throwable) {
        context.dispatcher = null
        context.complete(ExceptionalPromiseResult(e))
        return
    }
    context.dispatcher = null
    when (nextPoint) {
        is SuspendedCoroutine<DispatcherContext<T>> -> {
            when (val event = context.lastEvent) {
                is AwaitDispatcherEvent -> event.callback(Action {
                    execute { dispatchInCurrentThread(nextPoint, result) }
                })
                is DelayDispatcherEvent -> scheduleExecute(event.duration) {
                    dispatchInCurrentThread(nextPoint, result)
                }
                is SwitchDispatcherEvent -> event.newDispatcher.dispatch(nextPoint, result)
            }
        }
        is CompletedCoroutine<DispatcherContext<T>> -> context.complete(SucceedPromiseResult(result.element))
    }
}

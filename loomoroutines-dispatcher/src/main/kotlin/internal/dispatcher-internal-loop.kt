package dev.reformator.loomoroutines.dispatcher.internal

import dev.reformator.loomoroutines.common.CompletedCoroutine
import dev.reformator.loomoroutines.common.SuspendedCoroutine
import dev.reformator.loomoroutines.common.internal.Action
import dev.reformator.loomoroutines.common.internal.error
import dev.reformator.loomoroutines.common.internal.getLogger
import dev.reformator.loomoroutines.common.internal.invoke
import dev.reformator.loomoroutines.common.internal.kotlinstdlibstub.Ref
import dev.reformator.loomoroutines.dispatcher.Dispatcher
import dev.reformator.loomoroutines.dispatcher.ExceptionalPromiseResult
import dev.reformator.loomoroutines.dispatcher.SucceedPromiseResult
import java.util.concurrent.atomic.AtomicBoolean

private val log = getLogger()

fun <T> Dispatcher.dispatch(coroutine: SuspendedCoroutine<DispatcherContext<T>>, result: Ref.ObjectRef<T>) {
    if (canExecuteInCurrentThread()) {
        dispatchInCurrentThread(coroutine, result)
    } else {
        execute { dispatchInCurrentThread(coroutine, result) }
    }
}

private fun <T> Dispatcher.dispatchInCurrentThread(
    coroutine: SuspendedCoroutine<DispatcherContext<T>>,
    result: Ref.ObjectRef<T>
) {
    try {
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
                    is AwaitDispatcherEvent -> {
                        val awakened = AtomicBoolean(false)
                        event.callback(Action {
                            if (awakened.compareAndSet(false, true)) {
                                dispatch(nextPoint, result)
                            } else {
                                error("Awakener is already called.")
                            }
                        })
                    }

                    is DelayDispatcherEvent -> scheduleExecute(event.duration) {
                        dispatchInCurrentThread(nextPoint, result)
                    }

                    is SwitchDispatcherEvent -> event.newDispatcher.dispatch(nextPoint, result)
                }
            }
            is CompletedCoroutine<DispatcherContext<T>> -> context.complete(SucceedPromiseResult(result.element))
        }
    } catch (e: Throwable) {
        log.error(e) { "Failed executing dispatcher event." }
        throw e
    }
}

@file:JvmName("DispatcherUtils")

package dev.reformator.loomoroutines.dispatcher

import dev.reformator.loomoroutines.common.createCoroutine
import dev.reformator.loomoroutines.common.getRunningCoroutineContext
import dev.reformator.loomoroutines.common.internal.Consumer
import dev.reformator.loomoroutines.common.internal.Supplier
import dev.reformator.loomoroutines.common.internal.invoke
import dev.reformator.loomoroutines.common.internal.kotlinstdlibstub.Ref
import dev.reformator.loomoroutines.common.trySuspendCoroutine
import dev.reformator.loomoroutines.dispatcher.internal.DispatcherContext
import dev.reformator.loomoroutines.dispatcher.internal.DispatcherContextImpl
import dev.reformator.loomoroutines.dispatcher.internal.ScheduledExecutorServiceDispatcher
import dev.reformator.loomoroutines.dispatcher.internal.dispatch
import java.time.Duration
import java.util.concurrent.ScheduledExecutorService

@Target(AnnotationTarget.FUNCTION)
annotation class CallOnlyInDispatcher

val isInDispatcher: Boolean
    get() = getRunningCoroutineContext<DispatcherContext<*>>() != null

fun interface Notifier {
    operator fun invoke()
}

@CallOnlyInDispatcher
fun await(callback: Consumer<Notifier>) {
    sendDispatcherEvent {
        setAwaitLastEvent(callback)
    }
}

@CallOnlyInDispatcher
fun delay(duration: Duration) {
    sendDispatcherEvent {
        setDelayLastEvent(duration)
    }
}

@CallOnlyInDispatcher
fun <T> doIn(dispatcher: Dispatcher, action: Supplier<T>): T {
    val context = getMandatoryRunningCoroutineDispatcherContext()
    val oldDispatcher = context.dispatcher!!
    if (oldDispatcher === dispatcher) {
        return action()
    } else {
        sendDispatcherEvent {
            setSwitchLastEvent(dispatcher)
        }
        val result = action()
        sendDispatcherEvent {
            setSwitchLastEvent(oldDispatcher)
        }
        return result
    }
}

fun <T> Dispatcher.dispatch(body: Supplier<T>): Promise<T> {
    val context = DispatcherContextImpl<T>()
    val result = Ref.ObjectRef<T>()
    val coroutine = createCoroutine(context) { result.element = body() }
    dispatch(coroutine, result)
    return context.promise
}

fun ScheduledExecutorService.toDispatcher(): CloseableDispatcher =
    ScheduledExecutorServiceDispatcher(this)

@CallOnlyInDispatcher
private inline fun sendDispatcherEvent(crossinline generateEvent: DispatcherContext<*>.() -> Unit) {
    val suspended = trySuspendCoroutine<DispatcherContext<*>> {
        it.generateEvent()
        true
    }
    if (!suspended) {
        throwNotInDispatcher()
    }
}

@CallOnlyInDispatcher
private fun getMandatoryRunningCoroutineDispatcherContext(): DispatcherContext<*> =
    getRunningCoroutineContext<DispatcherContext<*>>() ?: throwNotInDispatcher()

@CallOnlyInDispatcher
private fun throwNotInDispatcher(): Nothing =
    error("Method must be called in a dispatcher coroutine.")

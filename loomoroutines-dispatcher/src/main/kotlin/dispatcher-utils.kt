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

/**
 * Optional annotation to warn that the method must be called only inside a dispatcher coroutine.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class CallOnlyInDispatcher

/**
 * Is current thread running in a dispatcher coroutine.
 */
val isInDispatcher: Boolean
    get() = getRunningCoroutineContext<DispatcherContext<*>>() != null

/**
 * Notifier about completing of an awaiting.
 * @see await
 */
fun interface Notifier {
    /**
     * Notify the dispatcher coroutine that the awaiting has completed.
     * Must be called only once for each [Notifier].
     * @see await
     */
    operator fun invoke()
}

/**
 * Suspend a dispatcher coroutine until a [Notifier] will be invoked.
 * The method suspend a dispatcher coroutine and call [callback] with a [Notifier] which must be invoked to continue the coroutine.
 */
@CallOnlyInDispatcher
fun await(callback: Consumer<Notifier>) {
    sendDispatcherEvent {
        setAwaitLastEvent(callback)
    }
}

/**
 * Suspend a dispatcher coroutine and continue it after a [delay][duration].
 */
@CallOnlyInDispatcher
fun delay(duration: Duration) {
    sendDispatcherEvent {
        setDelayLastEvent(duration)
    }
}

/**
 * Execute an [action] possibly switching a [Dispatcher].
 * @param dispatcher a dispatcher in which [action] will be executed
 * @return [action]'s result
 */
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
        val result = try {
            action()
        } finally {
            sendDispatcherEvent {
                setSwitchLastEvent(oldDispatcher)
            }
        }
        return result
    }
}

/**
 * Create a dispatcher coroutine that will run in [this] and execute [body].
 */
fun <T> Dispatcher.dispatch(body: Supplier<T>): Promise<T> {
    val context = DispatcherContextImpl<T>()
    val result = Ref.ObjectRef<T>()
    val coroutine = createCoroutine(context) { result.element = body() }
    dispatch(coroutine, result)
    return context.promise
}

/**
 * Create a [CloseableDispatcher] that executes actions in [this].
 */
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

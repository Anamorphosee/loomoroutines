@file:JvmName("DispatcherUtils")

package dev.reformator.loomoroutines.dispatcher

import dev.reformator.loomoroutines.common.RunningCoroutine
import dev.reformator.loomoroutines.common.createCoroutine
import dev.reformator.loomoroutines.common.getRunningCoroutineByContextType
import dev.reformator.loomoroutines.common.internal.Action
import dev.reformator.loomoroutines.common.internal.Callback
import dev.reformator.loomoroutines.common.internal.Generator
import dev.reformator.loomoroutines.common.internal.invoke
import dev.reformator.loomoroutines.common.internal.kotlinstdlibstub.Ref
import dev.reformator.loomoroutines.dispatcher.internal.DispatcherContext
import dev.reformator.loomoroutines.dispatcher.internal.DispatcherContextImpl
import dev.reformator.loomoroutines.dispatcher.internal.ScheduledExecutorServiceDispatcher
import dev.reformator.loomoroutines.dispatcher.internal.dispatch
import java.time.Duration
import java.util.concurrent.ScheduledExecutorService

@Target(AnnotationTarget.FUNCTION)
annotation class CallOnlyInDispatcher

val isInDispatcher: Boolean
    get() = getRunningCoroutineByContextType(DispatcherContext::class.java) != null

@CallOnlyInDispatcher
fun await(callback: Callback<Action>) {
    val coroutine = getMandatoryRunningDispatcherCoroutine()
    coroutine.coroutineContext.setAwaitLastEvent(callback)
    coroutine.suspend()
}

@CallOnlyInDispatcher
fun delay(duration: Duration) {
    val coroutine = getMandatoryRunningDispatcherCoroutine()
    coroutine.coroutineContext.setDelayLastEvent(duration)
    coroutine.suspend()
}

@CallOnlyInDispatcher
fun <T> doIn(dispatcher: Dispatcher, generator: Generator<T>): T {
    var coroutine = getMandatoryRunningDispatcherCoroutine()
    val oldDispatcher = coroutine.coroutineContext.dispatcher!!
    if (oldDispatcher === dispatcher) {
        return generator()
    } else {
        coroutine.coroutineContext.setSwitchLastEvent(dispatcher)
        coroutine.suspend()
        val result = generator()
        coroutine = getMandatoryRunningDispatcherCoroutine()
        coroutine.coroutineContext.setSwitchLastEvent(oldDispatcher)
        coroutine.suspend()
        return result
    }
}

fun <T> Dispatcher.dispatch(body: Generator<T>): Promise<T> {
    val context = DispatcherContextImpl<T>()
    val result = Ref.ObjectRef<T>()
    val coroutine = createCoroutine(context, Action { result.element = body() })
    dispatch(coroutine, result)
    return context.promise
}

fun ScheduledExecutorService.toDispatcher(): CloseableDispatcher =
    ScheduledExecutorServiceDispatcher(this)

private fun getMandatoryRunningDispatcherCoroutine(): RunningCoroutine<DispatcherContext<*>> =
    getRunningCoroutineByContextType(DispatcherContext::class.java) ?: error("Method must be called in a dispatcher coroutine.")

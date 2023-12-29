package dev.reformator.loomoroutines.dispatcher

import dev.reformator.loomoroutines.common.internal.Consumer
import dev.reformator.loomoroutines.common.internal.Supplier
import kotlin.time.Duration
import kotlin.time.toJavaDuration

@CallOnlyInDispatcher
@JvmSynthetic
inline fun await(crossinline callback: (Notifier) -> Unit) {
    await(Consumer { callback(it) })
}

@CallOnlyInDispatcher
@JvmSynthetic
fun delay(duration: Duration) {
    delay(duration.toJavaDuration())
}

@CallOnlyInDispatcher
@JvmSynthetic
inline fun <T> doIn(dispatcher: Dispatcher, crossinline action: () -> T): T =
    doIn(dispatcher, Supplier { action() })

@JvmSynthetic
inline fun <T> Dispatcher.dispatch(crossinline body: () -> T): Promise<T> =
    dispatch(Supplier { body() })

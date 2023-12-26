@file:KotlinApi

package dev.reformator.loomoroutines.dispatcher

import dev.reformator.loomoroutines.common.internal.Callback
import dev.reformator.loomoroutines.common.internal.Generator
import dev.reformator.loomoroutines.common.internal.KotlinApi
import dev.reformator.loomoroutines.common.internal.invoke
import kotlin.time.Duration
import kotlin.time.toJavaDuration

@CallOnlyInDispatcher
inline fun await(crossinline callback: (awakener: () -> Unit) -> Unit) {
    await(Callback { awakener ->
        callback { awakener() }
    })
}

@CallOnlyInDispatcher
@Suppress("NOTHING_TO_INLINE")
inline fun delay(duration: Duration) {
    delay(duration.toJavaDuration())
}

@CallOnlyInDispatcher
inline fun <T> doIn(dispatcher: Dispatcher, crossinline generator: () -> T): T =
    doIn(dispatcher, Generator { generator() })

inline fun <T> Dispatcher.dispatch(crossinline body: () -> T): Promise<T> =
    dispatch(Generator { body() })

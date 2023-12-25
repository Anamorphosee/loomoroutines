package dev.reformator.loomoroutines.common.internal

import dev.reformator.loomoroutines.common.Action
import dev.reformator.loomoroutines.common.CompletedCoroutine
import dev.reformator.loomoroutines.common.SuspendedCoroutine

class CompletedCoroutineImpl<out T>(override val coroutineContext: T) : CompletedCoroutine<T>

interface CoroutineFactory {
    fun <T> createCoroutine(context: T, body: Action): SuspendedCoroutine<T>
}
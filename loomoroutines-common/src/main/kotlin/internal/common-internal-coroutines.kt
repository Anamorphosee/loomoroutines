package dev.reformator.loomoroutines.common.internal

import dev.reformator.loomoroutines.common.CompletedCoroutine
import dev.reformator.loomoroutines.common.SuspendedCoroutine

interface RunningCoroutine<out T> {
    val coroutineContext: T

    fun suspend()
}

interface CoroutineFactory {
    fun <T> createCoroutine(context: T, body: Runnable): SuspendedCoroutine<T>
}

class CompletedCoroutineImpl<out T>(override val coroutineContext: T) : CompletedCoroutine<T>

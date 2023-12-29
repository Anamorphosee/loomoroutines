package dev.reformator.loomoroutines.common

sealed interface NotRunningCoroutine<out T> {
    val coroutineContext: T
}

interface CompletedCoroutine<out T>: NotRunningCoroutine<T>

interface SuspendedCoroutine<out T>: NotRunningCoroutine<T> {
    fun resume(): NotRunningCoroutine<T>
}

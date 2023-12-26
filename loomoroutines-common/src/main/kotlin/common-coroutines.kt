package dev.reformator.loomoroutines.common

interface RunningCoroutine<out T> {
    val coroutineContext: T

    fun suspend()
}

sealed interface NotRunningCoroutine<out T> {
    val coroutineContext: T
}

interface CompletedCoroutine<out T>: NotRunningCoroutine<T>

interface SuspendedCoroutine<out T>: NotRunningCoroutine<T> {
    fun resume(): NotRunningCoroutine<T>
}

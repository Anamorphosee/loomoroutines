package dev.reformator.loomoroutines.common

/**
 * Not running coroutine, not started, suspended or completed.
 * Must be one of [CompletedCoroutine] or [SuspendedCoroutine].
 * @param T the coroutine's context type
 */
sealed interface NotRunningCoroutine<out T> {
    /**
     * Coroutine context.
     */
    val coroutineContext: T
}

/**
 * Completed coroutine.
 * @param T the coroutine's context type
 */
interface CompletedCoroutine<out T>: NotRunningCoroutine<T>

/**
 * Suspended coroutine.
 * @param T the coroutine's context type
 */
interface SuspendedCoroutine<out T>: NotRunningCoroutine<T> {
    /**
     * Continue the coroutine in current thread.
     * The coroutine start or continue its execution in the method and returns from it when the coroutine suspends or completes.
     * @return the new coroutine state after resuming execution.
     */
    fun resume(): NotRunningCoroutine<T>
}

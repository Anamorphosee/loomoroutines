package dev.reformator.loomoroutines.dispatcher

import dev.reformator.loomoroutines.common.internal.Consumer
import java.time.Duration

/**
 * A dispatcher, which can execute actions.
 */
interface Dispatcher {
    /**
     * Execute an [action] in the dispatcher.
     */
    fun execute(action: Runnable) {
        scheduleExecute(Duration.ZERO, action)
    }

    /**
     * Execute an [action] in the dispatcher after a [delay].
     * Used for a [dev.reformator.loomoroutines.dispatcher.delay] method.
     */
    fun scheduleExecute(delay: Duration, action: Runnable)

    /**
     * Check, could an action be executed in the current thread instead of calling an [execute] method.
     * Used only for optimization, can always return `false`.
     */
    fun canExecuteInCurrentThread(): Boolean =
        false
}

/**
 * A [Dispatcher] that have to be closed after usage.
 */
interface CloseableDispatcher: Dispatcher, AutoCloseable {
    /**
     * Close the dispatcher.
     */
    override fun close()
}

/**
 * An asynchronous computation result.
 * @param T type of the result
 */
interface Promise<out T> {
    /**
     * State of the computation.
     */
    val state: PromiseState

    /**
     * Get the result of the computation or throw an exception if the computation will fail.
     * Possibly blocking operation. Never blocks if it is called inside a dispatcher coroutine.
     */
    fun join(): T

    /**
     * Add a [callback] that will be called after the computation will complete.
     * [callback] can be called inside the method if the [Promise] have already completed.
     */
    fun subscribe(callback: Consumer<PromiseResult<T>>)
}

/**
 * A state of a [Promise]
 */
enum class PromiseState {
    /**
     * A [Promise] is not completed yet.
     */
    RUNNING,

    /**
     * A [Promise] has completed successfully.
     */
    COMPLETED,

    /**
     * A [Promise] has completed exceptionally.
     */
    EXCEPTIONAL
}

/**
 * A completed [Promise] result.
 * Have to be one of [SucceedPromiseResult] or [ExceptionalPromiseResult].
 * @param T the result's type
 */
sealed interface PromiseResult<out T> {
    /**
     * Get the result or throw an exception if the computation has failed.
     */
    fun get(): T

    /**
     * Has the computation completed successfully.
     */
    val succeed: Boolean
}

/**
 * A successfully completed [Promise] result.
 * @property result the result of the computation
 */
class SucceedPromiseResult<out T>(private val result: T): PromiseResult<T> {
    override fun get(): T =
        result

    override val succeed: Boolean
        get() = true
}

/**
 * A [Promise] result completed exceptionally.
 * @property exception throws by the computation
 */
class ExceptionalPromiseResult(val exception: Throwable): PromiseResult<Nothing> {
    override fun get(): Nothing {
        throw exception
    }

    override val succeed: Boolean
        get() = false
}

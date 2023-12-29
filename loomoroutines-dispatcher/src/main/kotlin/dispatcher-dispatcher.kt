package dev.reformator.loomoroutines.dispatcher

import dev.reformator.loomoroutines.common.internal.Consumer
import java.time.Duration

interface Dispatcher {
    fun execute(action: Runnable) {
        scheduleExecute(Duration.ZERO, action)
    }

    fun scheduleExecute(delay: Duration, action: Runnable)

    fun canExecuteInCurrentThread(): Boolean =
        false
}

interface CloseableDispatcher: Dispatcher, AutoCloseable {
    override fun close()
}

interface Promise<out T> {
    val state: PromiseState

    fun join(): T

    fun subscribe(callback: Consumer<PromiseResult<T>>)
}

enum class PromiseState {
    RUNNING, COMPLETED, EXCEPTIONAL
}

sealed interface PromiseResult<out T> {
    fun get(): T

    val succeed: Boolean
}

class SucceedPromiseResult<out T>(private val result: T): PromiseResult<T> {
    override fun get(): T =
        result

    override val succeed: Boolean
        get() = true
}

class ExceptionalPromiseResult(val exception: Throwable): PromiseResult<Nothing> {
    override fun get(): Nothing {
        throw exception
    }

    override val succeed: Boolean
        get() = false
}

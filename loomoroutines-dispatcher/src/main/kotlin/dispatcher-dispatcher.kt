package dev.reformator.loomoroutines.dispatcher

import dev.reformator.loomoroutines.common.internal.Action
import dev.reformator.loomoroutines.common.internal.Callback
import java.time.Duration

interface Dispatcher {
    fun execute(action: Action) {
        scheduleExecute(Duration.ZERO, action)
    }

    fun scheduleExecute(delay: Duration, action: Action)

    fun canExecuteInCurrentThread(): Boolean =
        false
}

interface Promise<out T> {
    val state: PromiseState

    fun join(): T

    fun subscribe(callback: Callback<PromiseResult<T>>)
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

class ExceptionalPromiseResult<out T>(private val exception: Throwable): PromiseResult<T> {
    override fun get(): T {
        throw exception
    }

    override val succeed: Boolean
        get() = false
}

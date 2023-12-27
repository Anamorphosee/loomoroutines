package dev.reformator.loomoroutines.dispatcher

import dev.reformator.loomoroutines.common.internal.Action
import dev.reformator.loomoroutines.common.internal.Callback
import dev.reformator.loomoroutines.common.internal.invoke
import java.time.Duration

interface Dispatcher {
    companion object {
        @JvmField
        val virtualThreads = object: Dispatcher {
            override fun execute(action: Action) {
                Thread.startVirtualThread(action)
            }

            override fun canExecuteInCurrentThread(): Boolean =
                Thread.currentThread().isVirtual

            override fun scheduleExecute(delay: Duration, action: Action) {
                Thread.startVirtualThread {
                    Thread.sleep(delay)
                    action()
                }
            }
        }
    }

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

class ExceptionalPromiseResult(val exception: Throwable): PromiseResult<Nothing> {
    override fun get(): Nothing {
        throw exception
    }

    override val succeed: Boolean
        get() = false
}

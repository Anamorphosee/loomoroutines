@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package dev.reformator.loomoroutines.common.internal

import dev.reformator.loomoroutines.common.NotRunningCoroutine
import dev.reformator.loomoroutines.common.SuspendedCoroutine
import jdk.internal.vm.Continuation
import jdk.internal.vm.ContinuationScope
import java.util.concurrent.atomic.AtomicBoolean

private val log = getLogger()

private class LoomSuspendedCoroutine<out T>(private val continuation: LoomContinuation<T>) : SuspendedCoroutine<T> {
    private val dirty = AtomicBoolean(false)

    override val coroutineContext: T
        get() = continuation.coroutineContext

    override fun resume(): NotRunningCoroutine<T> {
        if (dirty.compareAndSet(false, true)) {
            assert { continuation.state == LoomContinuationState.SUSPENDED }
            continuation.next = getCurrentContinuation()
            loop {
                continuation.state = LoomContinuationState.RUNNING
                continuation.run()
                when (continuation.state) {
                    LoomContinuationState.SUSPENDED -> {
                        continuation.next = null
                        return LoomSuspendedCoroutine(continuation)
                    }
                    LoomContinuationState.SUSPENSION_INHERITED -> {
                        Continuation.yield(scope)
                    }
                    LoomContinuationState.RUNNING -> {
                        assert { continuation.isDone }
                        return CompletedCoroutineImpl(coroutineContext)
                    }
                }
            }
        } else {
            error("Suspended coroutine has already resumed.")
        }
    }
}

private val scope = ContinuationScope("Loomoroutines")

private class LoomContinuation<out T>(
    val coroutineContext: T,
    body: Runnable
): Continuation(scope, body) {
    var next: LoomContinuation<*>? = null
    var state = LoomContinuationState.SUSPENDED

    fun suspend() {
        var currentContinuation = getCurrentContinuation()!!
        while (currentContinuation !== this) {
            assert { currentContinuation.state == LoomContinuationState.RUNNING }
            currentContinuation.state = LoomContinuationState.SUSPENSION_INHERITED
            currentContinuation = currentContinuation.next!!
        }
        assert { currentContinuation.state == LoomContinuationState.RUNNING }
        currentContinuation.state = LoomContinuationState.SUSPENDED
        yield(scope)
    }
}

enum class LoomContinuationState {
    RUNNING, SUSPENSION_INHERITED, SUSPENDED
}

private fun getCurrentContinuation(): LoomContinuation<*>? =
    Continuation.getCurrentContinuation(scope) as LoomContinuation<*>?

object LoomCoroutineFactory: CoroutineFactory {
    override fun <T> createCoroutine(context: T, body: Runnable): SuspendedCoroutine<T> =
        LoomSuspendedCoroutine(LoomContinuation(context, body))

    override fun forEachRunningCoroutineContext(commandByContext: Function<Any?, SuspensionCommand>) {
        var continuation = getCurrentContinuation()
        while (continuation != null) {
            when (commandByContext(continuation.coroutineContext)) {
                SuspensionCommand.SUSPEND_AND_BREAK -> {
                    continuation.suspend()
                    return
                }
                SuspensionCommand.CONTINUE -> { }
                SuspensionCommand.BREAK -> {
                    return
                }
            }
            continuation = continuation.next
        }
    }
}

@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package dev.reformator.loomoroutines.common.internal

import dev.reformator.loomoroutines.common.NotRunningCoroutine
import dev.reformator.loomoroutines.common.SuspendedCoroutine
import jdk.internal.vm.Continuation
import jdk.internal.vm.ContinuationScope

private val log = getLogger()

private val scope = ContinuationScope("Loomoroutines")

private class UnsafeContinuation<out T>(
    override val coroutineContext: T,
    body: Runnable
): Continuation(scope, body), SuspendedCoroutine<T> {
    var next: UnsafeContinuation<*>? = null
    var state = LoomContinuationState.SUSPENDED

    fun suspend() {
        var currentContinuation = getCurrentContinuation()!!
        while (currentContinuation !== this) {
            currentContinuation.state = LoomContinuationState.SUSPENSION_INHERITED
            currentContinuation = currentContinuation.next!!
        }
        currentContinuation.state = LoomContinuationState.SUSPENDED
        yield(scope)
    }

    override fun resume(): NotRunningCoroutine<T> {
        next = getCurrentContinuation()
        loop {
            state = LoomContinuationState.RUNNING
            run()
            when (state) {
                LoomContinuationState.SUSPENDED -> {
                    next = null
                    return this
                }
                LoomContinuationState.SUSPENSION_INHERITED -> {
                    yield(scope)
                }
                LoomContinuationState.RUNNING -> {
                    return CompletedCoroutineImpl(coroutineContext)
                }
            }
        }
    }
}

private fun getCurrentContinuation(): UnsafeContinuation<*>? =
    Continuation.getCurrentContinuation(scope) as UnsafeContinuation<*>?

internal object UnsafeCoroutineFactory: CoroutineFactory {
    override fun <T> createCoroutine(context: T, body: Runnable): SuspendedCoroutine<T> =
        UnsafeContinuation(context, body)

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

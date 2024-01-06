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

    fun suspend() {
        assert {
            var currentContinuation = getCurrentContinuation()
            while (currentContinuation != null && currentContinuation !== this) {
                currentContinuation = currentContinuation.next
            }
            currentContinuation === this
        }
        next = null
        yield(scope)
    }

    override fun resume(): NotRunningCoroutine<T> {
        assert { next == null }
        next = getCurrentContinuation()
        loop {
            run()
            if (isDone) {
                return CompletedCoroutineImpl(coroutineContext)
            }
            if (next == null) {
                return this
            }
            yield(scope)
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

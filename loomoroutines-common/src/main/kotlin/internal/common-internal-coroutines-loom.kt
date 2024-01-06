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
            assert { continuation.next == null }
            continuation.next = getCurrentContinuation()
            loop {
                continuation.run()
                if (continuation.isDone) {
                    return CompletedCoroutineImpl(coroutineContext)
                }
                if (continuation.next == null) {
                    return LoomSuspendedCoroutine(continuation)
                }
                Continuation.yield(scope)
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
}

private fun getCurrentContinuation(): LoomContinuation<*>? =
    Continuation.getCurrentContinuation(scope) as LoomContinuation<*>?

internal object LoomCoroutineFactory: CoroutineFactory {
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

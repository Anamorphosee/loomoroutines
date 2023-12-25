@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package dev.reformator.loomoroutines.common.internal

import dev.reformator.loomoroutines.common.Action
import dev.reformator.loomoroutines.common.NotRunningCoroutine
import dev.reformator.loomoroutines.common.RunningCoroutine
import dev.reformator.loomoroutines.common.SuspendedCoroutine
import jdk.internal.vm.Continuation
import jdk.internal.vm.ContinuationScope
import java.util.concurrent.atomic.AtomicBoolean

class LoomSuspendedCoroutine<out T>(
    override val coroutineContext: T,
    private val continuation: Continuation,
    private val coroutinesStack: List<RunningCoroutine<*>>
): SuspendedCoroutine<T> {
    private val dirty = AtomicBoolean(false)

    override fun resume(): NotRunningCoroutine<T> {
        if (dirty.compareAndSet(false, true)) {
            var newCoroutineStack: List<RunningCoroutine<*>>? = null
            performInRunningCoroutinesScope {
                runningCoroutinesInternal.addAll(coroutinesStack)
                try {
                    continuation.run()
                } finally {
                    runningCoroutinesInternal.let { runningCoroutines ->
                        val index = runningCoroutines.indexOfFirst {
                            it is LoomRunningCoroutine<*> && it.continuation === continuation
                        }
                        if (index == 0) {
                            newCoroutineStack = runningCoroutines.copyList()
                            runningCoroutines.clear()
                        } else {
                            newCoroutineStack = runningCoroutines.subList(index, runningCoroutines.size).copyList()
                            repeat(runningCoroutines.size - index) {
                                runningCoroutines.removeLast()
                            }
                        }
                    }
                }
            }
            if (continuation.isDone) {
                return CompletedCoroutineImpl(coroutineContext)
            } else {
                return LoomSuspendedCoroutine(coroutineContext, continuation, newCoroutineStack!!)
            }
        } else {
            error("Suspended coroutine has already resumed.")
        }
    }
}

private class LoomRunningCoroutine<out T>(
    override val coroutineContext: T,
    val continuation: Continuation
) : RunningCoroutine<T> {
    override fun suspend() {
        Continuation.yield(continuation.scope)
    }
}

object LoomCoroutineFactory: CoroutineFactory {
    override fun <T> createCoroutine(context: T, body: Action): LoomSuspendedCoroutine<T> {
        val continuation = Continuation(ContinuationScope("Loomoroutines"), body)
        return LoomSuspendedCoroutine(
            coroutineContext = context,
            continuation = continuation,
            coroutinesStack = listOf(LoomRunningCoroutine(context, continuation))
        )
    }
}

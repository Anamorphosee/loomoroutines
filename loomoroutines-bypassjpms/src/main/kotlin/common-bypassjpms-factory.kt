package dev.reformator.loomoroutines.bypassjpms.internal

import dev.reformator.loomoroutines.common.NotRunningCoroutine
import dev.reformator.loomoroutines.common.SuspendedCoroutine
import dev.reformator.loomoroutines.common.internal.*
import dev.reformator.loomoroutines.common.internal.Function
import java.util.concurrent.atomic.AtomicBoolean

private class LoomSuspendedCoroutine<out T>(private val continuation: Any) : SuspendedCoroutine<T> {
    private val dirty = AtomicBoolean(false)

    @Suppress("UNCHECKED_CAST")
    override val coroutineContext: T
        get() = LoomoroutinesBypassJpmsContinuationSupport.getContext(continuation) as T

    override fun resume(): NotRunningCoroutine<T> {
        if (dirty.compareAndSet(false, true)) {
            assert { LoomoroutinesBypassJpmsContinuationSupport.getNext(continuation) == null }
            LoomoroutinesBypassJpmsContinuationSupport.setNext(
                continuation,
                LoomoroutinesBypassJpmsContinuationSupport.getCurrentLoomContinuation()
            )
            loop {
                LoomoroutinesBypassJpmsContinuationSupport.run(continuation)
                if (LoomoroutinesBypassJpmsContinuationSupport.isDone(continuation)) {
                    return CompletedCoroutineImpl(coroutineContext)
                }
                if (LoomoroutinesBypassJpmsContinuationSupport.getNext(continuation) != null) {
                    LoomoroutinesBypassJpmsContinuationSupport.yield()
                } else {
                    return LoomSuspendedCoroutine(continuation)
                }
            }
        } else {
            error("Suspended coroutine has already resumed.")
        }
    }
}

class BypassJpmsCoroutineFactory: CoroutineFactory {
    override val isAvailable: Boolean
        get() = true

    override fun <T> createCoroutine(context: T, body: Runnable): SuspendedCoroutine<T> =
        LoomSuspendedCoroutine(LoomoroutinesBypassJpmsContinuationSupport.create(context, body))

    override fun forEachRunningCoroutineContext(commandByContext: Function<Any?, SuspensionCommand>) {
        var coroutine = LoomoroutinesBypassJpmsContinuationSupport.getCurrentLoomContinuation()
        while (coroutine != null) {
            when (commandByContext(LoomoroutinesBypassJpmsContinuationSupport.getContext(coroutine))) {
                SuspensionCommand.BREAK -> return
                SuspensionCommand.CONTINUE -> coroutine = LoomoroutinesBypassJpmsContinuationSupport.getNext(coroutine)
                SuspensionCommand.SUSPEND_AND_BREAK -> {
                    LoomoroutinesBypassJpmsContinuationSupport.suspend(coroutine)
                    return
                }
            }
        }
    }

    override fun postInit() {
        LoomoroutinesBypassJpmsContinuationSupport.assertionEnabled = LoomoroutinesCommonRegistry.assertionEnabled
        super.postInit()
    }
}

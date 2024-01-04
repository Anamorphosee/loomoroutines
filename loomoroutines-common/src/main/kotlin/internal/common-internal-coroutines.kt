package dev.reformator.loomoroutines.common.internal

import dev.reformator.loomoroutines.common.CompletedCoroutine
import dev.reformator.loomoroutines.common.SuspendedCoroutine

internal enum class SuspensionCommand {
    CONTINUE, BREAK, SUSPEND_AND_BREAK
}

internal interface CoroutineFactory {
    fun <T> createCoroutine(context: T, body: Runnable): SuspendedCoroutine<T>

    fun forEachRunningCoroutineContext(commandByContext: Function<Any?, SuspensionCommand>)
}

internal class CompletedCoroutineImpl<out T>(override val coroutineContext: T) : CompletedCoroutine<T>

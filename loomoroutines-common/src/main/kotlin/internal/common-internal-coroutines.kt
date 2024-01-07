package dev.reformator.loomoroutines.common.internal

import dev.reformator.loomoroutines.common.CompletedCoroutine
import dev.reformator.loomoroutines.common.SuspendedCoroutine

enum class SuspensionCommand {
    CONTINUE, BREAK, SUSPEND_AND_BREAK
}

interface CoroutineFactory {
    val isAvailable: Boolean

    fun <T> createCoroutine(context: T, body: Runnable): SuspendedCoroutine<T>

    fun forEachRunningCoroutineContext(commandByContext: Function<Any?, SuspensionCommand>)

    fun postInit() { }
}

class CompletedCoroutineImpl<out T>(override val coroutineContext: T) : CompletedCoroutine<T>

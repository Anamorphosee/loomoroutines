@file:JvmName("CoroutineUtils")

package dev.reformator.loomoroutines.common

import dev.reformator.loomoroutines.common.internal.*
import java.util.function.Function as JavaFunction

fun <T> createCoroutine(coroutineContext: T, coroutineBody: Runnable): SuspendedCoroutine<T> =
    coroutineFactory.createCoroutine(coroutineContext, coroutineBody)

val runningCoroutinesContexts: List<Any?>
    get() {
        val result = mutableListOf<Any?>()
        forEachRunningCoroutineContext {
            result.add(it)
        }
        return result
    }

val runningCoroutinesNumber: Int
    get() {
        var result = 0
        forEachRunningCoroutineContext { result++ }
        return result
    }

fun getRunningCoroutineContext(predicate: Predicate<Any?>): Any? =
    getRunningCoroutineContextInternal { predicate(it) }

@Suppress("UNCHECKED_CAST")
fun <T: Any> getRunningCoroutineContext(contextType: Class<out T>, predicate: Predicate<T>): T? =
    getRunningCoroutineContextInternal { contextType.isInstance(it) && predicate(it as T) } as T?

fun <T: Any> getRunningCoroutineContext(contextType: Class<out T>): T? =
    getRunningCoroutineContext(contextType, alwaysTruePredicate)

fun trySuspendCoroutine(needSuspensionByContext: Predicate<Any?>): Boolean =
    trySuspendCoroutineInternal { needSuspensionByContext(it) }

@Suppress("UNCHECKED_CAST")
fun <T: Any> trySuspendCoroutine(contextType: Class<out T>, needSuspensionByContext: Predicate<T>): Boolean =
    trySuspendCoroutineInternal { contextType.isInstance(it) && needSuspensionByContext(it as T) }

fun suspendCoroutine(needSuspensionByContext: Predicate<Any?>) {
    suspendCoroutineInternal { trySuspendCoroutine(needSuspensionByContext) }
}

fun <T: Any> suspendCoroutine(contextType: Class<out T>, needSuspensionByContext: Predicate<T>) {
    suspendCoroutineInternal { trySuspendCoroutine(contextType, needSuspensionByContext) }
}

fun <T> NotRunningCoroutine<T>.toSuspended(): SuspendedCoroutine<T>? =
    this as? SuspendedCoroutine<T>

private inline fun getRunningCoroutineContextInternal(crossinline predicate: (Any?) -> Boolean): Any? {
    var result: Any? = null
    coroutineFactory.forEachRunningCoroutineContext {
        if (predicate(it)) {
            result = it
            SuspensionCommand.BREAK
        } else {
            SuspensionCommand.CONTINUE
        }
    }
    return result
}

private inline fun trySuspendCoroutineInternal(crossinline needSuspensionByContext: (Any?) -> Boolean): Boolean {
    var result = false
    coroutineFactory.forEachRunningCoroutineContext {
        if (needSuspensionByContext(it)) {
            result = true
            SuspensionCommand.SUSPEND_AND_BREAK
        } else {
            SuspensionCommand.CONTINUE
        }
    }
    return result
}

private inline fun suspendCoroutineInternal(trySuspend: () -> Boolean) {
    if (!trySuspend()) {
        error("Suspension had failed. Are you in the right coroutine context?")
    }
}

private inline fun forEachRunningCoroutineContext(crossinline body: (Any?) -> Unit) {
    coroutineFactory.forEachRunningCoroutineContext {
        body(it)
        SuspensionCommand.CONTINUE
    }
}

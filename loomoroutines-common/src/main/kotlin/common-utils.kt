@file:JvmName("CoroutineUtils")

package dev.reformator.loomoroutines.common

import dev.reformator.loomoroutines.common.internal.*

/**
 * Create a new not-started coroutine.
 * @param T coroutine's context type
 * @param coroutineContext coroutine's context. Used for identify the coroutine and possibly to send and receive some data between the coroutine's executions
 * @param coroutineBody coroutine's body. The coroutine will execute it
 * @return [SuspendedCoroutine] created coroutine. The coroutine is not started
 */
fun <T> createCoroutine(coroutineContext: T, coroutineBody: Runnable): SuspendedCoroutine<T> =
    LoomoroutinesCommonRegistry.coroutineFactory.createCoroutine(coroutineContext, coroutineBody)

/**
 * Contexts of the all running coroutines in the current thread.
 * Ordered from the most nested coroutine to the outer coroutine.
 */
val runningCoroutinesContexts: List<Any?>
    get() {
        val result = mutableListOf<Any?>()
        forEachRunningCoroutineContext {
            result.add(it)
        }
        return result
    }

/**
 * Number of running coroutines in the current thread.
 */
val runningCoroutinesNumber: Int
    get() {
        var result = 0
        forEachRunningCoroutineContext { result++ }
        return result
    }

/**
 * Find a coroutine's context by the [predicate].
 * [predicate] will be called for every coroutine's context from the most nested coroutine to the outer coroutine until it will return `true`.
 * @return the found coroutine's context or `null` if a context matching [predicate] is not found
 */
fun getRunningCoroutineContext(predicate: Predicate<Any?>): Any? =
    getRunningCoroutineContextInternal { predicate(it) }

/**
 * Find a coroutine's context by its [type][contextType] and [predicate].
 * @return the found coroutine's context or `null` if a context is not found
 */
@Suppress("UNCHECKED_CAST")
fun <T: Any> getRunningCoroutineContext(contextType: Class<out T>, predicate: Predicate<T>): T? =
    getRunningCoroutineContextInternal { contextType.isInstance(it) && predicate(it as T) } as T?

/**
 * Find a coroutine's context by its [type][contextType].
 * @return the found coroutine's context or `null` if a context is not found
 */
fun <T: Any> getRunningCoroutineContext(contextType: Class<out T>): T? =
    getRunningCoroutineContext(contextType, alwaysTruePredicate)

/**
 * Suspend a coroutine, which context matches a [predicate][needSuspensionByContext].
 * @return `true` if the matching coroutine was found. `false` - otherwise (in that case a coroutine's suspension wasn't happened)
 */
fun trySuspendCoroutine(needSuspensionByContext: Predicate<Any?>): Boolean =
    trySuspendCoroutineInternal { needSuspensionByContext(it) }

/**
 * Suspend a coroutine, which context matches a [type][contextType] and a [predicate][needSuspensionByContext].
 * @return `true` if the matching coroutine was found. `false` - otherwise (in that case a coroutine's suspension wasn't happened)
 */
@Suppress("UNCHECKED_CAST")
fun <T: Any> trySuspendCoroutine(contextType: Class<out T>, needSuspensionByContext: Predicate<T>): Boolean =
    trySuspendCoroutineInternal { contextType.isInstance(it) && needSuspensionByContext(it as T) }

/**
 * Same as [trySuspendCoroutine] but throws an exception in case if the coroutine's is not found.
 */
fun suspendCoroutine(needSuspensionByContext: Predicate<Any?>) {
    suspendCoroutineInternal { trySuspendCoroutine(needSuspensionByContext) }
}

/**
 * Same as [trySuspendCoroutine] but throws an exception in case if the coroutine's is not found.
 */
fun <T: Any> suspendCoroutine(contextType: Class<out T>, needSuspensionByContext: Predicate<T>) {
    suspendCoroutineInternal { trySuspendCoroutine(contextType, needSuspensionByContext) }
}

/**
 * @return [SuspendedCoroutine] if [this] is a not-completed coroutine, `null` otherwise
 */
fun <T> NotRunningCoroutine<T>.toSuspended(): SuspendedCoroutine<T>? =
    this as? SuspendedCoroutine<T>

private inline fun getRunningCoroutineContextInternal(crossinline predicate: (Any?) -> Boolean): Any? {
    var result: Any? = null
    LoomoroutinesCommonRegistry.coroutineFactory.forEachRunningCoroutineContext {
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
    LoomoroutinesCommonRegistry.coroutineFactory.forEachRunningCoroutineContext {
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
    LoomoroutinesCommonRegistry.coroutineFactory.forEachRunningCoroutineContext {
        body(it)
        SuspensionCommand.CONTINUE
    }
}

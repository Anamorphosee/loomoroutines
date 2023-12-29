@file:JvmName("CoroutineUtils")

package dev.reformator.loomoroutines.common

import dev.reformator.loomoroutines.common.internal.*
import java.util.*

fun <T> createCoroutine(coroutineContext: T, coroutineBody: Runnable): SuspendedCoroutine<T> =
    coroutineFactory.createCoroutine(coroutineContext, coroutineBody)

val runningCoroutineContexts: List<Any?>
    get() = runningCoroutinesScoped.getOrNull()?.map(RunningCoroutine<*>::coroutineContext) ?: emptyList()

val runningCoroutinesNumber: UInt
    get() = runningCoroutinesScoped.getOrNull()?.size?.toUInt() ?: 0U

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

private inline fun getRunningCoroutineContextInternal(predicate: (Any?) -> Boolean): Any? {
    runningCoroutinesScoped.getOrNull()?.forEachReversed {
        val context = it.coroutineContext
        if (predicate(context)) {
            return context
        }
    }
    return null
}

private inline fun trySuspendCoroutineInternal(needSuspensionByContext: (Any?) -> Boolean): Boolean {
    val coroutine = runningCoroutinesScoped.getOrNull()?.findLast { needSuspensionByContext(it.coroutineContext) }
    return if (coroutine != null) {
        coroutine.suspend()
        true
    } else {
        false
    }
}

private inline fun suspendCoroutineInternal(trySuspend: () -> Boolean) {
    if (!trySuspend()) {
        error("Suspending failed. Are you in the right coroutine context?")
    }
}

@file:JvmName("CoroutineUtils")

package dev.reformator.loomoroutines.common

import dev.reformator.loomoroutines.common.internal.Action
import dev.reformator.loomoroutines.common.internal.Predicate
import dev.reformator.loomoroutines.common.internal.coroutineFactory
import dev.reformator.loomoroutines.common.internal.invoke
import dev.reformator.loomoroutines.common.internal.runningCoroutinesScoped
import java.util.*

val runningCoroutines: List<RunningCoroutine<*>>
    get() = runningCoroutinesScoped.getOrNull()?.let { Collections.unmodifiableList(it) } ?: emptyList()

fun getRunningCoroutineByContextPredicate(predicate: Predicate<Any?>): RunningCoroutine<*>? =
    runningCoroutinesScoped.getOrNull()?.findLast { predicate(it.coroutineContext) }

@Suppress("UNCHECKED_CAST")
fun <T> getRunningCoroutineByContext(coroutineContext: T): RunningCoroutine<T>? =
    runningCoroutinesScoped.getOrNull()?.findLast { it.coroutineContext === coroutineContext } as RunningCoroutine<T>?

@Suppress("UNCHECKED_CAST")
fun <T: Any> getRunningCoroutineByContextPredicate(
    predicate: Predicate<T>,
    type: Class<out T>
): RunningCoroutine<T>? =
    runningCoroutinesScoped.getOrNull()?.findLast {
        val context = it.coroutineContext
        type.isInstance(context) && predicate(context as T)
    } as RunningCoroutine<T>?

@Suppress("UNCHECKED_CAST")
fun <T: Any> getRunningCoroutineByContextType(type: Class<out T>): RunningCoroutine<T>? =
    runningCoroutinesScoped.getOrNull()?.findLast { type.isInstance(it.coroutineContext) } as RunningCoroutine<T>?

fun <T> createCoroutine(coroutineContext: T, action: Action): SuspendedCoroutine<T> =
    coroutineFactory.createCoroutine(coroutineContext, action)

fun <T> NotRunningCoroutine<T>.toSuspended(): SuspendedCoroutine<T>? =
    this as? SuspendedCoroutine<T>

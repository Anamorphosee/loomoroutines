@file:JvmName("CoroutineUtils")

package dev.reformator.loomoroutines.common

import dev.reformator.loomoroutines.common.internal.coroutineFactory
import dev.reformator.loomoroutines.common.internal.runningCoroutinesScoped
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate as JavaPredicate
import java.util.function.Supplier


typealias Generator<T> = Supplier<out T>

typealias Action = Runnable

typealias Callback<T> = Consumer<in T>

typealias Predicate<T> = JavaPredicate<in T>

@JvmSynthetic
@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> Generator<T>.invoke(): T =
    get()

@JvmSynthetic
@Suppress("NOTHING_TO_INLINE")
inline operator fun Action.invoke() {
    run()
}

@JvmSynthetic
@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> Callback<T>.invoke(value: T) {
    accept(value)
}

@JvmSynthetic
@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> Predicate<T>.invoke(value: T): Boolean =
    test(value)

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

@file:JvmName("KotlinCoroutineUtils")

package dev.reformator.loomoroutines.common

import dev.reformator.loomoroutines.common.internal.Action
import dev.reformator.loomoroutines.common.internal.Callback
import dev.reformator.loomoroutines.common.internal.Predicate
import java.util.stream.Stream

inline fun <T> createCoroutine(context: T, crossinline action: () -> Unit): SuspendedCoroutine<T> =
    createCoroutine(context, Action { action() })

@Suppress("UNCHECKED_CAST")
inline fun <reified T> getRunningCoroutineByContextPredicate(crossinline predicate: (T) -> Boolean): RunningCoroutine<T>? =
    getRunningCoroutineByContextPredicate(Predicate { it is T && predicate(it) }) as RunningCoroutine<T>?

inline fun <reified T> getRunningCoroutineByContextType(): RunningCoroutine<T>? =
    getRunningCoroutineByContextPredicate { _: T -> true }

inline fun <T> iterator(crossinline generator: GeneratorScope<T>.() -> Unit): Iterator<T> =
    iterator(Callback { it.generator() })

inline fun <T> iterable(crossinline generator: GeneratorScope<T>.() -> Unit): Iterable<T> =
    iterable(Callback { it.generator() })

inline fun <T> stream(crossinline generator: GeneratorScope<T>.() -> Unit): Stream<out T> =
    stream(Callback { it.generator() })

inline fun <T> sequence(crossinline generator: GeneratorScope<T>.() -> Unit): Sequence<T> =
    Sequence { iterator(generator) }

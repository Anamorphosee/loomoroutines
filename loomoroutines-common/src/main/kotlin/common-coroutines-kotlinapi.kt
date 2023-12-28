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

inline fun <T> loomIterator(crossinline generator: GeneratorScope<T>.() -> Unit): Iterator<T> =
    loomIterator(Callback { it.generator() })

inline fun <T> loomIterable(crossinline generator: GeneratorScope<T>.() -> Unit): Iterable<T> =
    loomIterable(Callback { it.generator() })

inline fun <T> loomStream(crossinline generator: GeneratorScope<T>.() -> Unit): Stream<out T> =
    loomStream(Callback { it.generator() })

inline fun <T> loomSequence(crossinline generator: GeneratorScope<T>.() -> Unit): Sequence<T> =
    Sequence { loomIterator(generator) }

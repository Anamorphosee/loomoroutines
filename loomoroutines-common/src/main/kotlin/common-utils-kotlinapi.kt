@file:JvmName("KotlinCoroutineUtils")

package dev.reformator.loomoroutines.common

import dev.reformator.loomoroutines.common.internal.Consumer
import dev.reformator.loomoroutines.common.internal.Predicate
import java.util.stream.Stream

@JvmSynthetic
inline fun <T> createCoroutine(context: T, crossinline body: () -> Unit): SuspendedCoroutine<T> =
    createCoroutine(context, Runnable { body() })

@JvmSynthetic
inline fun <reified T> getRunningCoroutineContext(crossinline predicate: (T) -> Boolean): T? =
    getRunningCoroutineContext(Predicate { it is T && predicate(it) }) as T?

@JvmSynthetic
inline fun <reified T> getRunningCoroutineContext(): T? =
    getRunningCoroutineContext { _: T -> true }

@JvmSynthetic
inline fun <reified T> trySuspendCoroutine(crossinline needSuspensionByContext: (T) -> Boolean): Boolean =
    trySuspendCoroutine(Predicate { it is T && needSuspensionByContext(it) })

@JvmSynthetic
inline fun <reified T> suspendCoroutine(crossinline needSuspensionByContext: (T) -> Boolean) {
    suspendCoroutine(Predicate { it is T && needSuspensionByContext(it) })
}

@JvmSynthetic
inline fun <T> loomIterator(crossinline generator: GeneratorScope<T>.() -> Unit): Iterator<T> =
    loomIterator(Consumer { it.generator() })

@JvmSynthetic
inline fun <T> loomIterable(crossinline generator: GeneratorScope<T>.() -> Unit): Iterable<T> =
    loomIterable(Consumer { it.generator() })

@JvmSynthetic
inline fun <T> loomStream(crossinline generator: GeneratorScope<T>.() -> Unit): Stream<out T> =
    loomStream(Consumer<GeneratorScope<T>> { it.generator() })

@JvmSynthetic
inline fun <T> loomSequence(crossinline generator: GeneratorScope<T>.() -> Unit): Sequence<T> =
    Sequence { loomIterator(generator) }

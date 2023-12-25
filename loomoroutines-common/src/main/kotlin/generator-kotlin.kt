@file:JvmName("KotlinGeneratorUtils")
@file:KotlinApi

package dev.reformator.loomoroutines.common.kotlin

import dev.reformator.loomoroutines.common.GeneratorScope
import dev.reformator.loomoroutines.common.internal.KotlinApi
import java.util.stream.Stream
import dev.reformator.loomoroutines.common.iterator as commonIterator
import dev.reformator.loomoroutines.common.iterable as commonIterable
import dev.reformator.loomoroutines.common.stream as commonStream

inline fun <T> iterator(crossinline generator: GeneratorScope<T>.() -> Unit): Iterator<T> =
    commonIterator { it.generator() }

inline fun <T> iterable(crossinline generator: GeneratorScope<T>.() -> Unit): Iterable<T> =
    commonIterable { it.generator() }

inline fun <T> stream(crossinline generator: GeneratorScope<T>.() -> Unit): Stream<out T> =
    commonStream { it.generator() }

inline fun <T> sequence(crossinline generator: GeneratorScope<T>.() -> Unit): Sequence<T> =
    Sequence { iterator(generator) }

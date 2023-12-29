@file:JvmName("GeneratorUtils")

package dev.reformator.loomoroutines.common

import dev.reformator.loomoroutines.common.internal.Consumer
import dev.reformator.loomoroutines.common.internal.GeneratorIterator
import java.util.*
import java.util.stream.Stream
import java.util.stream.StreamSupport

interface GeneratorScope<in T> {
    fun emit(value: T)
}

fun <T> loomIterator(generator: Consumer<GeneratorScope<T>>): Iterator<T> =
    GeneratorIterator(generator)

fun <T> loomIterable(generator: Consumer<GeneratorScope<T>>): Iterable<T> =
    Iterable { loomIterator(generator) }

fun <T> loomStream(generator: Consumer<GeneratorScope<T>>): Stream<out T> =
    StreamSupport.stream(Spliterators.spliteratorUnknownSize(loomIterator(generator), Spliterator.ORDERED), false)

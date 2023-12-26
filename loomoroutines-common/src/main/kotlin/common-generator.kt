@file:JvmName("GeneratorUtils")

package dev.reformator.loomoroutines.common

import dev.reformator.loomoroutines.common.internal.Callback
import dev.reformator.loomoroutines.common.internal.GeneratorIterator
import java.util.*
import java.util.stream.Stream
import java.util.stream.StreamSupport

interface GeneratorScope<in T> {
    fun emit(value: T)
}

fun <T> iterator(generator: Callback<GeneratorScope<T>>): Iterator<T> =
    GeneratorIterator(generator)

fun <T> iterable(generator: Callback<GeneratorScope<T>>): Iterable<T> =
    Iterable { iterator(generator) }

fun <T> stream(generator: Callback<GeneratorScope<T>>): Stream<out T> =
    StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(generator), Spliterator.ORDERED), false)

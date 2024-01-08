@file:JvmName("GeneratorUtils")

package dev.reformator.loomoroutines.common

import dev.reformator.loomoroutines.common.internal.Consumer
import dev.reformator.loomoroutines.common.internal.GeneratorIterator
import java.util.*
import java.util.stream.Stream
import java.util.stream.StreamSupport

/**
 * Generator's scope. Used for emitting elements from a generator.
 * @param T the generator's items type
 */
interface GeneratorScope<in T> {
    /**
     * Emit a [value] from the generator.
     */
    fun emit(value: T)
}

/**
 * Create an [Iterator] that iterates over the items emitted from the [generator].
 * @param generator function, that receives [GeneratorScope] and emits the elements to it
 */
fun <T> loomIterator(generator: Consumer<GeneratorScope<T>>): Iterator<T> =
    GeneratorIterator(generator)

/**
 * Create an [Iterable] that iterates over the items emitted from the [generator].
 * @param generator function, that receives [GeneratorScope] and emits the elements to it
 */
fun <T> loomIterable(generator: Consumer<GeneratorScope<T>>): Iterable<T> =
    Iterable { loomIterator(generator) }

/**
 * Create a [Stream] that iterates over the items emitted from the [generator].
 * @param generator function, that receives [GeneratorScope] and emits the elements to it
 */
fun <T> loomStream(generator: Consumer<GeneratorScope<T>>): Stream<out T> =
    StreamSupport.stream(Spliterators.spliteratorUnknownSize(loomIterator(generator), Spliterator.ORDERED), false)

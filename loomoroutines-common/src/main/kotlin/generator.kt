@file:JvmName("GeneratorUtils")

package dev.reformator.loomoroutines.common

import dev.reformator.loomoroutines.common.internal.Callback
import dev.reformator.loomoroutines.common.internal.invoke
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.NoSuchElementException

interface GeneratorScope<in T> {
    fun emit(value: T)
}

fun <T> iterator(generator: Callback<GeneratorScope<T>>): Iterator<T> =
    GeneratorIterator(generator)

fun <T> iterable(generator: Callback<GeneratorScope<T>>): Iterable<T> =
    Iterable { iterator(generator) }

fun <T> stream(generator: Callback<GeneratorScope<T>>): Stream<out T> =
    StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(generator), Spliterator.ORDERED), false)

private class GeneratorIterator<out T>(generator: Callback<GeneratorScope<T>>): Iterator<T> {
    private val state = AtomicReference<GeneratorIteratorState<T>>(NotBufferedGeneratorIteratorState)
    private var coroutine: SuspendedCoroutine<GeneratorIteratorContext<T>>

    init {
        val context = GeneratorIteratorContext<T>()
        coroutine = createCoroutine(context) { generator(context) }
    }

    override fun hasNext(): Boolean =
        buffer()

    override fun next(): T {
        while (true) {
            if (!buffer()) {
                throw NoSuchElementException("Generator has already finished.")
            }
            val state = state.get()
            if (state is BufferedGeneratorIteratorState<T>
                && this.state.compareAndSet(state, NotBufferedGeneratorIteratorState)) {
                return state.buffer
            }
        }
    }

    private fun buffer(): Boolean {
        while (true) {
            if (state.compareAndSet(NotBufferedGeneratorIteratorState, BufferingGeneratorIteratorState)) {
                try {
                    when (val nextCoroutine = coroutine.resume()) {
                        is SuspendedCoroutine<GeneratorIteratorContext<T>> -> {
                            state.set(BufferedGeneratorIteratorState(nextCoroutine.coroutineContext.buffer))
                            coroutine = nextCoroutine
                            return true
                        }
                        is CompletedCoroutine<*> -> {
                            state.set(FinishedGeneratorIteratorState)
                            return false
                        }
                    }
                } catch (e: Throwable) {
                    state.set(ExceptionalGeneratorIteratorState(e))
                    throw e
                }
            }
            when (val state = state.get()) {
                NotBufferedGeneratorIteratorState -> continue
                BufferingGeneratorIteratorState -> error("Generator is already running in an another thread.")
                is BufferedGeneratorIteratorState<*> -> return true
                is ExceptionalGeneratorIteratorState -> throw state.exception
                FinishedGeneratorIteratorState -> return false
            }
        }
    }
}

private sealed interface GeneratorIteratorState<out T>
private data object NotBufferedGeneratorIteratorState: GeneratorIteratorState<Nothing>
private data object BufferingGeneratorIteratorState: GeneratorIteratorState<Nothing>
private class BufferedGeneratorIteratorState<out T>(val buffer: T): GeneratorIteratorState<T>
private class ExceptionalGeneratorIteratorState(val exception: Throwable): GeneratorIteratorState<Nothing>
private data object FinishedGeneratorIteratorState: GeneratorIteratorState<Nothing>

private class GeneratorIteratorContext<T>: GeneratorScope<T> {
    private var _buffer: T? = null

    override fun emit(value: T) {
        val coroutine = getRunningCoroutineByContext(this)!!
        _buffer = value
        coroutine.suspend()
    }

    val buffer: T
        get() {
            @Suppress("UNCHECKED_CAST")
            val result = _buffer as T
            _buffer = null
            return result
        }
}

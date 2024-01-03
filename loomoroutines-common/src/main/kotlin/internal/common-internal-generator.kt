package dev.reformator.loomoroutines.common.internal

import dev.reformator.loomoroutines.common.*

class GeneratorIterator<out T>(generator: Consumer<GeneratorScope<T>>): Iterator<T> {
    private val state = atomic<GeneratorIteratorState<T>>(NotBufferedGeneratorIteratorState)
    private var coroutine: SuspendedCoroutine<GeneratorIteratorContext<T>>

    init {
        val context = GeneratorIteratorContext<T>()
        coroutine = createCoroutine(context) { generator(context) }
    }

    override fun hasNext(): Boolean =
        buffer()

    override fun next(): T {
        loop {
            if (!buffer()) {
                throw NoSuchElementException("Generator has already finished.")
            }
            val state = state.value
            if (state is BufferedGeneratorIteratorState<T>
                && this.state.cas(state, NotBufferedGeneratorIteratorState)) {
                return state.buffer
            }
        }
    }

    private fun buffer(): Boolean {
        loop {
            if (state.cas(NotBufferedGeneratorIteratorState, BufferingGeneratorIteratorState)) {
                try {
                    when (val nextCoroutine = coroutine.resume()) {
                        is SuspendedCoroutine<GeneratorIteratorContext<T>> -> {
                            coroutine = nextCoroutine
                            ifAssert(assertBody = {
                                state.cas(
                                    expectedValue = BufferingGeneratorIteratorState,
                                    newValue = BufferedGeneratorIteratorState(nextCoroutine.coroutineContext.buffer)
                                )
                            }, notAssertBody = {
                                state.value = BufferedGeneratorIteratorState(nextCoroutine.coroutineContext.buffer)
                            })
                            return true
                        }
                        is CompletedCoroutine<*> -> {
                            ifAssert(assertBody = {
                                state.cas(
                                    expectedValue = BufferingGeneratorIteratorState,
                                    newValue = FinishedGeneratorIteratorState
                                )
                            }, notAssertBody = {
                                state.value = FinishedGeneratorIteratorState
                            })
                            return false
                        }
                    }
                } catch (e: Throwable) {
                    ifAssert(assertBody = {
                        state.cas(BufferingGeneratorIteratorState, ExceptionalGeneratorIteratorState(e))
                    }, notAssertBody = {
                        state.value = ExceptionalGeneratorIteratorState(e)
                    })
                    throw e
                }
            }
            when (val state = state.value) {
                NotBufferedGeneratorIteratorState -> return@loop
                BufferingGeneratorIteratorState -> error("Generator is already running in an another thread.")
                is BufferedGeneratorIteratorState<*> -> return true
                is ExceptionalGeneratorIteratorState -> throw RuntimeException(state.exception)
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
        suspendCoroutine { it: Any? ->
            val suspend = it === this
            if (suspend) {
                _buffer = value
            }
            suspend
        }
    }

    val buffer: T
        get() {
            @Suppress("UNCHECKED_CAST")
            val result = _buffer as T
            _buffer = null
            return result
        }
}

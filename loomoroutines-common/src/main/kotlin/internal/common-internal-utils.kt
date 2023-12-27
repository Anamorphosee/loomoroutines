package dev.reformator.loomoroutines.common.internal

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.Consumer
import java.util.function.Predicate as JavaPredicate
import java.util.function.Supplier

fun <T> List<T>.copyList(): List<T> =
    ArrayList(this)

fun getLogger(): Logger {
    val trace = Exception().stackTrace
    val index = trace.indexOfFirst { !it.className.startsWith("java") } + 1
    return LoggerFactory.getLogger(trace[index].className)
}

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

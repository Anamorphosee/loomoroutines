package dev.reformator.loomoroutines.common.internal

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.Predicate as JavaPredicate
import java.util.function.Supplier as JavaSupplier
import java.util.function.Consumer as JavaConsumer

fun <T> Collection<T>.copyList(): MutableList<T> =
    ArrayList(this)

fun getLogger(): Logger {
    val trace = Exception().stackTrace
    val index = trace.indexOfFirst { !it.className.startsWith("java") } + 1
    return LoggerFactory.getLogger(trace[index].className)
}

typealias Supplier<T> = JavaSupplier<out T>

typealias Consumer<T> = JavaConsumer<in T>

typealias Predicate<T> = JavaPredicate<in T>

val alwaysTruePredicate = Predicate<Any?> { true }

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> Supplier<T>.invoke(): T =
    get()

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> Consumer<T>.invoke(value: T) {
    accept(value)
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> Predicate<T>.invoke(value: T): Boolean =
    test(value)

@Suppress("NOTHING_TO_INLINE")
inline operator fun Runnable.invoke() {
    run()
}

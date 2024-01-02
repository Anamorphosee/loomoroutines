package dev.reformator.loomoroutines.common.internal

import dev.reformator.loomoroutines.common.internal.kotlinstdlibstub.Ref
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.Predicate as JavaPredicate
import java.util.function.Supplier as JavaSupplier
import java.util.function.Consumer as JavaConsumer
import java.util.function.Function as JavaFunction

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

typealias Function<INPUT, OUTPUT> = JavaFunction<in INPUT, out OUTPUT>

val alwaysTruePredicate = Predicate<Any?> { true }

typealias Mutable<T> = Ref.ObjectRef<T>

fun <T> mutable(value: T): Mutable<T> {
    val result = Mutable<T>()
    result.element = value
    return result
}

var <T> Mutable<T>.value: T
    get() = element
    set(value) {
        element = value
    }

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

@Suppress("NOTHING_TO_INLINE")
inline operator fun <INPUT, OUTPUT> Function<INPUT, OUTPUT>.invoke(input: INPUT): OUTPUT =
    apply(input)

inline fun loop(body: () -> Unit): Nothing {
    while (true) {
        body()
    }
}

inline fun assert(message: String = "Assertion check failed.", body: () -> Boolean) {
    if (assertionEnabled && !body()) {
        error(message)
    }
}

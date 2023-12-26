package dev.reformator.loomoroutines.common.internal

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.function.Consumer
import java.util.function.Predicate as JavaPredicate
import java.util.function.Supplier

fun <T> List<T>.copyList(): List<T> =
    ArrayList(this)

@Suppress("NOTHING_TO_INLINE")
inline fun getLogger(): Logger = LogManager.getLogger()

@Suppress("NOTHING_TO_INLINE")
inline fun Logger.error(e: Throwable, message: Generator<String>) {
    error(message, e)
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

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.BINARY)
annotation class KotlinApi

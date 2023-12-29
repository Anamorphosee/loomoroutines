package dev.reformator.loomoroutines.common.internal

import org.slf4j.Logger

inline fun <T> List<T>.forEachReversed(action: (T) -> Unit) {
    val iterator = this.listIterator(size)
    while (iterator.hasPrevious()) {
        val element = iterator.previous()
        action(element)
    }
}

inline fun Logger.info(e: Throwable, message: () -> String) {
    if (isInfoEnabled) {
        info(message(), e)
    }
}

inline fun Logger.info(message: () -> String) {
    if (isInfoEnabled) {
        info(message())
    }
}

inline fun Logger.error(e: Throwable, message: () -> String) {
    if (isErrorEnabled) {
        error(message(), e)
    }
}

inline fun Logger.error(message: () -> String) {
    if (isErrorEnabled) {
        error(message())
    }
}
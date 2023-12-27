package dev.reformator.loomoroutines.common.internal

import org.slf4j.Logger

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

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

inline fun Logger.debug(e: Throwable, message: () -> String) {
    if (isDebugEnabled) {
        debug(message(), e)
    }
}

inline fun Logger.debug(message: () -> String) {
    if (isDebugEnabled) {
        debug(message())
    }
}

inline fun Logger.trace(e: Throwable, message: () -> String) {
    if (isTraceEnabled) {
        trace(message(), e)
    }
}

inline fun Logger.trace(message: () -> String) {
    if (isTraceEnabled) {
        trace(message())
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

inline fun ifAssert(message: String = "Assertion check failed.", assertBody: () -> Boolean, notAssertBody: () -> Unit) {
    if (assertionEnabled) {
        if (!assertBody()) {
            error(message)
        }
    } else {
        notAssertBody()
    }
}

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
inline fun <T: AutoCloseable, R> T.use(block: (T) -> R): R {
    var exception: Throwable? = null
    return try {
        block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        try {
            close()
        } catch (e: Throwable) {
            if (exception != null) {
                (e as java.lang.Throwable).addSuppressed(exception)
            }
            throw e
        }
    }
}

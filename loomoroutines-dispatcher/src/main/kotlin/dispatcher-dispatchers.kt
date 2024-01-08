package dev.reformator.loomoroutines.dispatcher

import dev.reformator.loomoroutines.common.internal.error
import dev.reformator.loomoroutines.common.internal.getLogger
import dev.reformator.loomoroutines.common.internal.invoke
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities

private val log = getLogger()

/**
 * A [Dispatcher] that executes actions in virtual threads.
 */
@Suppress("Since15")
object VirtualThreadsDispatcher: Dispatcher {
    private val exceptionHandler = Thread.UncaughtExceptionHandler { _, e ->
        log.error(e) { "Uncaught exception in a virtual thread dispatcher: ${e.message}" }
    }

    override fun execute(action: Runnable) {
        Thread.ofVirtual().uncaughtExceptionHandler(exceptionHandler).start(action)
    }

    override fun scheduleExecute(delay: Duration, action: Runnable) {
        Thread.ofVirtual().uncaughtExceptionHandler(exceptionHandler).start {
            Thread.sleep(delay)
            action()
        }
    }

    override fun canExecuteInCurrentThread(): Boolean =
        Thread.currentThread().isVirtual
}

/**
 * A [Dispatcher] that executes actions in a Swing event dispatcher thread.
 */
object SwingDispatcher: Dispatcher {
    private val scheduledExecutor = Executors.newSingleThreadScheduledExecutor()

    override fun execute(action: Runnable) {
        SwingUtilities.invokeLater {
            try {
                action()
            } catch (e: Throwable) {
                log.error(e) { "Uncaught exception in a UI thread dispatcher: ${e.message}" }
            }
        }
    }

    override fun scheduleExecute(delay: Duration, action: Runnable) {
        scheduledExecutor.schedule({ execute(action) }, delay.toMillis(), TimeUnit.MILLISECONDS)
    }

    override fun canExecuteInCurrentThread(): Boolean =
        SwingUtilities.isEventDispatchThread()
}

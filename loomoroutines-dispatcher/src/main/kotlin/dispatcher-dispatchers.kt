package dev.reformator.loomoroutines.dispatcher

import dev.reformator.loomoroutines.common.internal.invoke
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities

@Suppress("Since15")
object VirtualThreadsDispatcher: Dispatcher {
    override fun execute(action: Runnable) {
        Thread.startVirtualThread(action)
    }

    override fun scheduleExecute(delay: Duration, action: Runnable) {
        Thread.startVirtualThread {
            Thread.sleep(delay)
            action()
        }
    }

    override fun canExecuteInCurrentThread(): Boolean =
        Thread.currentThread().isVirtual
}

object SwingDispatcher: Dispatcher {
    private val scheduledExecutor = Executors.newSingleThreadScheduledExecutor()

    override fun execute(action: Runnable) {
        SwingUtilities.invokeLater(action)
    }

    override fun scheduleExecute(delay: Duration, action: Runnable) {
        scheduledExecutor.schedule({ SwingUtilities.invokeLater(action) }, delay.toMillis(), TimeUnit.MILLISECONDS)
    }

    override fun canExecuteInCurrentThread(): Boolean =
        SwingUtilities.isEventDispatchThread()
}

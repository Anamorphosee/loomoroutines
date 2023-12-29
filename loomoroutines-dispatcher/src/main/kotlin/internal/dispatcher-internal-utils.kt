package dev.reformator.loomoroutines.dispatcher.internal

import dev.reformator.loomoroutines.dispatcher.CloseableDispatcher
import java.time.Duration
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class ScheduledExecutorServiceDispatcher(private val executor: ScheduledExecutorService): CloseableDispatcher {
    override fun execute(action: Runnable) {
        executor.execute(action)
    }

    override fun scheduleExecute(delay: Duration, action: Runnable) {
        executor.schedule(action, delay.toMillis(), TimeUnit.MILLISECONDS)
    }

    override fun close() {
        executor.close()
    }
}

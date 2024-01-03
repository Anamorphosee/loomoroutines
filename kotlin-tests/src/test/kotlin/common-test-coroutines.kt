import dev.reformator.loomoroutines.common.loomIterator
import dev.reformator.loomoroutines.dispatcher.*
import kotlin.coroutines.suspendCoroutine as kotlinSuspendCoroutine
import kotlinx.coroutines.*
import kotlinx.coroutines.delay as kotlinDelay
import kotlinx.coroutines.future.asCompletableFuture
import mu.KotlinLogging
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue

val log = KotlinLogging.logger {  }

class TestCoroutinePerformance {
    @Test
    fun fibonacciIterator() {
        System.setProperty("marker", "fibonacciIterator-kotlin")
        val kotlinIter = iterator {
            generateFibonacci { yield(it) }
        }
        doMeasurement(action = kotlinIter::next)

        System.setProperty("marker", "fibonacciIterator-loomoroutines")
        val loomIter = loomIterator {
            generateFibonacci(::emit)
        }
        doMeasurement(action = loomIter::next)
    }

    @Test
    fun dispatchersSwitch() {
        System.setProperty("marker", "dispatchersSwitch-kotlin")
        Executors.newSingleThreadScheduledExecutor().asCoroutineDispatcher().use { dispatcher1 ->
            Executors.newSingleThreadScheduledExecutor().asCoroutineDispatcher().use { dispatcher2 ->
                val scope = CoroutineScope(dispatcher1)
                doMeasurement {
                    scope.launch {
                        log.debug { "in dispatcher 1" }
                        withContext(dispatcher2) {
                            log.debug { "in dispatcher 2" }
                        }
                        log.debug { "again in dispatcher 1" }
                    }.asCompletableFuture().join()
                }
            }
        }

        System.setProperty("marker", "dispatchersSwitch-loomoroutines")
        Executors.newSingleThreadScheduledExecutor().toDispatcher().use { dispatcher1 ->
            Executors.newSingleThreadScheduledExecutor().toDispatcher().use { dispatcher2 ->
                doMeasurement {
                    dispatcher1.dispatch {
                        log.debug { "in dispatcher 1" }
                        doIn(dispatcher2) {
                            log.debug { "in dispatcher 2" }
                        }
                        log.debug { "again in dispatcher 1" }
                    }.join()
                }
            }
        }
    }

    @Test
    fun dispatcherDelay() {
        System.setProperty("marker", "dispatcherDelay-kotlin")
        Executors.newSingleThreadScheduledExecutor().asCoroutineDispatcher().use { dispatcher ->
            val scope = CoroutineScope(dispatcher)
            doMeasurement {
                scope.launch {
                    log.debug { "before delay" }
                    kotlinDelay(1)
                    log.debug { "after delay" }
                }.asCompletableFuture().join()
            }
        }

        System.setProperty("marker", "dispatcherDelay-loomoroutines")
        Executors.newSingleThreadScheduledExecutor().toDispatcher().use { dispatcher ->
            doMeasurement {
                dispatcher.dispatch {
                    log.debug { "before delay" }
                    delay(1.milliseconds)
                    log.debug { "after delay" }
                }.join()
            }
        }
    }

    @Test
    fun dispatcherAwait() {
        System.setProperty("marker", "dispatcherAwait-kotlin")
        Executors.newSingleThreadScheduledExecutor().asCoroutineDispatcher().use { dispatcher ->
            val scope = CoroutineScope(dispatcher)
            doMeasurement {
                scope.launch {
                    log.debug { "before await" }
                    kotlinSuspendCoroutine<Any?> {
                        log.debug { "before resume" }
                        it.resume(null)
                        log.debug { "after resume" }
                    }
                    log.debug { "after await" }
                }.asCompletableFuture().join()
            }
        }

        System.setProperty("marker", "dispatcherAwait-loomoroutines")
        Executors.newSingleThreadScheduledExecutor().toDispatcher().use { dispatcher ->
            doMeasurement {
                dispatcher.dispatch {
                    log.debug { "before await" }
                    await {
                        log.debug { "before resume" }
                        it()
                        log.debug { "after resume" }
                    }
                    log.debug { "after await" }
                }.join()
            }
        }
    }
}

inline fun generateFibonacci(emit: (Long) -> Unit) {
    var prev = 0L
    var curr = 1L
    while (true) {
        emit(curr)
        val tmp = curr + prev
        prev = curr
        curr = tmp
    }
}

inline fun <T> doMeasurement(
    repeatTimes: Int = 1000,
    logRound: (TimedValue<T>) -> Unit = { log.debug { "got item ${it.value} in ${it.duration}" } },
    logSummary: (average: Duration, median: Duration) -> Unit = { average: Duration, median: Duration ->
        log.info { "median duration is $median" }
        log.info { "average duration is $average" }
    },
    action: () -> T
) {
    val times = mutableListOf<Duration>()
    repeat(repeatTimes) {
        val timedValue = measureTimedValue(action)
        logRound(timedValue)
        times.add(timedValue.duration)
    }
    times.sort()
    logSummary(
        times.reduce(Duration::plus) / repeatTimes,
        (times[times.size / 2] + times[(times.size - 1) / 2]) / 2
    )
}

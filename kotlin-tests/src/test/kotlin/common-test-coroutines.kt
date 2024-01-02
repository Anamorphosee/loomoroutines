import dev.reformator.loomoroutines.common.createCoroutine
import dev.reformator.loomoroutines.common.loomIterator
import dev.reformator.loomoroutines.common.suspendCoroutine
import dev.reformator.loomoroutines.common.toSuspended
import mu.KotlinLogging
import java.util.function.Predicate
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue

val log = KotlinLogging.logger {  }

class TestCoroutines {

    @Test
    fun basicTest() {
        val context = Any()
        createCoroutine(context) {
            //
        }
    }
}

class TestCoroutinePerformance {
    private val alwaysTruePredicate: Predicate<Any?> = Predicate { true }

    @Test
    fun fibonacciIterator() {
        var value: Long = 0
        var coroutine = createCoroutine(null) {
            generateFibonacci {
                value = it
                suspendCoroutine(alwaysTruePredicate)
            }
        }

        val repeatTimes = 10000

        System.setProperty("marker", "kotlin")
        val kotlinIter = iterator {
            generateFibonacci { yield(it) }
        }
        doMeasurement(repeatTimes = repeatTimes, action = kotlinIter::next)

        System.setProperty("marker", "loomoroutines")
        doMeasurement(repeatTimes = repeatTimes, action = {
            coroutine = coroutine.resume().toSuspended()!!
            value
        })
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
    repeatTimes: Int = 500,
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
        (times[times.size / 2] + times[(times.size + 1) / 2]) / 2
    )
}

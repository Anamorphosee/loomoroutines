package dev.reformator.loomoroutines.kotlincoroutines.common.test

import dev.reformator.loomoroutines.common.*
import dev.reformator.loomoroutines.common.internal.getLogger
import dev.reformator.loomoroutines.common.internal.info
import java.math.BigInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

private val log = getLogger()

fun main() {
    testGenerator()
}

private fun testGenerator() {
    loomSequence {
        var prev = BigInteger.ZERO
        var current = BigInteger.ONE
        while (true) {
            emit(current)
            val tmp = prev + current
            prev = current
            current = tmp
        }
    }.take(20).forEach { log.info(it::toString) }
}

private fun testLockingMonitor() {
    val mon = ReentrantLock()
    val cor = AtomicReference<SuspendedCoroutine<*>?>(null)
    thread {
        createCoroutine(Unit) {
            mon.withLock {
                println("thread 1 lock: ${mon.isHeldByCurrentThread}")
                runningCoroutines.last().suspend()
                println("thread 2 lock: ${mon.isHeldByCurrentThread}")
            }
        }.also {
            cor.set(it.resume().toSuspended()!!)
        }
        Thread.sleep(1000)
    }
    thread {
        val cor = run {
            while (true) {
                cor.get()?.let {
                    return@run it
                }
                Thread.sleep(10)
            }
            error("")
        }
        cor.resume()
    }
}


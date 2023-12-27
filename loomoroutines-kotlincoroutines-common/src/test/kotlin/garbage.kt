package dev.reformator.loomoroutines.kotlincoroutines.common.test

import dev.reformator.loomoroutines.common.SuspendedCoroutine
import dev.reformator.loomoroutines.common.createCoroutine
import dev.reformator.loomoroutines.common.runningCoroutines
import dev.reformator.loomoroutines.common.toSuspended
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.resume

fun main() {
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
//    runBlocking {
//        println(suspendCoroutineUninterceptedOrReturn { cont ->
//            printCoroutine(cont)
//        }.length)
//        suspendCoroutine { cont ->
//            println(cont)
//            cont.resume("h")
//        }
//        delay(100)
//        async {
//
//        }.invokeOnCompletion {  }
//        flow {
//           emit(100)
//        }
//    }
}

fun printCoroutine(continuation: Continuation<String>): Any {
    println(continuation)
    thread {
        Thread.sleep(100)
        continuation.resume("foo")
    }
    return COROUTINE_SUSPENDED
}

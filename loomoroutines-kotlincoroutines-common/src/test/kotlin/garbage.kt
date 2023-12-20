package dev.reformator.loomoroutines.kotlincoroutines.common.test

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun main() {
    runBlocking {
        println(suspendCoroutineUninterceptedOrReturn { cont ->
            printCoroutine(cont)
        }.length)
        suspendCoroutine { cont ->
            println(cont)
            cont.resume("h")
        }
        delay(100)
        async {

        }.invokeOnCompletion {  }
        flow {
           emit(100)
        }
    }
}

fun printCoroutine(continuation: Continuation<String>): Any {
    println(continuation)
    thread {
        Thread.sleep(100)
        continuation.resume("foo")
    }
    return COROUTINE_SUSPENDED
}

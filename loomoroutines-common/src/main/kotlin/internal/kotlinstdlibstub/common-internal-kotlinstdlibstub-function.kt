package dev.reformator.loomoroutines.common.internal.kotlinstdlibstub

import java.io.Serializable

interface Function<out R>

interface FunctionBase<out R>: Function<R> {
    val arity: Int
}

abstract class Lambda<out R>(override val arity: Int): FunctionBase<R>, Serializable

interface Function0<out R> : Function<R> {
    operator fun invoke(): R
}

interface Function1<in P1, out R> : Function<R> {
    operator fun invoke(p1: P1): R
}

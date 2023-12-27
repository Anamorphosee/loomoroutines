package dev.reformator.loomoroutines.common.internal.kotlinstdlibstub

import dev.reformator.loomoroutines.common.internal.KotlinApi
import java.io.Serializable

@KotlinApi
interface Function<out R>

@KotlinApi
interface FunctionBase<out R> : Function<R> {
    val arity: Int
}

@KotlinApi
abstract class Lambda<out R>(override val arity: Int) : FunctionBase<R>, Serializable

@KotlinApi
interface Function0<out R> : Function<R> {
    operator fun invoke(): R
}

@KotlinApi
interface Function1<in P1, out R> : Function<R> {
    operator fun invoke(p1: P1): R
}

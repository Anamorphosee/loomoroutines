@file:JvmName("CollectionsKt")

package dev.reformator.loomoroutines.common.internal.kotlinstdlibstub

import java.util.*

fun <T> listOf(element: T): List<T> = Collections.singletonList(element)

fun <T> emptyList(): List<T> = Collections.emptyList()

fun <T> MutableList<T>.removeLast(): T = if (isEmpty()) throw NoSuchElementException("List is empty.") else removeAt(lastIndex)

val <T> List<T>.lastIndex: Int
    get() = this.size - 1

@file:JvmName("CollectionsKt")

package dev.reformator.loomoroutines.common.internal.kotlinstdlibstub

import java.util.*

fun <T> listOf(element: T): List<T> = Collections.singletonList(element)

fun <T> emptyList(): List<T> = Collections.emptyList()

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun <T> MutableList<T>.removeLast(): T = if (isEmpty()) throw NoSuchElementException("List is empty.") else removeAt(lastIndex)

val <T> List<T>.lastIndex: Int
    get() = this.size - 1

fun <T> Iterable<T>.collectionSizeOrDefault(default: Int): Int = if (this is Collection<*>) this.size else default

@file:JvmName("EnumEntriesKt")

package dev.reformator.loomoroutines.common.internal.kotlinstdlibstub

import java.io.Serializable
import java.util.AbstractList

sealed interface EnumEntries<E : Enum<E>> : List<E>

fun <E : Enum<E>> enumEntries(entries: Array<E>): EnumEntries<E> =
    EnumEntriesList(entries)

private class EnumEntriesList<T: Enum<T>>(private val entries: Array<T>): EnumEntries<T>, AbstractList<T>(), Serializable {
    override fun get(index: Int): T =
        entries[index]

    override val size: Int
        get() = entries.size
}

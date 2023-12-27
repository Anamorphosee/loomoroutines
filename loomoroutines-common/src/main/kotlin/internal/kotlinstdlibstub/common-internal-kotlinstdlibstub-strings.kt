@file:JvmName("StringsKt")

package dev.reformator.loomoroutines.common.internal.kotlinstdlibstub

fun String.startsWith(prefix: String, ignoreCase: Boolean = false): Boolean =
    if (!ignoreCase)
        (this as java.lang.String).startsWith(prefix)
    else
       regionMatches(0, prefix, 0, prefix.length, ignoreCase)

fun String.regionMatches(
    thisOffset: Int,
    other: String,
    otherOffset: Int,
    length: Int,
    ignoreCase: Boolean = false
): Boolean =
    if (!ignoreCase)
        (this as java.lang.String).regionMatches(thisOffset, other, otherOffset, length)
    else
        (this as java.lang.String).regionMatches(ignoreCase, thisOffset, other, otherOffset, length)

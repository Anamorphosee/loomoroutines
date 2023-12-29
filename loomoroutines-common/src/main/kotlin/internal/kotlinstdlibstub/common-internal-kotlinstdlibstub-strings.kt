@file:JvmName("StringsKt")

package dev.reformator.loomoroutines.common.internal.kotlinstdlibstub

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
fun String.startsWith(prefix: String, ignoreCase: Boolean = false): Boolean =
    if (!ignoreCase)
        (this as java.lang.String).startsWith(prefix)
    else
       regionMatches(0, prefix, 0, prefix.length, ignoreCase)

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
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

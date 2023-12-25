package dev.reformator.loomoroutines.common.internal

fun <T> List<T>.copyList(): List<T> =
    ArrayList(this)

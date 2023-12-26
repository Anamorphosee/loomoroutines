package dev.reformator.loomoroutines.common.internal

import dev.reformator.loomoroutines.common.internal.invoke

interface Scoped<T> {
    fun performReusable(ifNotSet: Generator<T>, action: Action)

    fun get(): T

    fun getOrNull(): T?
}

class ThreadLocalScoped<T>: Scoped<T> {
    companion object {
        private val noValue = Any()
    }

    private val value = object: ThreadLocal<T>() {
        override fun initialValue(): T {
            @Suppress("UNCHECKED_CAST")
            return noValue as T
        }
    }

    override fun performReusable(ifNotSet: Generator<T>, action: Action) {
        val currentValue = value.get()
        if (currentValue === noValue) {
            try {
                value.set(ifNotSet())
                action()
            } finally {
                value.remove()
            }
        } else {
            action()
        }
    }

    override fun get(): T {
        val currentValue = value.get()
        if (currentValue === noValue) {
            value.remove()
            error("Scoped is not set.")
        }
        return currentValue
    }

    override fun getOrNull(): T? {
        val currentValue = value.get()
        if (currentValue === noValue) {
            value.remove()
            return null
        }
        return currentValue
    }
}

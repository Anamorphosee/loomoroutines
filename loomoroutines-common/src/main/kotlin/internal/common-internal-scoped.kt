package dev.reformator.loomoroutines.common.internal

interface Scoped<T> {
    fun performReusable(ifNotSet: Supplier<T>, body: Runnable)

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

    override fun performReusable(ifNotSet: Supplier<T>, body: Runnable) {
        val currentValue = value.get()
        if (currentValue === noValue) {
            try {
                value.set(ifNotSet())
                body()
            } finally {
                value.remove()
            }
        } else {
            body()
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

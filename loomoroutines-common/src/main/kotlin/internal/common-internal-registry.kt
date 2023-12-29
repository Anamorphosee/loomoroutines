package dev.reformator.loomoroutines.common.internal

val runningCoroutinesScoped: Scoped<MutableList<RunningCoroutine<*>>> = ThreadLocalScoped()

val coroutineFactory: CoroutineFactory = LoomCoroutineFactory

private val newRunningCoroutinesListGenerator = Supplier<MutableList<RunningCoroutine<*>>> { mutableListOf() }

fun performInRunningCoroutinesScope(action: Runnable) {
    runningCoroutinesScoped.performReusable(newRunningCoroutinesListGenerator, action)
}

val runningCoroutinesInternal: MutableList<RunningCoroutine<*>>
    get() = runningCoroutinesScoped.get()

package dev.reformator.loomoroutines.common.internal

import dev.reformator.loomoroutines.common.Action
import dev.reformator.loomoroutines.common.Generator
import dev.reformator.loomoroutines.common.RunningCoroutine

val runningCoroutinesScoped: Scoped<MutableList<RunningCoroutine<*>>> = ThreadLocalScoped()

val coroutineFactory: CoroutineFactory = LoomCoroutineFactory

private val newRunningCoroutinesListGenerator = Generator<MutableList<RunningCoroutine<*>>> { mutableListOf() }

fun performInRunningCoroutinesScope(action: Action) {
    runningCoroutinesScoped.performReusable(newRunningCoroutinesListGenerator, action)
}

val runningCoroutinesInternal: MutableList<RunningCoroutine<*>>
    get() = runningCoroutinesScoped.get()

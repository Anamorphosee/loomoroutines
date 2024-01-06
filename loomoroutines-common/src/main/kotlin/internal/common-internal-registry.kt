package dev.reformator.loomoroutines.common.internal

import java.util.*

internal val coroutineFactory: CoroutineFactory =
    if (LoomCoroutineFactory.isAvailable) {
        LoomCoroutineFactory
    } else run {
        ServiceLoader.load(CoroutineFactory::class.java).forEach { it: CoroutineFactory ->
            if (it.isAvailable) {
                return@run it
            }
        }
        val moduleName = LoomCoroutineFactory::class.java.module.let { it: Module ->
            if (it.isNamed) {
                it.name
            } else {
                "ALL-UNNAMED"
            }
        }
        error("Loomoroutines is not available. Please add '--add-exports java.base/jdk.internal.vm=$moduleName' " +
                "JVM start arguments or add the dependency 'dev.reformator.loomoroutines:loomoroutines-bypassjpms'.")
    }

val assertionEnabled = true

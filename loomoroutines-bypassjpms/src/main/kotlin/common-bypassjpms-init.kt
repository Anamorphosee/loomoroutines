package dev.reformator.loomoroutines.bypassjpms.internal

import io.github.toolfactory.jvm.Driver
import java.lang.invoke.MethodHandles
import dev.reformator.loomoroutines.common.internal.use
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodType
import java.lang.invoke.VarHandle

internal object LoomoroutinesBypassJpmsContinuationSupport {
    private val constructor: MethodHandle
    private val suspend: MethodHandle
    private val getCurrentContinuation: MethodHandle
    private val context: VarHandle
    private val next: VarHandle
    private val run: MethodHandle
    private val isDone: MethodHandle
    private val yield: MethodHandle
    private val _assertionEnabled: VarHandle

    init {
        val loomContinuationClass = loadLoomContinuationClass()
        val lookup = MethodHandles.lookup()
        constructor = lookup.findConstructor(
            loomContinuationClass,
            MethodType.methodType(Void.TYPE, Object::class.java, Runnable::class.java)
        )
        suspend = lookup.findVirtual(loomContinuationClass, "suspend", MethodType.methodType(Void.TYPE))
        getCurrentContinuation = lookup.findStatic(
            loomContinuationClass,
            "getCurrentContinuation",
            MethodType.methodType(loomContinuationClass)
        )
        context = lookup.findVarHandle(loomContinuationClass, "context", Object::class.java)
        next = lookup.findVarHandle(loomContinuationClass, "next", loomContinuationClass)
        run = lookup.findVirtual(loomContinuationClass, "run", MethodType.methodType(Void.TYPE))
        isDone = lookup.findVirtual(loomContinuationClass, "isDone", MethodType.methodType(Boolean::class.javaPrimitiveType))
        yield = lookup.findStatic(loomContinuationClass, "yield", MethodType.methodType(Void.TYPE))
        _assertionEnabled = lookup.findStaticVarHandle(loomContinuationClass, "assertionEnabled", Boolean::class.javaPrimitiveType)
    }

    fun create(context: Any?, body: Runnable): Any =
        constructor.invoke(context, body)

    fun suspend(loomContinuation: Any) {
        suspend.invoke(loomContinuation)
    }

    fun getCurrentLoomContinuation(): Any? =
        getCurrentContinuation.invoke()

    fun getContext(loomContinuation: Any): Any? =
        context.get(loomContinuation)

    fun getNext(loomContinuation: Any): Any? =
        next.get(loomContinuation)

    fun setNext(loomContinuation: Any, value: Any?) {
        next.set(loomContinuation, value)
    }

    fun run(loomContinuation: Any) {
        run.invoke(loomContinuation)
    }

    fun isDone(loomContinuation: Any): Boolean =
        isDone.invoke(loomContinuation) as Boolean

    fun yield() {
        yield.invoke()
    }

    var assertionEnabled: Boolean
        get() = _assertionEnabled.get() as Boolean
        set(value) {
            _assertionEnabled.set(value)
        }
}

private fun loadLoomContinuationClass(): Class<*> {
    val driver: Driver = Driver.Factory.getNew()
    val lookup: MethodHandles.Lookup = driver.getConsulter(Object::class.java)
    return lookup.defineClass(loadResource("dev/reformator/loomoroutines/LoomContinuation.class"))
}

@Suppress("ClassName")
private class _jvmcommonStub

private fun loadResource(name: String): ByteArray? {
    val classLoader = try {
        Thread.currentThread().contextClassLoader
    } catch (_: Throwable) {
        null
    } ?: try {
        _jvmcommonStub::class.java.classLoader
    } catch (_: Throwable) {
        null
    } ?: try {
        ClassLoader.getSystemClassLoader()
    } catch (_: Throwable) {
        null
    }
    val stream = if (classLoader != null) {
        classLoader.getResourceAsStream(name)
    } else {
        ClassLoader.getSystemResourceAsStream(name)
    }
    return stream?.use {
        it.readBytes()
    }
}

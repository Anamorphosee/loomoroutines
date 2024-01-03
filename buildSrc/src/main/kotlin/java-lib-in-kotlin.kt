package dev.reformator.gradle.javalibinkotlin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import org.objectweb.asm.*
import org.objectweb.asm.tree.*

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class JavalibInKotlinPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.extraProperties.set("kotlin.stdlib.default.dependency", "false")
        target.plugins.apply("org.jetbrains.kotlin.jvm")
        target.tasks.named("compileKotlin", KotlinCompile::class.java) {
            doLast {
                destinationDirectory.get().transform()
            }
        }
    }
}

private fun Directory.transform() {
    val aggregatedInfo = getAggregatedInfo()
    visitClasses {
        isTransformationNeeded(aggregatedInfo) && transform()
    }
}

private data class AggregatedInfo(val classToOuterClass: Map<String, String>, val classToSourceFile: Map<String, String>)

private fun Directory.getAggregatedInfo(): AggregatedInfo {
    val classToOuterClass = mutableMapOf<String, String>()
    val classToSourceFile = mutableMapOf<String, String>()
    visitClasses {
        outerClass.let {
            if (it != null) {
                classToOuterClass[name] = it
            }
        }
        sourceFile.let {
            if (it != null) {
                classToSourceFile[name] = it
            }
        }
        false
    }
    return AggregatedInfo(
        classToOuterClass = classToOuterClass,
        classToSourceFile = classToSourceFile
    )
}

private fun ClassNode.isTransformationNeeded(aggregatedInfo: AggregatedInfo): Boolean {
    val outerClassName = generateSequence(name) { aggregatedInfo.classToOuterClass[it] }.last()
    return !aggregatedInfo.classToSourceFile[outerClassName].orEmpty().endsWith("-kotlinapi.kt")
}

private fun Directory.visitClasses(doRewrite: ClassNode.() -> Boolean) {
    asFileTree.visit {
        if (!isDirectory && name.endsWith(".class")) {
            val classNode = open().use {
                val classReader = ClassReader(it)
                val classNode = ClassNode(Opcodes.ASM9)
                classReader.accept(classNode, 0)
                classNode
            }
            if (doRewrite(classNode)) {
                val classWriter = ClassWriter(0)
                classNode.accept(classWriter)
                file.writeBytes(classWriter.toByteArray())
            }
        }
    }
}

private val typeReplacement = mapOf(
    "kotlin/jvm/internal/Intrinsics" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/Intrinsics",
    "kotlin/collections/CollectionsKt" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/CollectionsKt",
    "kotlin/text/StringsKt" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/StringsKt",
    "kotlin/enums/EnumEntriesKt" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/EnumEntriesKt",
    "kotlin/enums/EnumEntries" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/EnumEntries",

    "kotlin/jvm/internal/Ref" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/Ref",
    "kotlin/jvm/internal/Ref\$ObjectRef" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/Ref\$ObjectRef",
    "kotlin/jvm/internal/Ref\$IntRef" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/Ref\$IntRef",

    "kotlin/NoWhenBranchMatchedException" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/KotlinException",
    "kotlin/KotlinNothingValueException" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/KotlinException",

    "kotlin/Unit" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/Unit",
    "kotlin/Function" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/Function",
    "kotlin/jvm/internal/FunctionBase" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/FunctionBase",
    "kotlin/jvm/internal/Lambda" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/Lambda",
    "kotlin/jvm/functions/Function0" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/Function0",
    "kotlin/jvm/functions/Function1" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/Function1"
)

private val interfaceImplementationsToRemove = listOf(
    "kotlin/jvm/internal/markers/KMappedMarker"
)

private fun ClassNode.transform(): Boolean {
    var doTransform = false
    val needTranformationNotifier = { doTransform = true }
    transformField(superName, needTranformationNotifier) { superName = it }
    interfaces?.let { interfaces ->
        for (i in interfaces.indices) {
            transformField(interfaces[i], needTranformationNotifier) { interfaces[i] = it }
        }
    }
    fields?.forEach { field ->
        transformField(field.desc, needTranformationNotifier) { field.desc = it }
        transformField(field.signature, needTranformationNotifier) { field.signature = it }
    }
    innerClasses?.forEach { innerClass ->
        transformField(innerClass.name, needTranformationNotifier) { innerClass.name = it }
        transformField(innerClass.outerName, needTranformationNotifier) { innerClass.outerName = it }
    }
    methods?.forEach { method ->
        method.instructions?.let { instructions ->
            instructions.forEach { instruction ->
                if (instruction is MethodInsnNode) {
                    transformField(instruction.owner, needTranformationNotifier) { instruction.owner = it }
                    transformField(instruction.desc, needTranformationNotifier) { instruction.desc = it }
                } else if (instruction is TypeInsnNode) {
                    transformField(instruction.desc, needTranformationNotifier) { instruction.desc = it }
                } else if (instruction is FieldInsnNode) {
                    transformField(instruction.owner, needTranformationNotifier) { instruction.owner = it}
                    transformField(instruction.desc, needTranformationNotifier) { instruction.desc = it }
                } else if (instruction is InvokeDynamicInsnNode) {
                    transformField(instruction.desc, needTranformationNotifier) { instruction.desc = it }
                    transformHandle(instruction.bsm, needTranformationNotifier) { instruction.bsm = it }
                    for (index in instruction.bsmArgs.indices) {
                        val arg = instruction.bsmArgs[index]
                        if (arg is Type) {
                            transformField(arg.descriptor, needTranformationNotifier) { instruction.bsmArgs[index] = Type.getType(it) }
                        } else if (arg is Handle) {
                            transformHandle(arg, needTranformationNotifier) { instruction.bsmArgs[index] = it }
                        }
                    }
                } else if (instruction is FrameNode) {
                    transformFrame(instruction.local, needTranformationNotifier)
                    transformFrame(instruction.stack, needTranformationNotifier)
                }
            }
        }
        method.localVariables?.forEach { variable ->
            transformField(variable.desc, needTranformationNotifier) { variable.desc = it }
            transformField(variable.signature, needTranformationNotifier) { variable.signature = it }
        }
        transformField(method.desc, needTranformationNotifier) { method.desc = it }
        transformField(method.signature, needTranformationNotifier) { method.signature = it }
    }
    interfaces?.removeAll(interfaceImplementationsToRemove)?.let {
        doTransform = doTransform || it
    }
    return doTransform
}

private fun transformFrame(frame: MutableList<Any>?, notifyNeedTransform: () -> Unit) {
    if (frame != null) {
        for (index in frame.indices) {
            val arg = frame[index]
            if (arg is String) {
                transformField(arg, notifyNeedTransform) { frame[index] = it }
            }
        }
    }
}

private fun transformHandle(handle: Handle?, notifyNeedTransform: () -> Unit, fieldSetter: (Handle) -> Unit) {
    if (handle != null) {
        var owner = handle.owner
        var desc = handle.desc
        var needTransformation = false
        run {
            @Suppress("NAME_SHADOWING") val notifyNeedTransform = {
                needTransformation = true
            }
            transformField(handle.owner, notifyNeedTransform) { owner = it }
            transformField(handle.desc, notifyNeedTransform) { desc = it }
        }
        if (needTransformation) {
            fieldSetter(Handle(handle.tag, owner, handle.name, desc, handle.isInterface))
            notifyNeedTransform()
        }
    }
}

private fun transformField(field: String?, notifyNeedTransform: () -> Unit, fieldSetter: (String) -> Unit) {
    if (field != null) {
        var doTransform = false
        var transformed: String = field
        for ((originalType, replacedType) in typeReplacement) {
            if (transformed.contains(originalType)) {
                transformed = transformed.replace(originalType, replacedType)
                doTransform = true
            }
        }
        if (doTransform) {
            fieldSetter(transformed)
            notifyNeedTransform()
        }
    }
}

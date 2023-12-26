package dev.reformator.javalibinkotlin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import org.objectweb.asm.*
import org.objectweb.asm.tree.*
import java.io.File

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class JavalibInKotlinPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.extraProperties.set("kotlin.stdlib.default.dependency", "false")
        target.extraProperties.set("kotlinVersion", "")
        target.plugins.apply("org.jetbrains.kotlin.jvm")
        target.tasks.withType(KotlinCompile::class.java) {
            doLast {
                target.layout.buildDirectory.get()
                    .dir("classes")
                    .dir("kotlin")
                    .dir("main")
                    .asFileTree.visit {
                        if (!isDirectory) {
                            val file = file
                            if (file.name.endsWith(".class")) {
                                val classNode = getClassNode(file)
                                if (transformClass(classNode)) {
                                    rewriteClassNode(file, classNode)
                                }
                            }
                        }
                    }
            }
        }
    }
}

fun getClassNode(file: File): ClassNode {
    val classReader = ClassReader(file.inputStream())
    val classNode = ClassNode(Opcodes.ASM9)
    classReader.accept(classNode, 0)
    return classNode
}

private val typeReplacement = mapOf(
    "kotlin/jvm/internal/Intrinsics" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/Intrinsics",
    "kotlin/jvm/internal/Ref" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/Ref",
    "kotlin/jvm/internal/Ref\$ObjectRef" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/Ref\$ObjectRef",
    "kotlin/collections/CollectionsKt" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/Intrinsics",
    "kotlin/NoWhenBranchMatchedException" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/KotlinException"
)

private val interfaceImplementationsToRemove = listOf(
    "kotlin/jvm/internal/markers/KMappedMarker"
)

private val kotlinApiAnnotationDesc = "Ldev/reformator/loomoroutines/common/internal/KotlinApi;"

private fun transformClass(node: ClassNode): Boolean {
    if (node.invisibleAnnotations?.find { it.desc == kotlinApiAnnotationDesc } != null) {
        return false
    }
    var doTransform = false
    node.fields?.forEach { field ->
        if (transformField(field.desc) { field.desc = it }) {
            doTransform = true
        }
        if (transformField(field.signature) { field.signature = it }) {
            doTransform = true
        }
    }
    node.innerClasses?.forEach { innerClass ->
        if (transformField(innerClass.name) { innerClass.name = it }) {
            doTransform = true
        }
        if (transformField(innerClass.outerName) { innerClass.outerName = it }) {
            doTransform = true
        }
    }
    node.methods?.forEach { method ->
        method.instructions?.let { instructions ->
            instructions.forEach { instruction ->
                if (instruction is MethodInsnNode) {
                    if (transformField(instruction.owner) { instruction.owner = it }) {
                        doTransform = true
                    }
                    if (transformField(instruction.desc) { instruction.desc = it }) {
                        doTransform = true
                    }
                } else if (instruction is TypeInsnNode) {
                    if (transformField(instruction.desc) { instruction.desc = it }) {
                        doTransform = true
                    }
                } else if (instruction is FieldInsnNode) {
                    if (transformField(instruction.owner) { instruction.owner = it}) {
                        doTransform = true
                    }
                    if (transformField(instruction.desc) { instruction.desc = it }) {
                        doTransform = true
                    }
                } else if (instruction is InvokeDynamicInsnNode) {
                    if (transformField(instruction.desc) { instruction.desc = it }) {
                        doTransform = true
                    }
                    if (transformHandle(instruction.bsm) { instruction.bsm = it }) {
                        doTransform = true
                    }
                    for (index in instruction.bsmArgs.indices) {
                        val arg = instruction.bsmArgs[index]
                        if (arg is Type) {
                            if (transformField(arg.descriptor) { instruction.bsmArgs[index] = Type.getType(it) }) {
                                doTransform = true
                            }
                        } else if (arg is Handle) {
                            if (transformHandle(arg) { instruction.bsmArgs[index] = it }) {
                                doTransform = true
                            }
                        }
                    }
                } else if (instruction is FrameNode) {
                    if (transformFrame(instruction.local)) {
                        doTransform = true
                    }
                    if (transformFrame(instruction.stack)) {
                        doTransform = true
                    }
                }
            }
        }
        method.localVariables?.forEach { variable ->
            if (transformField(variable.desc) { variable.desc = it }) {
                doTransform = true
            }
            if (transformField(variable.signature) { variable.signature = it }) {
                doTransform = true
            }
        }
        if (transformField(method.desc) { method.desc = it }) {
            doTransform = true
        }
        if (transformField(method.signature) { method.signature = it }) {
            doTransform = true
        }
    }
    for (impl in interfaceImplementationsToRemove) {
        if (node.interfaces.remove(impl)) {
            doTransform = true
        }
    }
    return doTransform
}

private fun transformFrame(frame: MutableList<Any>?): Boolean {
    if (frame != null) {
        var doTransform = false
        for (index in frame.indices) {
            val arg = frame[index]
            if (arg is String && transformField(arg) { frame[index] = it }) {
                doTransform = true
            }
        }
        return doTransform
    }
    return false
}

private fun transformHandle(handle: Handle?, fieldSetter: (Handle) -> Unit): Boolean {
    if (handle != null) {
        var owner = handle.owner
        var desc = handle.desc
        if (transformField(handle.owner) { owner = it } || transformField(handle.desc) { desc = it }) {
            fieldSetter(Handle(handle.tag, owner, handle.name, desc, handle.isInterface))
            return true
        }
    }
    return false
}

private fun transformField(field: String?, fieldSetter: (String) -> Unit): Boolean {
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
            return true
        }
    }
    return false
}

private fun rewriteClassNode(file: File, node: ClassNode) {
    val classWriter = ClassWriter(0)
    node.accept(classWriter)
    file.writeBytes(classWriter.toByteArray())
}

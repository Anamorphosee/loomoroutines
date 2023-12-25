import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.objectweb.asm.Handle
import org.objectweb.asm.Type
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FrameNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.TypeInsnNode

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:${properties["kotlinVersion"]}")

    testImplementation("org.junit.jupiter:junit-jupiter:${properties["jupiterVersion"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_19
        freeCompilerArgs.set(freeCompilerArgs.get() + listOf("-include-runtime"))
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType(JavaCompile::class.java) {
    options.compilerArgs.plusAssign(listOf(
        "--add-exports", "java.base/jdk.internal.vm=dev.reformator.loomoroutines.common"
    ))
}

tasks.withType(KotlinCompile::class.java) {
    doLast {
        layout.buildDirectory.get()
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

fun getClassNode(file: File): ClassNode {
    val classReader = ClassReader(file.inputStream())
    val classNode = ClassNode(Opcodes.ASM9)
    classReader.accept(classNode, 0)
    return classNode
}

private val typeReplacement = mapOf(
    "kotlin/jvm/internal/Intrinsics" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/Intrinsics",
    "kotlin/jvm/internal/Ref\$ObjectRef" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/ObjectRef",
    "kotlin/collections/CollectionsKt" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/Intrinsics",
    "kotlin/NoWhenBranchMatchedException" to "dev/reformator/loomoroutines/common/internal/kotlinstdlibstub/KotlinException"
)

private val interfaceImplementationsToRemove = listOf(
    "kotlin/jvm/internal/markers/KMappedMarker"
)

fun transformClass(node: ClassNode): Boolean {
    var doTransform = false
    node.methods.orEmpty().forEach { method ->
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

fun transformFrame(frame: MutableList<Any>?): Boolean {
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

fun transformHandle(handle: Handle?, fieldSetter: (Handle) -> Unit): Boolean {
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

fun transformField(field: String?, fieldSetter: (String) -> Unit): Boolean {
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

fun rewriteClassNode(file: File, node: ClassNode) {
    val classWriter = ClassWriter(0)
    node.accept(classWriter)
    file.writeBytes(classWriter.toByteArray())
}

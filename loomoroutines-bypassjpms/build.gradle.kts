import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*

plugins {
    id("dev.reformator.javalibinkotlin")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:${kotlin.coreLibrariesVersion}")
    implementation(project(":loomoroutines-common"))
    implementation("io.github.toolfactory:jvm-driver:${properties["jvmDriverVersion"]}")
    implementation("org.slf4j:slf4j-api:${properties["slf4jVersion"]}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${properties["jupiterVersion"]}")
    testImplementation("org.apache.commons:commons-lang3:${properties["commonsLang3Version"]}")
    testRuntimeOnly("ch.qos.logback:logback-classic:${properties["logbackVersion"]}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${properties["jupiterVersion"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_19
        freeCompilerArgs.addAll("-Xjvm-default=all")
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("--add-exports", "java.base/jdk.internal.vm=dev.reformator.loomoroutines.bypassjpms"))
    doLast {
        val continuationClassFile = destinationDirectory.get().dir("java").dir("lang_").file("LoomContinuation.class").asFile
        if (continuationClassFile.exists()) {
            val classNode = continuationClassFile.inputStream().use {
                val classReader = ClassReader(it)
                val classNode = ClassNode(Opcodes.ASM9)
                classReader.accept(classNode, 0)
                classNode
            }
            classNode.name = classNode.name.transform
            classNode.fields.forEach { field ->
                field.desc = field.desc.transform
                field.signature = field.signature.transform
            }
            classNode.methods.forEach { method ->
                method.instructions.forEach { instruction ->
                    if (instruction is FieldInsnNode) {
                        instruction.desc = instruction.desc.transform
                        instruction.owner = instruction.owner.transform
                    } else if (instruction is FrameNode) {
                        instruction.local?.forEachIndexed { index, local ->
                            if (local is String) {
                                instruction.local[index] = local.transform
                            }
                        }
                        instruction.stack?.forEachIndexed { index, stack ->
                            if (stack is String) {
                                instruction.stack[index] = stack.transform
                            }
                        }
                    } else if (instruction is MethodInsnNode) {
                        instruction.desc = instruction.desc.transform
                        instruction.owner = instruction.owner.transform
                    } else if (instruction is TypeInsnNode) {
                        instruction.desc = instruction.desc.transform
                    } else if (instruction is InvokeDynamicInsnNode) {
                        instruction.desc = instruction.desc.transform
                        instruction.bsm = instruction.bsm.transform
                        for (index in instruction.bsmArgs.indices) {
                            val arg = instruction.bsmArgs[index]
                            if (arg is Type) {
                                instruction.bsmArgs[index] = arg.transform
                            } else if (arg is Handle) {
                                instruction.bsmArgs[index] = arg.transform
                            }
                        }
                    }
                }
                method.desc = method.desc.transform
                method.signature = method.signature.transform
                method.localVariables?.forEach { variable ->
                    variable.desc = variable.desc.transform
                    variable.signature = variable.signature.transform
                }
            }
            val classWriter = ClassWriter(0)
            classNode.accept(classWriter)
            destinationDirectory.get().dir("dev").dir("reformator").dir("loomoroutines")
                .file("LoomContinuation.class").asFile.writeBytes(classWriter.toByteArray())
            continuationClassFile.delete()
        }
    }
}

private val String?.transform: String?
    get() = this?.replace("java/lang_/LoomContinuation", "java/lang/LoomContinuation")

private val Handle?.transform: Handle?
    get() = this?.run {
        val owner = this.owner.transform
        val desc = this.desc.transform
        if (owner != this.owner || desc != this.desc) {
            Handle(tag, owner, name, desc, isInterface)
        } else {
            this
        }
    }

private val Type?.transform: Type?
    get() = this?.run {
        val desc = descriptor.transform
        if (desc != descriptor) {
            Type.getType(desc)
        } else {
            this
        }
    }

sourceSets {
    main {
        kotlin.destinationDirectory = java.destinationDirectory
    }
}

tasks.test {
    useJUnitPlatform()
}

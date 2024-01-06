import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.distsDirectory
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FrameNode
import org.objectweb.asm.tree.LocalVariableNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TypeInsnNode

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
    options.compilerArgs.addAll(listOf("--add-exports", "java.base/jdk.internal.vm=dev.reformator.loomoroutines.common.bypassjpms"))
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
            destinationDirectory.file("dev.reformator.loomoroutines.LoomContinuation.class")
                .get().asFile.writeBytes(classWriter.toByteArray())
            continuationClassFile.delete()
        }
    }
}

private val String?.transform: String?
    get() = this?.replace("java/lang_/LoomContinuation", "java/lang/LoomContinuation")

sourceSets {
    main {
        kotlin.destinationDirectory = java.destinationDirectory
    }
}

tasks.test {
    useJUnitPlatform()
}

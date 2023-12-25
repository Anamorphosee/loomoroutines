import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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

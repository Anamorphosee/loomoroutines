import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(project(":loomoroutines-common"))
    testImplementation(kotlin("test"))
    testImplementation("io.github.microutils:kotlin-logging-jvm:${properties["kotlinLoggingVersion"]}")
    testRuntimeOnly("ch.qos.logback:logback-classic:${properties["logbackVersion"]}")
    testImplementation("org.junit.jupiter:junit-jupiter:${properties["jupiterVersion"]}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${properties["kotlinxCoroutinesVersion"]}")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--add-exports", "java.base/jdk.internal.vm=dev.reformator.loomoroutines.common")
}


import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":loomoroutines-common"))
    implementation("org.slf4j:slf4j-api:${properties["slf4jVersion"]}")

    testRuntimeOnly("ch.qos.logback:logback-classic:${properties["logbackVersion"]}")
    testImplementation("org.junit.jupiter:junit-jupiter:${properties["jupiterVersion"]}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${properties["kotlinxCoroutinesVersion"]}")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_19
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
}

tasks.test {
    useJUnitPlatform()
}

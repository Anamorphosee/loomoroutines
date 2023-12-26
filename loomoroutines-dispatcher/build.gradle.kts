import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("dev.reformator.javalibinkotlin")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:${kotlin.coreLibrariesVersion}")
    implementation(project(":loomoroutines-common"))
    implementation("org.apache.logging.log4j:log4j-api:${properties["log4jVersion"]}")

    testImplementation("org.junit.jupiter:junit-jupiter:${properties["jupiterVersion"]}")
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

tasks.test {
    useJUnitPlatform()
}

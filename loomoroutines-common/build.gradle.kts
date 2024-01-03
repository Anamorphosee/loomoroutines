import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("dev.reformator.javalibinkotlin")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:${kotlin.coreLibrariesVersion}")
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

sourceSets {
    main {
        kotlin.destinationDirectory = java.destinationDirectory
    }
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--add-exports", "java.base/jdk.internal.vm=dev.reformator.loomoroutines.common")
}


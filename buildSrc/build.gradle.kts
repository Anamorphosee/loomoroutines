import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm-util:${properties["asmVersion"]}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${properties["kotlinVersion"]}")
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

gradlePlugin {
    plugins {
        create("javaLibInKotlin") {
            id = "dev.reformator.javalibinkotlin"
            implementationClass = "dev.reformator.javalibinkotlin.JavalibInKotlinPlugin"
        }
    }
}

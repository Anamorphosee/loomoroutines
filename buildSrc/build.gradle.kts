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
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:${properties["dokkaVersion"]}")
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

gradlePlugin {
    plugins {
        create("javaLibInKotlin") {
            id = "dev.reformator.javalibinkotlin"
            implementationClass = "dev.reformator.gradle.javalibinkotlin.JavalibInKotlinPlugin"
        }
    }
}

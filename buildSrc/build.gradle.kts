plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm-util:${properties["asmVersion"]}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${properties["kotlinVersion"]}")
    gradleKotlinDsl()
}

gradlePlugin {
    plugins {
        create("javaLibInKotlin") {
            id = "dev.reformator.javalibinkotlin"
            implementationClass = "dev.reformator.javalibinkotlin.JavalibInKotlinPlugin"
        }
    }
}

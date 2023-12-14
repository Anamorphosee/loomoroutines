plugins {
    id("java")
    kotlin("jvm")
}

group = "dev.reformator"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:24.1.0")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType(JavaCompile::class.java) {
    options.compilerArgs.plusAssign(listOf("--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED", "--add-modules", "jdk.incubator.concurrent"))
}
kotlin {
    jvmToolchain(20)
}
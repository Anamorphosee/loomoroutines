plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:${properties["jetbrainsAnnotationsVersion"]}")
    implementation(project(":loomoroutines-common"))

    testImplementation("org.junit.jupiter:junit-jupiter:${properties["jupiterVersion"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType(JavaCompile::class.java) {
    options.compilerArgs.plusAssign(listOf("-Xlint:unchecked", "-Xlint:preview"))
}

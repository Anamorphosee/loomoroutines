plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:${properties["jetbrainsAnnotationsVersion"]}")
    implementation(project(":loomoroutines-common"))
    implementation(project(":loomoroutines-dispatcher"))

    testRuntimeOnly("ch.qos.logback:logback-classic:${properties["logbackVersion"]}")
    testImplementation("org.junit.jupiter:junit-jupiter:${properties["jupiterVersion"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
}

tasks.test {
    useJUnitPlatform()
}

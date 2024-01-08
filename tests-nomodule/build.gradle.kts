plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(project(":loomoroutines-common"))
    testImplementation(project(":loomoroutines-dispatcher"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:${properties["jupiterVersion"]}")
    testImplementation("org.apache.commons:commons-lang3:${properties["commonsLang3Version"]}")
    testRuntimeOnly("ch.qos.logback:logback-classic:${properties["logbackVersion"]}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${properties["jupiterVersion"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED")
}

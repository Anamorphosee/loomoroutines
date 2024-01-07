plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(project(":loomoroutines-common"))
    testRuntimeOnly(project(":loomoroutines-bypassjpms"))
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
}

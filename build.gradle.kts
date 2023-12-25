buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.ow2.asm:asm-util:${properties["asmVersion"]}")
    }
}

subprojects {
    group = "dev.reformator.loomoroutines"
    version = "0.0.1-SNAPSHOT"
}

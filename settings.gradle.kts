pluginManagement {
    plugins {
        kotlin("jvm") version "1.9.21"
    }
}

rootProject.name = "loomoroutines"
include(
    "loomoroutines-common",
    "loomoroutines-kotlincoroutines-common"
)

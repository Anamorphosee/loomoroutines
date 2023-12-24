pluginManagement {
    plugins {
        kotlin("jvm") version "1.9.21"
    }
}

rootProject.name = "loomoroutines"
include(
    "loomoroutines-common",
    "loomoroutines-dispatcher",
    "loomoroutines-dispatcher-swing",
    "loomoroutines-kotlincoroutines-common"
)

package dev.reformator.gradle.removejigsawdummy

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.tasks.compile.JavaCompile

class RemoveJigsawDummyPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply("org.gradle.java")
        target.tasks.withType(JavaCompile::class.java) {
            doLast {
                target.layout.buildDirectory.get()
                    .dir("classes")
                    .dir("java")
                    .dir("main")
                    .transform(target)
            }
        }
    }
}

private fun Directory.transform(target: Project) {
    asFileTree.visit {
        if (!isDirectory && name.endsWith("JigsawDummy.class")) {
            file.delete()
        }
    }
}

package org.mybop.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

class JavaMergerPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        if (!project.plugins.any { JavaPlugin::class.java.isInstance(it) }) {
            throw IllegalStateException("The 'java' plugin is required.")
        }
        project.extensions.create("merge", MergePluginExtension::class.java)
        project.tasks.create("merge", JavaMergeTask::class.java)
    }
}

package org.mybop.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction

open class JavaMergeTask : DefaultTask() {

    @TaskAction
    fun merge() {
        val extension = project.extensions.findByType(MergePluginExtension::class.java)!!

        val javaConvention = project.convention.getPlugin(JavaPluginConvention::class.java)

        val builder = FileBuilder(javaConvention.sourceSets
                .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
                .allJava)

        val classCode = builder.processClass(extension.mainClassName)

        builder.write(project.buildDir.toPath().resolve("merge"), classCode)
    }
}

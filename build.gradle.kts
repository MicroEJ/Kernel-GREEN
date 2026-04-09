/*
 * Kotlin
 *
 * Copyright 2023-2026 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */

import com.microej.gradle.tasks.BuildApplicationObjectFileTask
import com.microej.gradle.tasks.BuildExecutableTask
import com.microej.gradle.tasks.BuildVirtualDeviceTask
import com.microej.gradle.tasks.RunOnSimulatorTask
import com.microej.gradle.tasks.LoadVeeTask

plugins {
    alias(libs.plugins.microej.application)
}

group = "com.microej.kernel"
version = "2.3.2"

microej {
    applicationEntryPoint = "com.microej.kernel.green.Main"
    skippedCheckers = "readme"
    produceExecutableDuringBuild()
    produceVirtualDeviceDuringBuild()
    architectureUsage = System.getProperty("com.microej.architecture.usage") ?: "eval" // or "prod"
}

// Fetch the VEE Port as a module dependency (default)
// Update the following variables to set your VEE Port information
val defaultVeePortGroup: String = "com.nxp.vee.mimxrt1170_mapps"
val defaultVeePortModule: String = "vee-port"
val defaultVeePortVersion: String = "3.1.0"


// Allows to override the VEE Port information with command line arguments
val veePortGroup: String = providers.systemProperty("veeport.group").getOrElse(defaultVeePortGroup)
val veePortModule: String = providers.systemProperty("veeport.module").getOrElse(defaultVeePortModule)
val veePortVersion: String = providers.systemProperty("veeport.version").getOrElse(defaultVeePortVersion)


// Fetch the VEE Port by specifying the local sources
// Update the defaultLocalVEEPortPath to your VEE Port local source folder
// Setting a value to defaultLocalVEEPortPath will force Gradle to use the local VEEPort path for building.
// If this variable is not an empty String, Gradle will use it to fetch the VEE Port.
val defaultLocalVEEPortPath: String = ""
val localVEEPortPath: String = providers.systemProperty("local.veeport.path").getOrElse(defaultLocalVEEPortPath)


dependencies {
    // Foundation libraries.
    implementation(libs.ej.edc)
    implementation(libs.ej.bon)
    implementation(libs.ej.kf)
    implementation(libs.ej.fs)
    implementation(libs.ej.net)
    implementation(libs.ej.ssl)
    implementation(libs.ej.microui)
    implementation(libs.ej.drawing)

    // Libraries.
    implementation(libs.ej.ui.mwt)
    implementation(libs.ej.eclasspath.logging)
    implementation(libs.ej.eclasspath.formatter)
    implementation(libs.ej.runtime.basictool)
    implementation(libs.ej.runtime.service)
    implementation(libs.ej.runtime.property)
    implementation(libs.ej.runtime.storage.fs)
    implementation(libs.ej.util.progress)
    implementation(libs.library.kf.connectivity)
    implementation(libs.library.kf.util)

    // Mandatory Kernel API files.
    implementation(libs.kernel.api.edc)
    implementation(libs.kernel.api.bon)
    implementation(libs.kernel.api.kf)
    implementation(libs.kernel.api.net)
    implementation(libs.kernel.api.ssl)
    implementation(libs.kernel.api.microui)
    implementation(libs.kernel.api.drawing)
    implementation(libs.kernel.api.mwt)
    implementation(libs.kernel.api.logging)
    implementation(libs.kernel.api.basictool)
    implementation(libs.kernel.api.service)
    implementation(libs.kernel.api.property)
    implementation(libs.kernel.api.storage)
    implementation(libs.kernel.api.connectivity)
    implementation(libs.kernel.api.trace)

    // NTP dependency.
    implementation(libs.ej.iot.net.util)

    // AppConnect libraries.
    implementation(libs.library.appconnect.http)
    implementation(libs.library.appconnect.kf)

    // MicroEJ SDK extensions.
    microejTool(libs.tool.application.repository)

    // AspectJ dependency.
    microejTool(libs.tool.weaver.aspectj)

    // VEE Port dependency
    // If any local VEE Port path is provided, look for the local source, otherwise fetch the VEE Port as module dependency
    if (localVEEPortPath.isEmpty()) {
        microejVee("$veePortGroup:$veePortModule:$veePortVersion")
    } else {
        microejVee(files(localVEEPortPath))
    }

}

// Workaround to add the foundation AspectJ as a dependency without adding it in the VEE port.
val loadVee = tasks.withType(LoadVeeTask::class).named("loadVee")
loadVee.configure {
    inputs.files(configurations.getByName("virtualDeviceToolClasspath"))

    doLast {
        configurations.getByName("virtualDeviceToolClasspath").resolvedConfiguration.files.filter { it.name.contains("aspectj") }
            .forEach {
            // Extract AspectJ in a temporary folder.
            project.copy {
                from(zipTree(it))
                into(project.layout.buildDirectory.dir("tmp/aspectJ"))
            }
            // Copy the "content" folder in the "build/vee" folder.
            project.copy {
                from(project.layout.buildDirectory.dir("tmp/aspectJ/content"))
                into(loadedVeeDir)
            }
        }
    }
}

allprojects {
    tasks.withType<BuildExecutableTask> {
        setOnlyIf {
            // Avoid to build the kernel again during a buildFeature
            !(project.hasProperty("feature.skip.build.kernel") && (findProperty("feature.skip.build.kernel") == "true") && gradle.taskGraph.allTasks.stream()
                .map { it.name }.anyMatch { it.equals("buildFeature") })
        }
    }
}

val aspectSources = project.file("src/main/aspectj")
val aspectSkip = "false"

tasks.withType<BuildApplicationObjectFileTask> {
    systemProperties.put("microej.option.microej.aspectj.skip", aspectSkip)
    systemProperties.put("microej.option.microej.aspects.src", aspectSources.absolutePath)
}

tasks.withType<BuildVirtualDeviceTask> {
    systemProperties.put("microej.option.microej.aspectj.skip", aspectSkip)
    systemProperties.put("microej.option.microej.aspects.src", aspectSources.absolutePath)
}

tasks.withType<RunOnSimulatorTask> {
    systemProperties.put("microej.option.microej.aspectj.skip", aspectSkip)
    systemProperties.put("microej.option.microej.aspects.src", aspectSources.absolutePath)
}

// Skip building javadoc (as a workaround).
val skipJavaDoc = providers.systemProperty("skip.java.doc").map { it.toBoolean() }.orElse(true)
tasks.named<Javadoc>("javadoc") {
    source = sourceSets.getByName("main").allJava
    classpath = sourceSets.getByName("main").compileClasspath
    onlyIf {
        !(skipJavaDoc.get())
    }
}
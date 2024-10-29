/*
 * Kotlin
 *
 * Copyright 2023-2024 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */

import org.gradle.internal.os.OperatingSystem

plugins {
    id("com.microej.gradle.application") version "0.19.0"
}

group = "com.microej.kernel"
version = "2.0.0"

repositories {
    mavenCentral()
}

microej {
    applicationEntryPoint = "com.microej.kernel.green.Main"
    produceExecutableDuringBuild()
    skippedCheckers = "readme"
}

// Fetch the VEE Port as a module dependency (default)
// Update the following variables to set your VEE Port information
val defaultVeePortGroup: String = "com.nxp.vee.mimxrt1170"
val defaultVeePortModule: String = "evk_platform_mapps"
val defaultVeePortVersion: String = "2.2.0"


// Allows to override the VEE Port information with command line arguments
val veePortGroup: String = providers.systemProperty("veeport.group").getOrElse(defaultVeePortGroup)
val veePortModule: String = providers.systemProperty("veeport.module").getOrElse(defaultVeePortModule)
val veePortVersion: String = providers.systemProperty("veeport.version").getOrElse(defaultVeePortVersion)


// Fetch the VEE Port by specifying the local sources
// Update the defaultLocalVEEPortPath to your VEE Port local source folder
// Setting a value to defaultLocalVEEPortPath will force Gradle to use the local VEEPort path for building.
val defaultLocalVEEPortPath: String = ""  // If this variable is not an empty String, Gradle will use it to fetch the VEE Port.
val localVEEPortPath: String = providers.systemProperty("local.veeport.path").getOrElse(defaultLocalVEEPortPath)


dependencies {
    // Foundation libraries
    implementation("ej.api:edc:1.3.5")
    implementation("ej.api:bon:1.4.1")
    implementation("ej.api:kf:1.7.0")
    implementation("ej.api:net:1.1.2")
    implementation("ej.api:ssl:2.2.1")
    implementation("ej.api:microui:3.1.1")
    implementation("ej.api:drawing:1.0.4")
    implementation("ej.api:fs:2.1.1")

    // Add on libraries
    implementation("ej.library.ui:mwt:3.3.0")
    implementation("ej.library.eclasspath:logging:1.2.1")
    implementation("ej.library.runtime:basictool:1.6.0")
    implementation("com.microej.library.kf:connectivity:2.1.0")
    implementation("com.microej.library.util:kf-util:2.8.0")
    implementation("ej.library.runtime:service:1.2.0")
    implementation("ej.library.runtime:property:4.2.0")
    implementation("ej.library.runtime:storage-fs:1.2.0")

    // Mandatory Kernel API files
    implementation("com.microej.kernelapi:edc:1.1.0")
    implementation("com.microej.kernelapi:bon:1.3.0")
    implementation("com.microej.kernelapi:kf:2.0.3")
    implementation("com.microej.kernelapi:net:1.2.2")
    implementation("com.microej.kernelapi:ssl:1.1.0")
    implementation("com.microej.kernelapi:basictool:1.3.0")
    implementation("com.microej.kernelapi:storage:1.1.0")
    implementation("com.microej.kernelapi:microui:3.0.0")
    implementation("com.microej.kernelapi:mwt:2.2.0")
    implementation("com.microej.kernelapi:drawing:1.1.0")
    implementation("com.microej.kernelapi:logging:1.0.0")
    implementation("com.microej.kernelapi:connectivity:1.3.0")
    implementation("com.microej.kernelapi:trace:1.0.0")
    implementation("com.microej.kernelapi:service:1.3.1")
    implementation("com.microej.kernelapi:property:1.3.1")

    // Ntp dependency
    implementation("ej.library.iot:net-util:1.2.0")

    // Command Server socket dependencies
    implementation("com.microej.library.kernel:localdeploy-impl:1.0.0")
    implementation("ej.library.iot:rcommand:2.10.1")
    implementation("ej.library.iot:rcommand-serversocket:2.5.0")

    // MicroEJ SDK Extensions
    microejTool("com.microej.tool.kernel:localdeploy-extension:2.0.0")
    microejTool("com.is2t.tools:application-repository-extension:1.0.3")

    // VEE Port dependency
    // If any local VEE Port path is provided, look for the local source, otherwise fetch the VEE Port as module dependency
    if(localVEEPortPath.isEmpty()){
        microejVee("$veePortGroup:$veePortModule:$veePortVersion")
    }else{
        microejVee(files(localVEEPortPath))
    }

}

// Workaround for mimxrt1170 VEEPort on linux OS
val myTask = tasks.register("mimxrt1170LinuxWorkAround") {
    dependsOn("loadVee")

    doLast {
        val veePortScriptDir = file("build/bsp/projects/nxpvee-ui/armgcc")
        if (OperatingSystem.current().isLinux && veePortScriptDir.exists()) {
            exec {
                commandLine("chmod", "-R", "755", veePortScriptDir.absolutePath)
            }
        }
    }

}

project.getTasks().getByName("buildExecutable").dependsOn(myTask);


// Copyright 2021-2025 MicroEJ Corp. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be found with this software.

// Define Gradle arguments
String veePortGroup = "com.nxp.vee.mimxrt1170_mapps"
String veePortModuleName = "vee-port"
String veePortVersion = "3.1.0"
String artifactName = "NXP-MIMXRT1170-${veePortVersion.replace('.', "_")}_GCC_GREEN"
String imageHeap = "1843200"
String skipJavaDoc = "false"

buildWithGradle {

	ARTIFACTS_DOMAIN = 'cross-developer'

	CODE_ANALYSIS_RUN_IN_FEATURE = true

	ARGS = "-Daccept-microej-sdk-eula-v3-1b=YES -Dcom.microej.architecture.usage=prod -Dmicroej.option.ej.microui.memory.imagesheap.size=$imageHeap -Dveeport.group=$veePortGroup -Dveeport.module=$veePortModuleName -Dveeport.version=$veePortVersion -Dartifact.name=$artifactName -Dskip.java.doc=$skipJavaDoc"

	DOCKER_IMAGE = 'artifactory.cross:18084/microej/sdk-gcc-arm-make-nxp-imxrt1170:2.2.0'
}

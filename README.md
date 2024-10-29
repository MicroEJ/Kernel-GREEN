![](https://shields.microej.com/endpoint?url=https://repository.microej.com/packages/badges/sdk_6.0.json)
![](https://shields.microej.com/endpoint?url=https://repository.microej.com/packages/badges/arch_8.0.json)
![](https://shields.microej.com/endpoint?url=https://repository.microej.com/packages/badges/gui_3.json)

Kernel GREEN
============

Welcome to the Kernel "GREEN", a MicroEJ Kernel Application project.

Its main goal is to provide a turnkey Kernel code example that offers basic services for developing a [Sandboxed Application](https://docs.microej.com/en/latest/ApplicationDeveloperGuide/sandboxedApplication.html) and deploying it easily on your Device.
Additionally, this project serves as a starting point for Kernel developers to learn and demonstrate most of the Multi-Sandboxing capabilities of the MicroEJ technology.

Feel free to fork and adapt the Kernel to fit your specific needs.

**IMPORTANT**: Kernel development requires a fundamental comprehension of building VEE Ports and Standalone Applications using MICROEJ SDK.
If you are not already familiar with MicroEJ Technology, you should start with the [MicroEJ Getting Started](https://docs.microej.com/en/latest/overview/gettingStarted.html) tutorials beforehand.
With that done, you can confidently embark on your exciting journey into Kernel development with MicroEJ.

# Specification

From a top-level perspective the Kernel provides the following services.

* **Network services**

  * [CommandServer](./src/main/java/com/microej/kernel/green/localdeploy/CommandServer.java)

    Listens for lifecycle management commands on port `4000` and handles them.

  * [NTPService](./src/main/java/com/microej/kernel/green/ntp/NTPService.java)

    Synchronizes the system clock using the Network Time Protocol.

* **Application services**

  * **Kernel-local services** (services registered by the Kernel and only accessible from the Kernel context)

    * [ConnectivityManager](https://repository.microej.com/javadoc/microej_5.x/apis/android/net/ConnectivityManager.NetworkCallback.html)

      Allows for querying the state of network connectivity and getting notified of network connectivity changes.

    * [Timer](https://repository.microej.com/javadoc/microej_5.x/apis/ej/bon/Timer.html)

      Allows scheduling possibly repeating background tasks in an efficient way.

  * **Shared services** (services registered by the Kernel and accessible from the Sandboxed Applications context)

    * [Storage](./src/main/java/com/microej/kernel/green/storage/StorageKfFs.java)

      Eases up data storage/retrieval to/from the persistent storage.

In more detail the Kernel implements the following specification.

- Sandboxed Applications Lifecycle

  - Enables the deployment of Sandboxed Applications from the MICROEJ SDK to your device through a TCP/IP connection.
    Features `.fo` files are also persisted.

  - Automatically starts all previously deployed Sandboxed Applications during boot.

  - Registers an instance of [ej.kf.FeatureStateListener](https://repository.microej.com/javadoc/microej_5.x/apis/ej/kf/FeatureStateListener.html) to log when the state of an Application changes.

- Runtime

  - Registers an instance of [ej.bon.Timer](https://repository.microej.com/javadoc/microej_5.x/apis/ej/bon/Timer.html) as a Kernel-local service allowing for scheduling time-based tasks without extra-thread creation.

  - Enables communication between Sandboxed Applications using [Shared Interfaces](https://docs.microej.com/en/latest/ApplicationDeveloperGuide/sandboxedAppSharedInterface.html).

    - Registers default [Kernel converters](https://docs.microej.com/en/latest/KernelDeveloperGuide/featuresCommunication.html?kernel-types-converter).

    - Provides a Shared Registry for Sandboxed Applications to register and retrieve services declared as Shared Interfaces.

- Networking

  - Starts the [ServerSocket](https://repository.microej.com/javadoc/microej_5.x/apis/java/net/ServerSocket.html) and listens for Sandboxed Applications deployment commands.

  - Registers an instance of [android.net.ConnectivityManager](https://repository.microej.com/javadoc/microej_5.x/apis/android/net/ConnectivityManager.html) as a Kernel-local service allowing for monitoring network connectivity.

  - Registers an [android.net.ConnectivityManager.NetworkCallback](https://repository.microej.com/javadoc/microej_5.x/apis/android/net/ConnectivityManager.NetworkCallback.html) that logs all available network interfaces on network state change.

  - Synchronizes the time of the device using NTP.

- Persistency

  - Registers an instance of [ej.storage.Storage](https://repository.microej.com/javadoc/microej_5.x/apis/ej/storage/Storage.html) as a shared service to provide Sandboxed Applications with a simple persistency mechanism.
    The implementation is based on FS (File System) API. The Kernel and each Sandboxed Application have their own data space.

- Graphical User Interface

  - Starts the MicroUI Graphical Engine.

  - Initializes the display with a [Black screen](./src/main/java/com/microej/kernel/green/gui/BlackScreenDisplayable.java). When a Sandboxed Application is uninstalled, the Kernel checks if itself or any other started application do own a [Displayable](https://repository.microej.com/javadoc/microej_5.x/apis/index.html?ej/microui/display/Displayable.html) object, if none is found, it will render the black screen.

- Security Management

  - Registers a logging-only [SecurityManager](https://repository.microej.com/javadoc/microej_5.x/apis/java/lang/SecurityManager.html) that grants any permission from any application and logs the event to the debug console.

    Please refer to the [Security Management](#security-management) section for more information.

# Set up the Kernel Project

## Prerequisites

### Environment

- MICROEJ SDK `6.0.0` or higher (tested with [MICROEJ SDK 6](https://docs.microej.com/en/latest/SDK6UserGuide/install.html)).
- A [MicroEJ Evaluation License](https://docs.microej.com/en/latest/SDK6UserGuide/licenses.html#evaluation-licenses).

### VEE Port

The Kernel can be built using any VEE Port that provides the following Foundation Libraries.

| Foundation Library | Version |
| ------------------ |---------|
| EDC                | 1.3     |
| BON                | 1.4     |
| KF                 | 1.7     |
| NET                | 1.1     |
| SSL                | 2.2     |
| MicroUI            | 3.1     |
| Drawing            | 1.0     |
| FS                 | 2.1     |

**WARNING**: **The VEE Port must be built with [Multi-Sandbox](https://docs.microej.com/en/latest/VEEPortingGuide/multiSandbox.html) capability.**
Check out the [VEE Porting Guide](https://docs.microej.com/en/latest/VEEPortingGuide/multiSandbox.html#installation) for more information about enabling Multi-Sandbox capacities.

#### Reference VEE Ports

The Kernel has been tested against the following reference VEE Ports:
- [VEE Port for NXP i.MX RT1170 EVK v2.2.0 (default)](https://github.com/nxp-mcuxpresso/nxp-vee-imxrt1170-evk/tree/NXPVEE-MIMXRT1170-EVK-2.2.0)
```
mkdir nxpvee-mimxrt1170-prj
cd nxpvee-mimxrt1170-prj
west init -m https://github.com/nxp-mcuxpresso/nxp-vee-imxrt1170-evk .
west update
cd nxpvee-mimxrt1170-evk/
git checkout NXPVEE-MIMXRT1170-EVK-2.2.0
```

**NOTE:** ``west`` is a tool provided by the NXP MCUXpresso SDK Developer necessary for the NXP i.MX RT1170 EVK board, more information about west [here](https://github.com/nxp-mcuxpresso/nxp-vee-imxrt1170-evk/tree/NXPVEE-MIMXRT1170-EVK-2.2.0?tab=readme-ov-file#mcuxpresso-installer-tool).

- [VEE Port for STMicroelectronics STM32F7508-DK Discovery Kit v2.3.0](https://github.com/MicroEJ/VEEPort-STMicroelectronics-STM32F7508-DK/tree/2.3.0)
```
git clone --branch 2.3.0 https://github.com/MicroEJ/VEEPort-STMicroelectronics-STM32F7508-DK.git
git submodule update --init --recursive
```

For further information about VEE Ports, please refer to their respective README.

##### Reference VEE Ports version compatibility matrix

###### STM32F7508-DK

| Kernel version | VEE Port version |
|----------------|------------------|
| 1.3.0          | 2.0.0            |
| 1.4.0          | 2.0.0            |
| 2.0.0          | 2.3.0            |

###### NXP i.MX RT1170 EVK

| Kernel version | VEE Port version |
|----------------|------------------|
| 1.3.0          | 2.1.1            |
| 1.4.0          | 2.1.1            |
| 2.0.0          | 2.2.0            |


## Import the Kernel project in an IDE

The MicroEJ SDK6 is not bound to a specific IDE, therefore you are free to use any of the supported IDE.
MicroEJ documents the usage of SDK 6 on the following IDEs:
* [Android Studio](https://developer.android.com/studio?hl=en)
* [IntelliJ IDEA](https://www.jetbrains.com/idea/)
* [Eclipse](https://eclipseide.org/)

To understand how to import a Kernel project the listed IDEs, please refer to the public documentation **[Import Project](https://docs.microej.com/en/latest/SDK6UserGuide/importProject.html)**.

## Configure the VEE Port

The VEE Port configuration is done in the [build.gradle.kts](./build.gradle.kts) file.

The VEE Port can be configured in one of the following ways:

- By declaring it as a Module Dependency. (default)
- By specifying its local source directory.

### Declaring the VEE Port as a Module Dependency

This approach allows for fetching the VEE Port sources from a MicroEJ repository. By default, the MicroEJ SDK is configured to fetch VEE Ports from the [Developer Repository](https://docs.microej.com/en/latest/SDKUserGuide/repository.html#developer-repository).

In order to declare the VEE Port dependency: 
* Open the [build.gradle.kts](./build.gradle.kts) file.
* Set the ``defaultVeePortGroup`` variable to your VEE Port group name.
* Set the ``defaultVeePortModule`` variable to your VEE Port module name.
* Set the ``defaultVeePortVersion`` variable to your VEE Port version.

You can also override the default variables by using specifying arguments in the Gradle commands.

For instance, to ensure the VEE Port configuration, you can execute the ``loadVee`` gradle task such as:  
``gradlew loadVee -Dveeport.group=yourVEEPortGroup -Dveeport.module=yourVEEPortModuleName -Dveeport.version=yourVEEPortVersion``


If the VEE Port has been correctly configured, the output result should be:
```
BUILD SUCCESSFUL in 12s
1 actionable task: 1 up-to-date
```

**REMINDER**: **The VEE Port must be built with [Multi-Sandbox](https://docs.microej.com/en/latest/VEEPortingGuide/multiSandbox.html) capability.**


### Specifying the VEE Port source directory

This approach allows for building the Kernel against a VEE Port which sources are fetched locally. _Kernel GREEN_ is indeed not bound to a specific VEE Port and can be built against any other VEE Port as long as the [VEE Port requirements](#vee-port) are met.

Sources for the reference VEE Ports are [available on GitHub](#reference-vee-ports).

In order to set a local VEE Port path:
* Open the [build.gradle.kts](./build.gradle.kts) file.
* Set the ``defaultLocalVEEPortPath`` variable to your local VEE Port source folder.

You can also override the default variable by using specifying arguments in the Gradle commands.

For instance, to ensure the VEE Port configuration, you can execute the ``loadVee`` gradle task such as:  
``gradlew loadVee -Dlocal.veeport.path=C:\path\to\local\veeport\source``

**NOTE:** If the variable ``defaultLocalVEEPortPath`` is not empty, the ``build.gradle.kts`` file will use the specified local VEE Port path. Otherwise, it will use the module dependency way to fetch the VEE Port.

## Set up the VEE Port build environment

Before going any further with the build the VEE Port must be set up with toolchain-specific environment variables and BSP Connection options.

Please refer to the VEE Port specific documentation for more details. As for the [reference VEE Ports](#reference-vee-ports), please refer to the project _README_ file.

Also, please remember that a valid MicroEJ License is required for building. Please refer to [this section from the MicroEJ SDK User Guide](https://docs.microej.com/en/latest/SDKUserGuide/licenses.html#evaluation-licenses) to get help on obtaining and installing a MicroEJ Evaluation License.

# Build the Kernel

Once the VEE Port is configured, you are ready to build the Kernel.
To build the Kernel, execute the ``buildExecutable`` Gradle task using either the GUI from your Idea or by using the CLI:
``gradlew buildExecutable``

The build then start and can take few minutes.

At the end of the build, you should see the following:  
``
BUILD SUCCESSFUL in 41s
8 actionable tasks: 4 executed, 4 up-to-date
``

The output of the build is located in the `build/application/executable` directory should contain the build output:
* `application.out`: the executable file to be programmed on the device.


# Deploy an application

Once the Kernel is built, Sandboxed Applications can then be dynamically deployed on the Kernel.

For more information about Sandboxed Applications, please refer to the [official documentation](https://docs.microej.com/en/latest/ApplicationDeveloperGuide/sandboxedApplication.html)

To deploy an application on the Kernel, paste the following code in the ``build.gradle.kts`` file of the application:

```
import com.microej.gradle.tasks.ExecToolTask
import com.microej.gradle.tasks.LoadKernelExecutableTask
import com.microej.gradle.tasks.LoadVeeTask

val loadVee = tasks.withType(LoadVeeTask::class).named("loadVee")
val loadKernelExecutableTask = tasks.withType(LoadKernelExecutableTask::class).named("loadKernelExecutable")

tasks.register<ExecToolTask>("localDeploy") {

    group="microej"
    
    veeDir.set(loadVee.get().loadedVeeDir)
    resourcesDirectories.from(project.extensions.getByType(SourceSetContainer::class)
            .getByName(SourceSet.MAIN_SOURCE_SET_NAME).output.resourcesDir,
            project.layout.buildDirectory.dir("generated/microej-app-wrapper/resources"))
    classesDirectories.from(project.extensions.getByType(SourceSetContainer::class)
            .getByName(SourceSet.MAIN_SOURCE_SET_NAME).output.classesDirs)

    classpathFromConfiguration.from(project.getConfigurations().getByName("runtimeClasspath"))

    // These inputs concern the localDeploymentSocket tool only
    toolName = "localDeploymentSocket"
    inputs.file(loadKernelExecutableTask.get().loadedKernelExecutableFile)
    toolProperties.putAll(mapOf(
            "application.main.class" to microej.applicationEntryPoint,
            "board.server.host" to "x.x.x.x",
            "board.server.port" to "4000",
            "board.timeout" to "120000",
            "use.storage" to "true"
    ))
    doFirst {
        toolProperties["kernel.filename"] = loadKernelExecutableTask.get().loadedKernelExecutableFile.get().asFile.absolutePath
    }
}
```

This code will register a new Gradle task named ``localDeploy`` in the ``microej`` group for the Sandboxed application that can be run like any other Gradle task.

**Note:** in the above code, make sure to update ``application.main.class``, ``board.server.host``, ``board.server.port``,``board.timeout``, ``use.storage`` properties according to your needs.


# Going further

By now, you should have completed the initial functional Kernel.
Before proceeding with any customization, please consult the [Kernel Developer Guide](https://docs.microej.com/en/latest/KernelDeveloperGuide/index.html).

If you wish to understand the core Multi-Sandboxing mechanisms, you can refer to the following resources:

- The [Kernel & Features specification](https://docs.microej.com/en/latest/KernelDeveloperGuide/kf.html) which provides a detailed explanation of core concepts.
- The [ej.kf.Kernel class](https://repository.microej.com/javadoc/microej_5.x/apis/ej/kf/Kernel.html) which offers the complete Javadoc API.

The following sections deal with specific topics you may want to experiment with.

## Security management

_Note: please refer to [this section](https://docs.microej.com/en/latest/KernelDeveloperGuide/kernelCreation.html#implement-a-security-policy) from the [Kernel Developer Guide](https://docs.microej.com/en/latest/KernelDeveloperGuide/index.html) to get a primer on security management._

_Kernel GREEN_ provides two ready-to-use implementations:
* (default) a logging-only policy using [KernelSecurityManager](https://repository.microej.com/javadoc/microej_5.x/apis/com/microej/kf/util/security/KernelSecurityManager.html) in order to demonstrate how the Kernel may restrict sensitive or possibly unsafe operations performed by applications.
* an actual security policy based on resource file from applications using [KernelSecurityPolicyManager](https://repository.microej.com/javadoc/microej_5.x/apis/com/microej/kf/util/security/KernelSecurityPolicyManager.html) that allows application to describe permissions they will need at runtime.


These operations cover all operations restricted by one of the supported [Permission](https://repository.microej.com/javadoc/microej_5.x/apis/java/security/Permission.html)s which are, in this Kernel:

* [DisplayPermission](https://repository.microej.com/javadoc/microej_5.x/apis/ej/microui/display/DisplayPermission.html)
* [EventPermission](https://repository.microej.com/javadoc/microej_5.x/apis/ej/microui/event/EventPermission.html)
* [FilePermission](https://repository.microej.com/javadoc/microej_5.x/apis/java/io/FilePermission.html)
* [FontPermission](https://repository.microej.com/javadoc/microej_5.x/apis/ej/microui/display/FontPermission.html)
* [ImagePermission](https://repository.microej.com/javadoc/microej_5.x/apis/ej/microui/display/ImagePermission.html)
* [MicroUIPermission](https://repository.microej.com/javadoc/microej_5.x/apis/ej/microui/MicroUIPermission.html)
* [NetPermission](https://repository.microej.com/javadoc/microej_5.x/apis/java/net/NetPermission.html)
* [ej.property.PropertyPermission](https://repository.microej.com/javadoc/microej_5.x/apis/ej/property/PropertyPermission.html)
* [java.util.PropertyPermission](https://repository.microej.com/javadoc/microej_5.x/apis/java/util/PropertyPermission.html)
* [RuntimePermission](https://repository.microej.com/javadoc/microej_5.x/apis/java/lang/RuntimePermission.html)
* [ServicePermission](https://repository.microej.com/javadoc/microej_5.x/apis/ej/service/ServicePermission.html)
* [SocketPermission](https://repository.microej.com/javadoc/microej_5.x/apis/java/net/SocketPermission.html)
* [SSLPermission](https://repository.microej.com/javadoc/microej_5.x/apis/javax/net/ssl/SSLPermission.html)


To switch between the two ready-to-use implementations listed above, you can edit the [security.properties.list](src/main/resources/security.properties.list) file by updating the ``security.manager.mode`` property value to the following values:
* ``LOGGING`` (default): uses the logging only security management policy to show any protected access by any application at runtime.
* ``POLICY_FILE``: uses the resource file based system to allow each application to describe permissions it will need at runtime.

A more complete explanation of these implementations is available on the [MicroEJ Developer Website](https://docs.microej.com/en/latest/KernelDeveloperGuide/applicationSecurityPolicy.html) in the  ``Application security policy`` section.

# Troubleshooting

## The local specified VEE Port path is not correctly set

```
FAILURE: Build failed with an exception.

* What went wrong:
  Execution failed for task ':loadVee'.
> No 'release.properties' and 'architecture.properties' files found.
The given file Path/to/specified/VEEPort/path is not a VEE archive.
```

This error is caused when the path specified for a local VEE Port in the ``build.gradle.kts`` file is not pointing to a valid VEE Port folder.

---
_Copyright 2021-2024 MicroEJ Corp. All rights reserved._  
_Use of this source code is governed by a BSD-style license that can be found with this software._
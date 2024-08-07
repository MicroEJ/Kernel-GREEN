![](https://shields.microej.com/endpoint?url=https://repository.microej.com/packages/badges/sdk_5.7.json)
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

    - Initializes the display with a [black screen](./src/main/java/com/microej/firmware/developer/green/BlackScreenDisplayable.java). When a Sandboxed Application is uninstalled, the Kernel checks if itself or any other started application do own a [Displayable](https://repository.microej.com/javadoc/microej_5.x/apis/index.html?ej/microui/display/Displayable.html) object, if none is found, it will render a new black screen.

- Security Management

    - By default, registers a logging-only [SecurityManager](https://repository.microej.com/javadoc/microej_5.x/apis/java/lang/SecurityManager.html) that grants any permission from any application and logs the event to the debug console.

    - Configuration can be done to use a security management based on policy files coming from applications instead.

      Please refer to the [Security Management](#security-management) section for more information.

# Set up the Kernel Project

## Prerequisites

### Environment

- MICROEJ SDK `5.7.0` or higher (tested with [MICROEJ SDK Distribution](https://docs.microej.com/en/latest/SDKUserGuide/installSDKDistributionLatest.html) `23.07`).
- A [MicroEJ Evaluation License](https://docs.microej.com/en/latest/SDKUserGuide/licenses.html).

### VEE Port

The Kernel can be built using any VEE Port that provides the following Foundation Libraries.

| Foundation Library | Version |
| ------------------ | ------- |
| EDC                | 1.3     |
| BON                | 1.4     |
| KF                 | 1.6     |
| NET                | 1.1     |
| SSL                | 2.2     |
| MicroUI            | 3.1     |
| Drawing            | 1.0     |
| FS                 | 2.1     |

**WARNING**: The VEE Port must be built with [Multi-Sandbox](https://docs.microej.com/en/latest/VEEPortingGuide/multiSandbox.html) capability.  
Check out the [VEE Porting Guide](https://docs.microej.com/en/latest/VEEPortingGuide/multiSandbox.html#installation) for more information about enabling Multi-Sandbox capacities.

#### Reference VEE Ports

The Kernel has been tested against the following reference VEE Ports:
- [VEE Port for STMicroelectronics STM32F7508-DK Discovery Kit v2.0.0](https://github.com/MicroEJ/VEEPort-STMicroelectronics-STM32F7508-DK/tree/2.0.0)
- [VEE Port for NXP i.MX RT1170 EVK v2.1.1](https://github.com/nxp-mcuxpresso/nxp-vee-imxrt1170-evk/tree/NXPVEE-MIMXRT1170-EVK-2.1.1)

##### Reference VEE Ports version compatibility matrix

###### STM32F7508-DK

| Kernel version | VEE Port version |
|----------------|------------------|
| 1.3.0          | 2.0.0            |
| 1.4.0          | 2.0.0            |

###### NXP i.MX RT1170 EVK

| Kernel version | VEE Port version |
|----------------|------------------|
| 1.3.0          | 2.1.1            |
| 1.4.0          | 2.1.1            |

## Import the Kernel Project in the SDK

Import the projects in the SDK's workspace:

- Select `File > Import > General > Existing Projects into Workspace`.
- Select the root directory the sources were cloned into.
- Click on the `Finish` button.
- As the VEE Port has not been configured yet the following error message should appear.
  ```
  'IvyDE resolve' has encountered a problem. Some projects fail to be resolved.
  ```
  The VEE Port will be configured in the next section so click on `OK` to discard the error.

At that point the Kernel Application project `com.microej.kernel.green` has now been imported into your workspace.

## Configure the VEE Port

The VEE Port can be configured in one of the following ways:

- By specifying its local source directory. (default)
- By declaring it as a Module Dependency.

### Specifying the VEE Port source directory

This approach allows for building the Kernel against a VEE Port which sources are fetched locally. _Kernel GREEN_ is indeed not bound to a specific VEE Port and can be built against any other VEE Port as long as the [VEE Port requirements](#vee-port) are met.

Sources for the reference VEE Ports are [available on GitHub](#reference-vee-ports).

In order to specify the VEE Port source directory, please refer to the instructions described in the [`module.ivy`](./module.ivy) file.

### Declaring the VEE Port as a Module Dependency

This approach allows for fetching the VEE Port sources from a MicroEJ repository. By default, the MicroEJ SDK is configured to fetch VEE Ports from the [Developer Repository](https://docs.microej.com/en/latest/SDKUserGuide/repository.html#developer-repository).

In order to declare the VEE Port dependency, set the organization, name and version in the [module.properties](./module.properties) file.

For the VEE Port dependency to be resolved in the workspace, add the aforementioned properties file to the _Ant_ runtime by following the below steps.
- Open `Window` > `Preferences`.
- In the `Ant` section, select `Runtime`.
- In the `Properties` tab, click on `Add Files...`
- Select the `module.properties` file located at the root of the Kernel project.
- Click `OK`.
- Restart the SDK.

## Set up the VEE Port build environment

Before going any further with the build, the VEE Port must be set up with toolchain-specific environment variables and BSP Connection options.

Please refer to the VEE Port specific documentation for more details. As for the [reference VEE Ports](#reference-vee-ports), please refer to the project _README_ file.

Also, please remember that a valid MicroEJ License is required for building. Please refer to [this section from the MicroEJ SDK User Guide](https://docs.microej.com/en/latest/SDKUserGuide/licenses.html#evaluation-licenses) to get help on obtaining and installing a MicroEJ Evaluation License.

# Build the Kernel

Once the VEE Port is configured, you are ready to build the Kernel.
Right-click on the Kernel project and select `Build Module`.
The build is started and may take a few minutes.

At the end of the build, the `target~/artifacts/` directory should contain the build output:
- `GREEN.out`: the Executable file to be programmed on the Device.
- `GREEN.vde`: the Virtual Device for running Sandboxed Applications on this Kernel.

# Develop Sandboxed Applications

You are now ready to test your Kernel and deploy your first Sandboxed Application.
To do so follow the [Get Started with Multi-Sandbox for STM32F7508-DK Discovery Kit](https://developer.microej.com/stm32f7508-dk-discovery-kit-get-started-multi-sandbox/) guide using your own generated Executable and Virtual Device files.

You will learn how to:
- create a first Sandboxed Application,
- run a Sandboxed Application on the Virtual Device,
- run the same Sandboxed Application on your real device.

Instructions may apply to any VEE Port, except for those related to the deployment of the Multi-Sandbox Firmware which are specific the STM32F7508-DK Discovery Kit.
When working with a different target device, you will need to adjust these instructions accordingly.

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
- (default) a logging-only policy using [KernelSecurityManager](https://repository.microej.com/javadoc/microej_5.x/apis/com/microej/kf/util/security/KernelSecurityManager.html) in order to demonstrate how the Kernel may restrict sensitive or possibly unsafe operations performed by applications.
- an actual security policy based on resource file from applications using [KernelSecurityPolicyManager](https://repository.microej.com/javadoc/microej_5.x/apis/com/microej/kf/util/security/KernelSecurityPolicyManager.html) that allows application to describe permissions they will need at runtime.


These operations cover all operations restricted by one of the supported [Permission](https://repository.microej.com/javadoc/microej_5.x/apis/java/security/Permission.html)s which are, in this Kernel:

- [DisplayPermission](https://repository.microej.com/javadoc/microej_5.x/apis/ej/microui/display/DisplayPermission.html)
- [EventPermission](https://repository.microej.com/javadoc/microej_5.x/apis/ej/microui/event/EventPermission.html)
- [FilePermission](https://repository.microej.com/javadoc/microej_5.x/apis/java/io/FilePermission.html)
- [FontPermission](https://repository.microej.com/javadoc/microej_5.x/apis/ej/microui/display/FontPermission.html)
- [ImagePermission](https://repository.microej.com/javadoc/microej_5.x/apis/ej/microui/display/ImagePermission.html)
- [MicroUIPermission](https://repository.microej.com/javadoc/microej_5.x/apis/ej/microui/MicroUIPermission.html)
- [NetPermission](https://repository.microej.com/javadoc/microej_5.x/apis/java/net/NetPermission.html)
- [ej.property.PropertyPermission](https://repository.microej.com/javadoc/microej_5.x/apis/ej/property/PropertyPermission.html)
- [java.util.PropertyPermission](https://repository.microej.com/javadoc/microej_5.x/apis/java/util/PropertyPermission.html)
- [RuntimePermission](https://repository.microej.com/javadoc/microej_5.x/apis/java/lang/RuntimePermission.html)
- [ServicePermission](https://repository.microej.com/javadoc/microej_5.x/apis/ej/service/ServicePermission.html)
- [SocketPermission](https://repository.microej.com/javadoc/microej_5.x/apis/java/net/SocketPermission.html)
- [SSLPermission](https://repository.microej.com/javadoc/microej_5.x/apis/javax/net/ssl/SSLPermission.html)


To switch between the two ready-to-use implementations listed above, you can edit the [security.properties.list](src/main/resources/security.properties.list) file by updating the ``security.manager.mode`` property value to the following values:
- ``LOGGING`` (default): uses the logging only security management policy to show any protected access by any application at runtime.
- ``POLICY_FILE``: uses the resource file based system to allow each application to describe permissions it will need at runtime.

A more complete explanation of these implementations is available on the [MicroEJ Developer Website](https://docs.microej.com/en/latest/KernelDeveloperGuide/applicationSecurityPolicy.html) in the  ``Application security policy`` section.


# Troubleshooting


## The local specified VEE Port sources path is not correctly set

```
    platform-loader:set-use-dir:
    [echo] Platform loaded from property `platform-loader.target.platform.dir`. []

    platform-loader:load-platform-dir:
    [copy] Copying 206 files to C:\Users\microej\Kernel-Green\target~\virtualDevice
    [copy] Copied 79 empty directories to 23 empty directories under C:\Users\microej\Kernel-Green\target~\virtualDevice

    BUILD FAILED
    Found an error when building GREEN
    * Where

    File : C:\Program Files\MicroEJ\MicroEJ-SDK-23.07\rcp\configuration\org.eclipse.osgi\515\data\repositories\microej-build-repository\com\is2t\easyant\plugins\platform-loader\2.1.0\platform-loader-2.1.0.ant
    Line : 377 column : 167

    * Problem Report:

    The Platform must have been built from an Architecture version 7.10.0 or higher (Architecture version not found)
```

This issue is caused by a wrong path to the local VEE Port sources folder, please double check the ``platform-loader.target.platform.dir`` property in the [module.ivy](./module.ivy) file


## The specified VEE Port does not have Multi-Sandbox capabilities

```
    spawn-jvm:
    [java] Buildfile: C:\Program Files\MicroEJ\MicroEJ-SDK-23.07\rcp\configuration\org.eclipse.osgi\515\data\repositories\microej-build-repository\com\is2t\easyant\plugins\platform-launcher\4.1.0\launch-4.1.0.xml
    [java] init:
    [java] launch:
    [java] BUILD FAILED
    [java] C:\Program Files\MicroEJ\MicroEJ-SDK-23.07\rcp\configuration\org.eclipse.osgi\515\data\repositories\microej-build-repository\com\is2t\easyant\plugins\platform-launcher\4.1.0\launch-4.1.0.xml:25: The following error occurred while executing this line:
    [java] java.io.FileNotFoundException: C:\Users\microej\Kernel-Green\target~\virtualDevice\scripts\kernelPackager.microejTool (The system cannot find the file specified)
    [java] 	at java.io.FileInputStream.open(Native Method)
    [java] 	at java.io.FileInputStream.<init>(FileInputStream.java:131)
    [java] 	at org.apache.tools.ant.helper.ProjectHelper2.parse(ProjectHelper2.java:250)
    [java] 	at org.apache.tools.ant.helper.ProjectHelper2.parse(ProjectHelper2.java:178)
    [java] 	at org.apache.tools.ant.ProjectHelper.configureProject(ProjectHelper.java:93)
    [java] 	at org.apache.tools.ant.taskdefs.Ant.execute(Ant.java:392)
    [java] 	at org.apache.tools.ant.UnknownElement.execute(UnknownElement.java:293)
    [java] 	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    [java] 	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
    [java] 	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    [java] 	at java.lang.reflect.Method.invoke(Method.java:483)
    [java] 	at org.apache.tools.ant.dispatch.DispatchUtils.execute(DispatchUtils.java:106)
    [java] 	at org.apache.tools.ant.Task.perform(Task.java:348)
    [java] 	at org.apache.tools.ant.Target.execute(Target.java:435)
    [java] 	at org.apache.tools.ant.Target.performTasks(Target.java:456)
    [java] 	at org.apache.tools.ant.Project.executeSortedTargets(Project.java:1405)
    [java] 	at org.apache.tools.ant.Project.executeTarget(Project.java:1376)
    [java] 	at org.apache.tools.ant.helper.DefaultExecutor.executeTargets(DefaultExecutor.java:41)
    [java] 	at org.apache.tools.ant.Project.executeTargets(Project.java:1260)
    [java] 	at org.apache.tools.ant.Main.runBuild(Main.java:857)
    [java] 	at org.apache.tools.ant.Main.startAnt(Main.java:236)
    [java] 	at org.apache.tools.ant.Main.start(Main.java:199)
    [java] 	at org.apache.tools.ant.Main.main(Main.java:287)
    [java] Total time: 0 seconds

    BUILD FAILED
    Found an error when building GREEN
    * Where
    
    File : C:\Program Files\MicroEJ\MicroEJ-SDK-23.07\rcp\configuration\org.eclipse.osgi\515\data\repositories\microej-build-repository\com\is2t\easyant\plugins\kernel-packager\5.1.0\kernel-packager-5.1.0.ant
    Line : 109 column : 5
```

This error will appear when trying to build a Kernel against a VEE Port that does not have Multi-Sandboxed capabilities. To fix this issue, update the value of the property ``com.microej.platformbuilder.module.multi.enabled`` to ``true`` (this property is usually set in the ``module.properties`` file from the ``configuration`` project of the VEE Port).


---
_Copyright 2021-2024 MicroEJ Corp. All rights reserved._  
_Use of this source code is governed by a BSD-style license that can be found with this software._
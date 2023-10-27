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

  - Registers a logging-only [SecurityManager](https://repository.microej.com/javadoc/microej_5.x/apis/java/lang/SecurityManager.html) that grants any permission from any application and logs the event to the debug console.

    Please refer to the [Security Management](#security-management) section for more information.

# Setup the Kernel Project

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

The Kernel has been tested against the [VEE Port for STMicroelectronics STM32F7508-DK Discovery Kit](https://github.com/MicroEJ/VEEPort-STMicroelectronics-STM32F7508-DK) v2.0.0.

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

As for the reference VEE Port for `STMicroelectronics STM32F7508-DK Discovery Kit` sources may be retrieved by cloning the [VEE Port repository available on GitHub](https://github.com/MicroEJ/VEEPort-STMicroelectronics-STM32F7508-DK).

In order to specify the VEE Port source directory, please refer to the instructions described in the [`module.ivy`](./module.ivy) file.

### Declaring the VEE Port as a Module Dependency

This approach allows for fetching the VEE Port sources from a MicroEJ repository. By default the MicroEJ SDK is configured to fetch VEE Ports from the [Developer Repository](https://docs.microej.com/en/latest/SDKUserGuide/repository.html#developer-repository).

In order to declare the VEE Port dependency, set the organization, name and version in the [module.properties](./module.properties) file. By default the VEE Port for _STMicroelectronics STM32F7508-DK Discovery Kit_ with the STM32CubeIDE / GCC C toolchain is selected as the target VEE Port.

For the VEE Port dependency to be resolved in the workspace, add the aforementioned properties file to the _Ant_ runtime by following the below steps.
- Open `Window` > `Preferences`.
- In the `Ant` section, select `Runtime`.
- In the `Properties` tab, click on `Add Files...`
- Select the `module.properties` file located at the root of the Kernel project.
- Click `OK`.
- Restart the SDK.

## Set up the VEE Port build environment

Before going any further with the build the VEE Port must be set up with toolchain-specific environment variables and BSP Connection options.

Please refer to the VEE Port specific documentation for more details. For the reference VEE Port for `STMicroelectronics STM32F7508-DK Discovery Kit` this information can be found in the [VEE Port README file](https://github.com/MicroEJ/VEEPort-STMicroelectronics-STM32F7508-DK#readme).

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

_Kernel GREEN_ provides a logging-only [SecurityManager](https://repository.microej.com/javadoc/microej_5.x/apis/java/lang/SecurityManager.html) in order to demonstrate how the Kernel may restrict sensitive or possibly unsafe operations performed by applications.

When enabled this security manager logs on the debug console any attempt from applications to perform a restricted operation before authorizing it, providing a similar output to below.

```
Granted permission 'java.net.NetPermission' with action 'getNetworkInformation' for feature 'Feature'
```

These operations cover all operations restricted by one of the supported [Permission](https://repository.microej.com/javadoc/microej_5.x/apis/java/security/Permission.html)s which are:

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

The Kernel maps all permissions to the same [PermissionLogger](./src/main/java/com/microej/kernel/green/security/PermissionLogger.java) instance that simply grants the permission to any application and logs the event to the debug console.

However more complex permission checks can be implemented by following the steps below.

1. Define a _permission check delegate_ class that implements the [FeaturePermissionCheckDelegate](https://repository.microej.com/javadoc/microej_5.x/apis/com/microej/kf/util/security/FeaturePermissionCheckDelegate.html) interface like `CustomPermissionCheckDelegate` below.

   ```Java
   public class CustomPermissionCheckDelegate implements FeaturePermissionCheckDelegate {

       @Override
       public void checkPermission(Permission permission, Feature feature) {

           // Actual permission check goes here...

       }

   }
   ```

2. Associate an instance of this _permission check delegate_ class with the [Permission](https://repository.microej.com/javadoc/microej_5.x/apis/java/security/Permission.html) to be checked (like `NetPermission` in the example below) by means of the security manager.

   ```Java
   KernelSecurityManager securityManager = new KernelSecurityManager();

   // ...

   securityManager.setFeaturePermissionDelegate(NetPermission.class, new CustomPermissionCheckDelegate());
   ```

As an example one may want to prevent applications from performing network operations that may be considered harmful.

This may be achieved on the Kernel-side by unconditionally denying the [NetPermission](https://repository.microej.com/javadoc/microej_5.x/apis/java/net/NetPermission.html) to all applications like so.

```Java
final KernelSecurityManager securityManager = new KernelSecurityManager();

// Allocate a FeaturePermissionCheckDelegate denying any permission from any application...
final FeaturePermissionCheckDelegate permissionDenier = new FeaturePermissionCheckDelegate() {
	@Override
	public void checkPermission(Permission permission, Feature feature) {
		throw new SecurityException();
	}
};

// ... and associate this FeaturePermissionCheckDelegate instance with the Permission to be denied
securityManager.setFeaturePermissionDelegate(NetPermission.class, permissionDenier);
```
---
_Copyright 2021-2023 MicroEJ Corp. All rights reserved._  
_Use of this source code is governed by a BSD-style license that can be found with this software._
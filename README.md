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

This Kernel implements the following specification:

- Sandboxed Applications Lifecycle
  
  - Enables the deployment of Sandboxed Applications from the MICROEJ SDK to your device through a TCP/IP connection.
    Features `.fo` files are also persisted.
  
  - Automatically starts all previously deployed Sandboxed Applications during boot. 
  
  - Registers an instance of [ej.kf.FeatureStateListener](https://repository.microej.com/javadoc/microej_5.x/apis/ej/kf/FeatureStateListener.html) to log when the state of an Application changes.

- Runtime
  
  - Registers [ej.bon.Timer](https://repository.microej.com/javadoc/microej_5.x/apis/ej/bon/Timer.html) as a service. 
    Sandboxed Applications can schedule time-based tasks without extra-thread creation.

  - Enables communication between Sandboxed Applications using [Shared Interfaces](https://docs.microej.com/en/latest/ApplicationDeveloperGuide/sandboxedAppSharedInterface.html).
  
    - Registers default [Kernel converters](https://docs.microej.com/en/latest/KernelDeveloperGuide/featuresCommunication.html?kernel-types-converter).

    - Provides a Shared Registry for Sandboxed Applications to register and retrieve services declared as Shared Interfaces.

- Networking

  - Starts the [ServerSocket](https://repository.microej.com/javadoc/microej_5.x/apis/java/net/ServerSocket.html) and listens for Sandboxed Applications deployment commands.

  - Registers an instance of [android.net.ConnectivityManager.NetworkCallback](https://repository.microej.com/javadoc/microej_5.x/apis/android/net/ConnectivityManager.NetworkCallback.html) to log all available network interfaces on network state update.

  - Synchronizes the time of the device using NTP.

- Persistency

  - Registers a [ej.storage.Storage](https://repository.microej.com/javadoc/microej_5.x/apis/ej/storage/Storage.html) service to provide a simple persistency mechanism for Sandboxed Applications.
    The implementation is based on FS (File System) API. The Kernel and each Sandboxed Application have their own data space.

- Graphical User Interface
 
  - Starts the MicroUI Graphical Engine.
  
  - Initializes the display with a [black screen](./src/main/java/com/microej/firmware/developer/green/BlackScreenDisplayable.java). When a Sandboxed Application is uninstalled, the Kernel checks if itself or any other started application do own a [Displayable](https://repository.microej.com/javadoc/microej_5.x/apis/index.html?ej/microui/display/Displayable.html) object, if none is found, it will render a new black screen.


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

The Kernel has been tested using the [VEE Port for STMicroelectronics STM32F7508-DK Discovery kit](https://github.com/MicroEJ/VEEPort-STMicroelectronics-STM32F7508-DK/tree/2.0.0) v2.0.0.

## Import the Kernel Project in the SDK

Import the projects in the SDK's workspace:

- Select `File > Import > General > Existing Projects into Workspace`.
- Select root directory to where you cloned the sources.
- Click on the `Finish` button. 

The Kernel Application project `com.microej.kernel.green` is imported in your workspace.

## Configure the VEE Port

The VEE Port can be configured in one of the following ways:

- By declaring a Module Dependency (default).
- By declaring a local directory.

### Declare the VEE Port from a Module Dependency
Set the VEE Port dependency organization, name and version in the [module.properties](./module.properties) file to select the target VEE Port.  
By default the VEE Port for _STMicroelectronics STM32F7508-DK Discovery Kit_ with the STM32CubeIDE / GCC C toolchain is selected as the target VEE Port.

For the VEE Port dependency to be resolved in the workspace, add the aforementioned properties file to the _Ant_ runtime by following the below steps.
- Select on `Window` > `Preferences`.
- In the `Ant` section, select `Runtime`.
- In the `Properties` tab, click on `Add Files...`
- Select the `module.properties` file located at the root of the Kernel project.
- Click `Ok`.
- Restart the SDK.

Please note that the MicroEJ SDK is by default configured to fetch VEE Ports from the [Developer Repository](https://docs.microej.com/en/latest/SDKUserGuide/repository.html#developer-repository) by default.

### Declare the VEE Port from a Local Directory

If you want to build the Kernel on a VEE Port built locally, follow instructions described in the [`module.ivy`](./module.ivy) file to configure the target VEE Port directory.

### Setup Specific Options

Before going further, your VEE Port must be setup with toolchain specific environment variables and BSP Connection options.
Refer to the VEE Port specific documentation for more details.

# Build the Kernel

Once the VEE Port is configured, you are ready to build the Kernel. 
Right-click on the Kernel project and select `Build Module`.
The build is started and may take few minutes.  

At the end of the build, the `target~/artifacts/` directory should contain the build output:
- `GREEN.out`: the Executable file to be programmed on the Device.
- `GREEN.vde`: the Virtual Device for running Sandboxed Applications on this Kernel.

# Develop Sandboxed Applications

You are now ready to test your Kernel and deploy your first Sandboxed Application.
To do so follow the [Get Started with Multi-Sandbox for STM32F7508-DK Discovery Kit](https://developer.microej.com/stm32f7508-dk-discovery-kit-get-started-multi-sandbox/) guide using your own generated Executable and Virtual Device files.

You will learn how to:
- create a first Sandboxed Application,
- run a Sandboxed Application on the Virtual Device,
- run the same Sandboxed Application on your real device. . 

Instructions may apply to any VEE Port, except for those related to the deployment of the Multi-Sandbox Firmware which are specific the STM32F7508-DK Discovery kit.
When working with a different target device, you will need to adjust these instructions accordingly.

# Going further

By now, you should have completed the initial functional Kernel. 
Before proceeding with any customization, please consult the [Kernel Developer Guide](https://docs.microej.com/en/latest/KernelDeveloperGuide/index.html).

If you wish to understand the core Multi-Sandboxing mechanisms, you can refer to the following resources:

- The [Kernel & Features specification](https://docs.microej.com/en/latest/KernelDeveloperGuide/kf.html) which provides a detailed explanation of core concepts.
- The [ej.kf.Kernel class](https://repository.microej.com/javadoc/microej_5.x/apis/ej/kf/Kernel.html) which offers the complete Javadoc API.


---
_Copyright 2021-2023 MicroEJ Corp. All rights reserved._  
_Use of this source code is governed by a BSD-style license that can be found with this software._

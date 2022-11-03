![](https://shields.microej.com/endpoint?url=https://repository.microej.com/packages/badges/sdk_5.4.json)
![](https://shields.microej.com/endpoint?url=https://repository.microej.com/packages/badges/arch_7.16.json)
![](https://shields.microej.com/endpoint?url=https://repository.microej.com/packages/badges/gui_3.json)

# Green Firmware


# Overview

Green firmware is a [Multi-sandboxed evaluation firmware](https://docs.microej.com/en/latest/KernelDeveloperGuide/overview.html#multi-sandbox-build-flow).

> NOTE:  
> The current project shows errors trying to compile in IDE as by default it loads the platform remotely using properties not yet supported by IVYDE.
> However the build module will work as easyant override this property during its build.
> You can also load the plaform locally to remove errors.

### Kernel APIs

In order for the kernel to specify which methods and static fields features can use, the kernel needs to add Kernel APIs as dependencies in its module.ivy.
The Kernel API module also allow to specify types to use as they can be different between kernel and features.
You can find further informations about Kernel APIS in the [kernel developer guide](https://docs.microej.com/en/latest/KernelDeveloperGuide/kernelAPI.html).

Here's the list of Kernel APIs available in the Green Firmware: 

- EDC
- BON
- KF
- NET
- SSL
- BasicTool
- Wadapps
- Storage
- Service
- MicroUI
- MWT
- Drawing
- Logging
- Connectivity
- Property
- Trace

### System apps

The Green firmware embeds the following system apps:

- CommandServer-Socket
- Network Time Protocol (NTP)

### Dynamic management of features

Features are dynamically managed on the board at runtime.

#### Kernel start
At kernel start up, each application stored in a specific folder as fo file is installed and started one at a time.


#### Management during runtime
Applications are sent to the board as a .fo file and written in its filesystem then the kernel loads and installs this application from this file.

The default behaviour of a Kernel is to directly install the application in RAM memory but the firmware Green does explicitely specify that any received application should be persistent and therefore stored in file system.
Once the kernel has copied the .fo file to its file system, it installs and starts the application.


### Security Policy

As the Green firmware is an evaluation firmware, its security policy is different from usual use case. The Green firmware will only disallow the stop or uninstall of any resident application and leave any other action allowed.

# Usage

## Platform load

By default the firmware will download the platform from the MicroEJ Central Repository during the build.
You can however use a local platform by following these steps: 
> Make sure to use a [Multi-Sandbox](https://docs.microej.com/en/latest/PlatformDeveloperGuide/multiSandbox.html) platform otherwise an error will occure during the build saying that some files are missing.
1. In the module.ivy file, uncomment the ``<ea:property name="platform-loader.target.platform.dir" value="" />`` property and set the value to the path of the platform ``/source`` folder
2. In the module.ivy file, comment the default platform dependency (initally located at the end of dependencies)

## Build the firmware binaries

In the ``module.ivy`` file you can either choose to load the platform remotely or to load it locally.
Once you have configured your platform import:
1. Right Click on the project
2. Select ``Build Module``
3. Wait until the end of the build (it may take few minutes)

Once the build is completed, you can find the .out and .vde files in ``/target~/artifacts/``

## Import the virtual device

1. Click ``File``
2. Select ``Import``
3. In the Import window, open ``MicroEJ`` wizard
4. Select ``Virtual Devices``
5. Click ``Next >``
6. Check ``Select directory``
7. Browse to ``com.microej.firmware.developer.green\target~\artifacts``
8. Click ``Select Folder``
9. You should see the ``VDE-green`` appear in the ``Target:`` list make sure it is selected
10. Accept the MicroEJ Studio license agreement
11. Click ``Finish``

The virtual device has been imported to your workspace.

## Run an application on simulator

>Note: a sandboxed application does have a different entry point than standalone applications (i.e BackgroundService or FeatureEntryPoint class).
>Please refer to https://docs.microej.com/en/latest/SDKUserGuide/sdkMigrationNotes.html?highlight=wadapps#wadapps-application-update to migrate a standalone application to a sandboxed application.

1. Right Click on the project you want to run
2. Select ``Run as -> MicroEJ Application``
3. Select your application entry point, click ``Ok``
4. Select the virtual device (.vde) you built, click ``Ok``

The application will start building on the simulator.

## Run an application on board

The GREEN firmware is a multi-sandbox firmware, the CommandServer-Socket allows the user to deploy application using sockets on a local network.
The board will need an internet connection, then the ``CONNECTIVITY`` api from the MicroEJ-Developer Runtime Environment will automatically assign an ip address to the board using DHCP.


1. Right Click on the project you want to run (You can directly go to step 5 if you have already launched the application once on simulator)
2. Select ``Run as -> MicroEJ Application``
3. Select your application entry point, click ``Ok``
4. Select the platform (.vde) you built, click ``Ok``

The application will start building on the simulator and create a run configuration.

5. Click ``Run -> Run Configurations...``
6. Select the run configuration of your project
7. Go the the ``Execution`` tab
8. In the ``Execution`` section check ``Execute on device`` then in the ``Settings`` list, select ``Local Deployment (Socket)``
9. Go to the ``Configuration`` tab
10. Click ``Local Deployment (Socket)``
11. In the ``Host`` field, insert the board's ip (the ip can be found in the standard output of the board)
12. Click ``Apply`` then ``Run``

The application will build and then be automatically deployed on the board using sockets.

# Requirements

N/A.

# Dependencies

_All dependencies are retrieved transitively by MicroEJ Module Manager_.

# Source

N/A.

# Restrictions

None.

# Troubleshooting

Network latencies or stability inconsistencies can cause issues during the deployment, if you encounter an error during the deployment, try building the application again.

---
_Copyright 2021-2022 MicroEJ Corp. All rights reserved._  
_Use of this source code is governed by a BSD-style license that can be found with this software._

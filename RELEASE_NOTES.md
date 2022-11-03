# Overview

The Green Firmware is a MicroEJ [Multi-Sandbox Firmware](https://docs.microej.com/en/latest/KernelDeveloperGuide/overview.html#multi-sandbox-build-flow) that allows the embedded device to run and manage multiple Sandboxed Applications simultaneously for a dedicated board.
MICROEJ SDK allows application developers to write Java applications and run them on a virtual (simulated) or real device.

The Green Firmware has the capability to locally deploy applications created with MICROEJ SDK.

Visit [https://docs.microej.com/](https://docs.microej.com/en/latest/ApplicationDeveloperGuide/index.html) for more information about Sandboxed Applications, Virtual Devices, Firmware, and demos.

## Supported Boards

- STMicroelectronics: [STM32F7508-DK](https://www.st.com/en/evaluation-tools/stm32f7508-dk.html)

# Features
## Foundations Libraries

This firmware implements the following libraries from MicroEJ:

- EDC 1.3
- BON 1.4
- KF 1.5
- NET 1.1
- SSL 2.2
- MicroUI 3.1
- DRAWING 1.0
- FS 2.0

## Included Add-on Java Libraries

- MWT 3.3
- WADAPPS 2.2

## Preinstalled MicroEJ Resident Applications

- NTP (Network time protocol): Synchronizes the time of the device.
- CommandServer-Socket: Allows the deployment of MicroEJ Applications through a local network connection.

## MicroEJ SDK Workbench and Extensions

The Green virtual device embeds the following tools:

- wadapps-localdeploy-socket-extension: Allows the user to select an application project, build, and deploy it to the device over the network.
- wadapps-localdeploy-wpk-socket-extension: Allows the user to select a WPK (Wadapps Package Kit) application and deploy it to the device over the network.
- wadapps-console-socket-extension: Allows to start the Wadapps Admin Console over Socket to manage the firmware.
- wadapps-localdeploy-resident-launcher: Allows the user to select an application project and integrate it into an existing firmware as a resident application.
- wadapps-firmware-customizer-extension: Allows the user to select a WPK application and integrate it into an existing firmware as a resident application.
- application-repository-extension: Allows the user to load WPK applications in simulation to emulate embedded behavior. 

## Security policy

The Green Firmware has a custom security policy for its evaluation purpose; any action is allowed apart from stopping or uninstalling resident applications.

# Firmware API Javadoc

Javadoc can be viewed in MicroEJ SDK Resource Center view by going to ``Help`` > ``MicroEJ Resource Center`` > ``Javadoc`` (after the import of the corresponding virtual device).

# Demos

- Demo applications and getting started are available at the [MicroEJ Developer website](https://developer.microej.com/get-started/).

# Changelog

## [0.1.0] - Unreleased

### Added

- Developer firmware.
- Wadapps2 support.
- MicroUI3 support.
- CommandServer-Socket system application.
- NTP system application.
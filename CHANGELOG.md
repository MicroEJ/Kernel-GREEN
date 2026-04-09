# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.3.2] - 2026-04-09

### Removed

- `repositories` block declaring `mavenCentral` in `build.gradle.kts`.

## [2.3.1] - 2025-10-15

### Changed

- Revert ``ej.library.ui:mwt`` to version ``3.3.0``.
- Revert ``com.microej.kernelapi:mwt`` to version ``2.2.0``.

## [2.3.0] - 2025-10-14

### Added

- Feature policy management to control resource limitations (CPU, RAM, flash, network) and priority.
- NET/FS hooks to enforce flash and bandwidth limits via AspectJ.
- Property to avoid rebuilding kernel when building features.

### Changed

- Update ``AppConnect`` to version ``3.2.0``.
- Update ``microej-sdk`` to version ``1.4.0``.
- Update ``ej.api:edc`` to version ``1.3.7``.
- Update ``ej.api:bon`` to version ``1.4.4``.
- Update ``ej.api:net`` to version ``1.1.4``.
- Update ``ej.api:ssl`` to version ``2.2.3``.
- Update ``ej.api:microui`` to version ``3.6.0``.
- Update ``ej.library.ui:mwt`` to version ``3.6.1``.
- Update ``ej.library.runtime:basictool`` to version ``1.8.0``.
- Update ``com.microej.library.util:kf-util`` to version ``3.1.2``.
- Update ``ej.library.iot:net-util`` to version ``1.4.0``.
- Update ``com.microej.kernelapi:edc`` to version ``1.2.0``.
- Update ``com.microej.kernelapi:bon`` to version ``1.4.0``.
- Update ``ej.library.iot:net-util`` to version ``1.4.0``.
- Update ``com.microej.kernelapi:kf`` to version ``2.2.0``.
- Update ``com.microej.kernelapi:net`` to version ``1.3.0``.
- Update ``com.microej.kernelapi:ssl`` to version ``1.3.0``.
- Update ``com.microej.kernelapi:storage`` to version ``1.3.0``.
- Update ``com.microej.kernelapi:microui`` to version ``3.6.0``.
- Update ``com.nxp.vee.mimxrt1170_mapps`` to version ``3.1.0``.
- Update ``com.microej.platform.i386`` to version ``2.2.0``.

### Fixed

- NullPointerException when using resource monitoring on simulator.

### Removed

- Remove support for STM32F7508-DK.
- Remove Ivy descriptor workaround (already supported by MicroEJ SDK >= ``1.2.0``).

## [2.2.0] - 2025-03-31

### Changed

- Renamed Health Monitoring to Resource Monitoring.

### Removed

- Moved Resource Monitoring to the KF-Util library.
 
## [2.1.1] - 2025-03-27

### Fixed

- Missing files in published artifact

## [2.1.0] - 2025-03-19

### Changed

- Rename ``Supported VEE Port`` to ``Requirements`` in the ``README`` and move this section at the beginning of the document.
- Update RT1170 VEE Port to version 3.0.0
- Update gradle plugin to 1.1.0

### Fixed

- Resolved an issue in the Storage service where the kernel instance was shared across apps. A sandboxed instance is now
  created for each app, ensuring isolation and improved security.

### Added

- Add ``App Connect`` web application, allowing to manage applications lifecycle through a Web UI or an HTTP REST API.
- Health monitoring (CPU & RAM)
- `KernelUncaughtExceptionHandler` to manage misbehaving apps, crashing apps are now automatically uninstalled.

### Removed

- Legacy Command Server, replaced by ``App Connect``, see the [Deploy an application](./README.md#deploy-an-application)
  section for more details.

## [2.0.0] - 2024-10-29

### Changed

- Migrated the project to [SDK6](https://docs.microej.com/en/latest/SDK6UserGuide/index.html) using a Gradle-based build
  system. **Note:** This version is incompatible with SDK5 and Ivy; use previous versions if needed.
- Update ``com.microej.library.wadapps#connectivity:2.0.0`` to ``com.microej.library.kf#connectivity:2.1.0``. a more
  lightweight version with less transitive dependencies.

## [1.4.0] - 2024-08-07

### Changed

- Upgrade `com.microej.library.util#kf-util` to version `2.8.0`.

### Added

- Add a new security manager mode to use resource policy file from applications.
- Add a section in the ``README`` file for Kernel and reference VEE Ports compatibility.
- Add a section in the ``README`` file for troubleshooting.

## [1.3.1] - 2024-02-29

### Changed

- Upgrade `com.microej.library.util#kf-util` to version `2.7.1`.

## [1.3.0] - 2024-02-16

### Changed

- Add VEE Port for NXP i.MX RT1170 EVK to reference VEE Ports.

## [1.2.0] - 2023-10-27

### Added

- Add logging-only SecurityManager.
- Add kernel.intern file for NET module usage.

### Changed

- Update the service, property and kf-util dependencies and usages.
- Update documentation.
- Change the default way of fetching a VEE Port (switch from remote module dependency to local source)

## [1.1.0] - 2023-09-13

### Added

- Add a network state listener to log for network state changes.
- Add a feature state listener to log for feature state changes.
- Add implementation of StorageFS over KF in the Kernel project.

### Changed

- Update the project architecture
- Change the command-server-socket from system-app to be directly embedded into the kernel.
- Change the NTP from system-app to be directly embedded into the kernel.
- Update the package and project name.

### Removed

- Remove wadapps dependencies (partial).
- Remove admin-console-socket-extension.
- Remove localdeploy-wpk-socket-extension.

## [1.0.1] - 2022-11-03

### Changed

- Update RELEASE_NOTES.md to remove outdated section.

## [1.0.0] - 2022-11-03

### Added

- Developer firmware.
- Wadapps2 support.
- MicroUI3 support.
- CommandServer-Socket system application.
- NTP system application.

---

_Copyright 2021-2026 MicroEJ Corp. All rights reserved._
_Use of this source code is governed by a BSD-style license that can be found with this software._

# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
_Copyright 2021-2024 MicroEJ Corp. All rights reserved._  
_Use of this source code is governed by a BSD-style license that can be found with this software._  
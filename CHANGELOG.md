# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


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
_Copyright 2021-2023 MicroEJ Corp. All rights reserved._  
_Use of this source code is governed by a BSD-style license that can be found with this software._  
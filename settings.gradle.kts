/*
 * Copyright 2023-2024 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
var defaultName: String = "GREEN"
rootProject.name = providers.systemProperty("artifact.name").getOrElse(defaultName)


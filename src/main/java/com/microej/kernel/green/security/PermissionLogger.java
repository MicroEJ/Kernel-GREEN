/*
 * Java
 *
 * Copyright 2023 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green.security;

import java.security.Permission;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microej.kf.util.security.FeaturePermissionCheckDelegate;

import ej.kf.Feature;

/**
 * An example logging-only {@link FeaturePermissionCheckDelegate} implementation granting all permissions.
 */
public class PermissionLogger implements FeaturePermissionCheckDelegate {

	private final Logger logger;
	private final Level logLevel;

	/**
	 * Construct.
	 *
	 * @param logger
	 *            Logger.
	 * @param logLevel
	 *            Log level.
	 */
	public PermissionLogger(final Logger logger, final Level logLevel) {
		this.logger = logger;
		this.logLevel = logLevel;
	}

	@Override
	public void checkPermission(final Permission permission, final Feature feature) {
		final String permissionClassName = permission.getClass().getName();
		final String permissionName = permission.getName();

		this.logger.log(this.logLevel, "Granted permission '" + permissionClassName + "' with action '" + permissionName
				+ "' for feature '" + feature.getName() + "'");
	}

}

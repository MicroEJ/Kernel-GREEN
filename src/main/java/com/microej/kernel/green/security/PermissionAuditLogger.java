/*
 * Java
 *
 * Copyright 2023-2024 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green.security;

import java.security.Permission;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microej.kf.util.security.FeaturePermissionCheckDelegate;

import ej.kf.Feature;

/**
 * A logging-only implementation of {@link FeaturePermissionCheckDelegate} that grants all permissions. This
 * implementation records permission checks, allowing detailed tracking of actions granted to specified features within
 * the application.
 *
 * <p>
 * Note: This class is meant for logging purposes and does not enforce any actual permission restrictions.
 * </p>
 */
public class PermissionAuditLogger implements FeaturePermissionCheckDelegate {

	private static final Logger LOGGER = Logger.getLogger("PermissionAuditLogger");

	/**
	 * Logs the permission granted to a specific feature.
	 *
	 * @param permission
	 *            the {@link Permission} requested by the app
	 * @param app
	 *            the {@link Feature} "app" that is requesting the permission
	 * 
	 */
	@Override
	public void checkPermission(final Permission permission, final Feature app) {

		final String resource = permission.getClass().getName();
		final String actionOnResource = permission.getName();
		final String appName = app.getName();

		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.log(Level.FINE, "Permission granted: action '" + actionOnResource + "' on resource '" + resource
					+ "' for application '" + appName + "'.");
		}
	}
}

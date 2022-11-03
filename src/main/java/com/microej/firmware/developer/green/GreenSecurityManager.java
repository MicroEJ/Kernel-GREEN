/*
 * Java
 *
 * Copyright 2021-2022 MicroEJ Corp. MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.firmware.developer.green;

import java.security.Permission;

import ej.basictool.ArrayTools;
import ej.kf.Kernel;
import ej.wadapps.application.Application;
import ej.wadapps.application.ApplicationPermission;

/**
 * An implementation of a {@link GreenSecurityManager} which disallow stopping or uninstalling resident applications.
 */
public class GreenSecurityManager extends SecurityManager {

	private final Application[] residentApplications;

	/**
	 * Creates an instance of this {@link GreenSecurityManager}
	 *
	 * @param residentApplications
	 *            the list of application which are resident applications.
	 */
	public GreenSecurityManager(Application[] residentApplications) {
		this.residentApplications = residentApplications;
	}

	@Override
	public void checkPermission(Permission perm) {
		if (Kernel.isInKernelMode()) {
			// Kernel has all the rights: no checks
			return;
		}

		Kernel.enter();
		if (perm instanceof ApplicationPermission) {
			ApplicationPermission appPerm = (ApplicationPermission) perm;
			String action = appPerm.getActions();
			Application app = appPerm.getApplication();
			boolean stopOrUninstallAction = action.equals(ApplicationPermission.STOP_ACTION)
					|| action.equals(ApplicationPermission.UNINSTALL_ACTION);
			boolean disallow = app == null || ArrayTools.containsEquals(this.residentApplications, app);
			if (stopOrUninstallAction && disallow) {
				throw new SecurityException();
			}
		} else {
			// allow all other perms
		}
	}

}

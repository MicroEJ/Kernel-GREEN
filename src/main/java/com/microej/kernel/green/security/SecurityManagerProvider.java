/*
 * Java
 *
 * Copyright 2024 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green.security;

import java.io.FilePermission;
import java.net.NetPermission;
import java.net.SocketPermission;
import java.util.PropertyPermission;

import javax.net.ssl.SSLPermission;

import com.microej.kf.util.security.KernelSecurityManager;
import com.microej.kf.util.security.KernelSecurityPolicyManager;
import com.microej.kf.util.security.SecurityPolicyResourceLoader;

import ej.microui.MicroUIPermission;
import ej.microui.display.DisplayPermission;
import ej.microui.display.FontPermission;
import ej.microui.display.ImagePermission;
import ej.microui.event.EventPermission;
import ej.service.ServiceFactory;
import ej.service.ServicePermission;

/**
 * Provides a {@link SecurityManager} for the application, configured based on the specified security mode. The security
 * manager can operate in two modes:
 * <ul>
 * <li><b>LOGGING:</b> Logs all permission checks without enforcing them.</li>
 * <li><b>POLICY_FILE:</b> Enforces permissions as defined in the security policy file.</li>
 * </ul>
 *
 * <p>
 * To enable permission checks, the security management capability must be enabled. Refer to
 * <a href="https://docs.microej.com/en/latest/KernelDeveloperGuide/kernelCreation.html#implement-a-security-policy">
 * Implement Security Policy</a> for more details.
 * </p>
 */
public class SecurityManagerProvider {

	private static final String SECURITY_MANAGER_POLICY_FILE_MODE = "POLICY_FILE";

	// Private constructor to prevent instantiation
	private SecurityManagerProvider() {
	}

	/**
	 * 
	 * @param securityManagerMode
	 *            security Manager Mode
	 * 
	 * @return an instance of {@link SecurityManager} configured according to the application properties.
	 * 
	 */
	public static SecurityManager get(String securityManagerMode) {
		if (SECURITY_MANAGER_POLICY_FILE_MODE.equals(securityManagerMode)) {
			return newFilePolicySecurityManager();
		} else {
			return newLoggingSecurityManager();
		}
	}

	/**
	 * Creates a custom {@link SecurityManager} that logs all permission requests without enforcing them. This
	 * configuration is useful for debugging and monitoring permissions during development, as it grants all permissions
	 * but records each permission check in the log for inspection.
	 *
	 * <p>
	 * <b>Usage:</b> This configuration does not restrict any permissions; it only logs access attempts. Use it in
	 * development or testing environments where you want to monitor permission requests without enforcing strict access
	 * controls.
	 * </p>
	 *
	 * @return a {@link SecurityManager} configured to log permission checks without enforcing them.
	 */
	private static SecurityManager newLoggingSecurityManager() {
		// Instantiate the KernelSecurityManager that will handle logging of permission checks.
		KernelSecurityManager securityManager = new KernelSecurityManager();

		// Create an instance of PermissionLogger to handle and log each permission check.
		// This logger will record the permissions checked without denying or allowing them explicitly.
		PermissionAuditLogger permissionLogger = new PermissionAuditLogger();

		// Register each specific permission type with the PermissionLogger. Each entry here defines
		// a permission type that will be logged rather than enforced, enabling insight into application
		// behavior regarding resource access without strict security constraints.
		securityManager.setFeaturePermissionDelegate(DisplayPermission.class, permissionLogger);
		securityManager.setFeaturePermissionDelegate(EventPermission.class, permissionLogger);
		securityManager.setFeaturePermissionDelegate(FilePermission.class, permissionLogger);
		securityManager.setFeaturePermissionDelegate(FontPermission.class, permissionLogger);
		securityManager.setFeaturePermissionDelegate(ImagePermission.class, permissionLogger);
		securityManager.setFeaturePermissionDelegate(MicroUIPermission.class, permissionLogger);
		securityManager.setFeaturePermissionDelegate(NetPermission.class, permissionLogger);
		securityManager.setFeaturePermissionDelegate(ej.property.PropertyPermission.class, permissionLogger);
		securityManager.setFeaturePermissionDelegate(PropertyPermission.class, permissionLogger);
		securityManager.setFeaturePermissionDelegate(RuntimePermission.class, permissionLogger);
		securityManager.setFeaturePermissionDelegate(ServicePermission.class, permissionLogger);
		securityManager.setFeaturePermissionDelegate(SocketPermission.class, permissionLogger);
		securityManager.setFeaturePermissionDelegate(SSLPermission.class, permissionLogger);

		// Return the configured SecurityManager instance for use.
		return securityManager;
	}

	/**
	 * Creates a {@link SecurityManager} that enforces permissions based on a predefined security policy file. This
	 * configuration loads permissions for all active apps at kernel startup, providing an enforceable permission model
	 * based on the specified security policies.
	 *
	 * <p>
	 * <b>Usage:</b> Ensure that a valid security policy file is configured and that the necessary dependencies, such as
	 * {@link SecurityPolicyResourceLoader}, are properly set up in your environment.
	 * </p>
	 *
	 * @return an instance of {@link SecurityManager} that enforces feature-based permissions as defined in the security
	 *         policy file.
	 */
	private static SecurityManager newFilePolicySecurityManager() {

		// Retrieve the necessary service to load the security policy resources.
		SecurityPolicyResourceLoader securityPolicyResourceLoader = ServiceFactory
				.getRequiredService(SecurityPolicyResourceLoader.class);

		// Create an instance of KernelSecurityPolicyManager with the specified resource loader.
		// This manager handles the mapping of permissions to apps based on the policy file provided by the app.
		return new KernelSecurityPolicyManager(securityPolicyResourceLoader);
	}

}

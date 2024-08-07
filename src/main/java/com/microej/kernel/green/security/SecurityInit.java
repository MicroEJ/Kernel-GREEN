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
import java.util.logging.Level;

import javax.net.ssl.SSLPermission;

import com.microej.kernel.green.Main;
import com.microej.kf.util.security.KernelSecurityManager;
import com.microej.kf.util.security.KernelSecurityPolicyManager;
import com.microej.kf.util.security.SecurityPolicyResourceLoader;

import ej.kf.Feature;
import ej.kf.Kernel;
import ej.microui.MicroUIPermission;
import ej.microui.display.DisplayPermission;
import ej.microui.display.FontPermission;
import ej.microui.display.ImagePermission;
import ej.microui.event.EventPermission;
import ej.service.ServiceFactory;
import ej.service.ServicePermission;

/**
 * Class to handle Security initialization.
 */
public class SecurityInit {
	private static final String SECURITY_MANAGER_LOGGING_MODE = "LOGGING";
	private static final String SECURITY_MANAGER_POLICY_FILE_MODE = "POLICY_FILE";
	private static final String SECURITY_MANAGER_MODE_PROPERTY = "security.manager.mode";

	private SecurityInit() {
		// disable the public constructor
	}

	/**
	 * This method set the security management policy based on the chosen mode in the security.properties.list file
	 */
	public static void initSecurityManager() {

		Main.LOGGER.info("Initializing custom Security Manager");

		// Retrieve the security manager mode from System properties.
		// Default and fallback mode are LOGGING.
		// To change the security manager mode, please refer to the Security Management section in the README.
		String securityManagerMode = System.getProperty(SECURITY_MANAGER_MODE_PROPERTY, SECURITY_MANAGER_LOGGING_MODE);

		// Check the security manager mode.
		// If the security manager mode is not recognised, fallback to LOGGING mode.
		if (securityManagerMode.equals(SECURITY_MANAGER_LOGGING_MODE)) {
			// Log only if the logger level if info or above
			if (Main.LOGGER.isLoggable(Level.INFO)) {
				Main.LOGGER.log(Level.INFO, securityManagerMode + " security management mode detected"); // NOSONAR
			}
			registerLoggingSecurityManager();
		} else if (securityManagerMode.equals(SECURITY_MANAGER_POLICY_FILE_MODE)) {
			// Log only if the logger level if info or above
			if (Main.LOGGER.isLoggable(Level.INFO)) {
				Main.LOGGER.log(Level.INFO, securityManagerMode + " security management mode detected"); // NOSONAR
			}
			registerFilePolicySecurityManager();
		} else {
			Main.LOGGER.info("Security manager mode not recognised, falling back to LOGGING mode");
			registerLoggingSecurityManager();
		}
	}

	/**
	 * Creates and registers a custom {@link SecurityManager} that will log all permission requests.
	 */
	private static void registerFilePolicySecurityManager() {
		// For the permission checks to occur the security management capability must be enabled first
		// See https://docs.microej.com/en/latest/KernelDeveloperGuide/kernelCreation.html#implement-a-security-policy

		// Retrieve the JSON implementation necessary for the Security manager to be able to parse feature policy files.
		final SecurityPolicyResourceLoader securityPolicyResourceLoader = ServiceFactory
				.getRequiredService(SecurityPolicyResourceLoader.class);

		// Create the KernelSecurityPolicyManager class using the JSON resource loader
		KernelSecurityPolicyManager kernelSecurityPolicyManager = new KernelSecurityPolicyManager(
				securityPolicyResourceLoader);

		System.setSecurityManager(kernelSecurityPolicyManager); // NOSONAR

		// Load feature's permission for loaded features at kernel start (i.e Simulator or XIP)
		for (Feature feature : Kernel.getAllLoadedFeatures()) {
			kernelSecurityPolicyManager.addToPermissionMap(feature);
		}

	}

	/**
	 * Create and register a custom {@link SecurityManager} that will log all permission requests.
	 */
	private static void registerLoggingSecurityManager() {

		// For the permission checks to occur the security management capability must be enabled first
		// See https://docs.microej.com/en/latest/KernelDeveloperGuide/kernelCreation.html#implement-a-security-policy

		// Instantiate the SecurityManager object to be registered
		final KernelSecurityManager securityManager = new KernelSecurityManager();

		// Instantiate the FeaturePermissionCheckDelegate object that will handle the actual permission checks
		// This implementation always grants the permission being checked and logs the event with the specified logger
		// and log level
		final PermissionLogger permissionLogger = new PermissionLogger(Main.LOGGER, java.util.logging.Level.INFO);

		// Register the instantiated FeaturePermissionCheckDelegate for checking all the supported permissions
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

		// Register the instantiated SecurityManager object as the system-wide security manager
		System.setSecurityManager(securityManager); // NOSONAR

	}
}

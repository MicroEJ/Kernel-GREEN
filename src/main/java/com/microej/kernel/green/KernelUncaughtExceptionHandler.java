/*
 * Java
 *
 * Copyright 2024 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.microej.kf.util.KernelSupport;

import ej.kf.Feature;
import ej.kf.Kernel;
import ej.kf.Module;

/**
 * Handles uncaught exceptions and manages the lifecycle of applications causing them.
 */
public class KernelUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static final Logger LOGGER = Logger.getLogger("UncaughtExceptionHandler");

	@Override
	public void uncaughtException(Thread thread, Throwable error) {
		Module module = Kernel.getOwner(thread);
		if (module instanceof Kernel) {
			throw new IllegalStateException("Unhandled exception in Kernel.", error);
		}

		if (LOGGER.isLoggable(Level.SEVERE)) {
			LOGGER.log(Level.SEVERE, "uncaught exception in app " + module.getName() + ":" + module.getVersion(),
					error);
		}

		// Perform cleanup in a separate thread.
		// Stopping the app directly here retains references to the thread and error arguments from the app,
		Thread workerThread = new Thread(new RemoveAppTask(module), "UninstallTask-" + module.getName());
		workerThread.setDaemon(true);
		workerThread.start();
	}

	public static class RemoveAppTask implements Runnable {

		private static final int APP_STOP_TIMEOUT_MS = 30_000;

		private final Module module;

		public RemoveAppTask(Module module) {
			this.module = module;
		}

		@Override
		public void run() {
			final Feature app = (Feature) module;
			if (stop(app)) {
				uninstall(app);
			}
		}

		private boolean stop(Feature app) {
			if (KernelSupport.stopFeature(app, APP_STOP_TIMEOUT_MS)) {
				return true;
			}
			if (LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, "Cannot stop app '" + app.getName() + "', check stale kernel references");
			}
			return false;
		}

		private void uninstall(Feature app) {
			try {
				Kernel.uninstall(app);
			} catch (Exception e) {
				if (LOGGER.isLoggable(Level.SEVERE)) {
					LOGGER.log(Level.SEVERE, "Cannot uninstall app '" + app.getName() + "', cause: " + e.getMessage());
				}
			}
		}
	}
}

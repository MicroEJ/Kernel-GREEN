/*
 * Java
 *
 * Copyright 2021-2025 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microej.kernel.green.gui.BlackScreenDisplayable;
import com.microej.kernel.green.monitoring.MonitoringLoggerTimerTask;
import com.microej.kernel.green.net.OnInternetStateChanged;
import com.microej.kernel.green.net.OnNetworkStateChanged;
import com.microej.kernel.green.security.SecurityManagerProvider;
import com.microej.kf.connectivity.ConnectivityManagerKF;
import com.microej.kf.util.BooleanConverter;
import com.microej.kf.util.ByteConverter;
import com.microej.kf.util.CharacterConverter;
import com.microej.kf.util.DateConverter;
import com.microej.kf.util.DoubleConverter;
import com.microej.kf.util.FloatConverter;
import com.microej.kf.util.IProgressMonitorConverter;
import com.microej.kf.util.InputStreamConverter;
import com.microej.kf.util.IntegerConverter;
import com.microej.kf.util.ListConverter;
import com.microej.kf.util.LongConverter;
import com.microej.kf.util.MapConverter;
import com.microej.kf.util.RunnableWithResult;
import com.microej.kf.util.ShortConverter;
import com.microej.kf.util.StringConverter;
import com.microej.kf.util.monitoring.ResourceMonitoringService;
import com.microej.kf.util.monitoring.ResourceMonitoringServiceImpl;
import com.microej.kf.util.service.ServiceRegistryKF;
import com.microej.library.appconnect.http.AppConnectServer;

import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import ej.annotation.Nullable;
import ej.bon.Timer;
import ej.kf.AlreadyLoadedFeatureException;
import ej.kf.Feature;
import ej.kf.Feature.State;
import ej.kf.FeatureStateListener;
import ej.kf.IncompatibleFeatureException;
import ej.kf.InvalidFormatException;
import ej.kf.Kernel;
import ej.microui.MicroUI;
import ej.microui.display.Display;
import ej.net.HttpPollerConnectivityManager;
import ej.net.util.connectivity.SimpleNetworkCallbackAdapter;
import ej.service.ServiceFactory;
import ej.storage.Storage;

/**
 * Kernel Entry Point Class
 */
public class Main {

	private static final Logger LOGGER = Logger.getLogger("Main");
	private static final String SECURITY_MANAGER_ENABLED_PROPERTY = "security.manager.enabled";
	private static final String SECURITY_MANAGER_MODE_PROPERTY = "security.manager.mode";
	private static final String APP_STORAGE_PREFIX = "app_";
	private static final String APP_STORAGE_SUFFIX = ".fo";

	/**
	 * Kernel Entry Point
	 *
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new KernelUncaughtExceptionHandler());
		initializeKernel();
		setupGui();
		setupSecurityManager();
		setupTimerService();
		setupConnectivityManagerService();
		registerNetworkStateCallback();
		registerInternetConnectivityCallback();
		installApplications();
		startApplications();

		startResourceMonitoring();
		startAppConnect();
	}

	/**
	 * Initialize The Kernel Instance
	 */
	private static void initializeKernel() {
		final Kernel kernel = Kernel.getInstance();
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Starting Kernel " + kernel.getName() + " - " + kernel.getVersion());
		}

		// -----------------------
		// Actions on Apps State Changes
		// Register a listener to monitor application state changes: Start, Stop, Install, Uninstall
		// -----------------------
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Registering Application State Change Listener");
		}
		Kernel.addFeatureStateListener(new OnAppStateChanged());

		// -----------------------
		// Shared Interface Converters
		// ------------------------
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Registering Shared Interface Converters");
		}
		// Register kernel converters for Shared Interface communication between apps,
		// for more information, see the documentation:
		// https://docs.microej.com/en/latest/KernelDeveloperGuide/featuresCommunication.html#kernel-types-converter
		Kernel.addConverter(new BooleanConverter());
		Kernel.addConverter(new ByteConverter());
		Kernel.addConverter(new CharacterConverter());
		Kernel.addConverter(new DoubleConverter());
		Kernel.addConverter(new FloatConverter());
		Kernel.addConverter(new IntegerConverter());
		Kernel.addConverter(new LongConverter());
		Kernel.addConverter(new ShortConverter());
		Kernel.addConverter(new StringConverter());
		Kernel.addConverter(new InputStreamConverter());
		Kernel.addConverter(new DateConverter());
		Kernel.addConverter(new ListConverter<>());
		Kernel.addConverter(new MapConverter<>());
		Kernel.addConverter(new IProgressMonitorConverter());
	}

	/**
	 * GUI
	 * <p>
	 * Initializes MicroUI and displays a black screen until an application requests display access.
	 */
	private static void setupGui() {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Starting MicroUI (MicroEJ EMBEDDED GUI Framework)");
		}
		MicroUI.start();
		Display.getDisplay().requestShow(new BlackScreenDisplayable());
	}

	private static void setupSecurityManager() {
		if (Boolean.getBoolean(SECURITY_MANAGER_ENABLED_PROPERTY)) {
			String securityManagerMode = System.getProperty(SECURITY_MANAGER_MODE_PROPERTY);

			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("Security Manager is enabled in mode " + securityManagerMode);
			}

			// Instantiate and set the SecurityManager based on the specified mode
			SecurityManager securityManager = SecurityManagerProvider.get(securityManagerMode);
			System.setSecurityManager(securityManager); // NOSONAR: Setting the system security manager is intentional.
		}
	}

	private static void setupTimerService() {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Setup Timer Internal Service");
		}

		// Timer Service
		// for scheduling periodic tasks across the system.
		// This timer is intended for system-wide use, and its cancellation is prohibited.
		// Attempting to cancel this timer will throw an UnsupportedOperationException.
		final Timer timer = new Timer() {
			@Override
			public void cancel() {
				throw new UnsupportedOperationException("This Kernel timer can't be canceled");
			}
		};

		// Register the Timer service with the registry
		// This service is internal to the Kernel and inaccessible to applications
		final ServiceRegistryKF serviceRegistry = (ServiceRegistryKF) ServiceFactory.getServiceRegistry();
		serviceRegistry.register(Timer.class, timer, true);
	}

	private static void setupConnectivityManagerService() {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Setup Connectivity Manager Service");
		}

		final Timer timer = ServiceFactory.getRequiredService(Timer.class);

		// ConnectivityManager Service
		// Set up a poller to regularly check for internet connectivity
		// Initialize ConnectivityManager with HTTP polling to monitor internet access
		// Register a network callback to handle changes in network status
		final HttpPollerConnectivityManager httpPollerConnectivityManager = new HttpPollerConnectivityManager(timer);
		final ConnectivityManager connectivityManager = new ConnectivityManagerKF(httpPollerConnectivityManager);

		// Register the ConnectivityManager service with the registry
		// This service is accessible to applications, allowing them to monitor network state changes
		final ServiceRegistryKF serviceRegistry = (ServiceRegistryKF) ServiceFactory.getServiceRegistry();
		serviceRegistry.register(ConnectivityManager.class, connectivityManager, false);
	}

	/**
	 * Registers a callback to be invoked when the state of a network interface changes.
	 * <p>
	 * The callback will handle events such as the network interface going UP or DOWN.
	 *
	 * @see OnNetworkStateChanged
	 */
	private static void registerNetworkStateCallback() {
		ConnectivityManager connectivityManager = ServiceFactory.getRequiredService(ConnectivityManager.class);
		connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().build(), new OnNetworkStateChanged());
	}

	/**
	 * Registers a callback to monitor changes in Internet connectivity.
	 * <p>
	 * The callback handles events such as gaining or losing access to the Internet.
	 *
	 * @see OnInternetStateChanged
	 */
	private static void registerInternetConnectivityCallback() {
		Timer timer = ServiceFactory.getRequiredService(Timer.class);
		ConnectivityManager connectivityManager = ServiceFactory.getRequiredService(ConnectivityManager.class);
		connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().build(),
				new SimpleNetworkCallbackAdapter(new OnInternetStateChanged(timer)));
	}

	/**
	 * Example of application installation from storage.
	 * <p>
	 * Installs all applications from the root kernel storage. Application files must start with APP_STORAGE_PREFIX and
	 * end with APP_STORAGE_SUFFIX.
	 */
	private static void installApplications() {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("Install application from storage");
		}

		Storage storage = ServiceFactory.getRequiredService(Storage.class);

		try {
			for (String file : storage.getIds()) {
				// Check if the file is an application based on its naming convention
				if (file.startsWith(APP_STORAGE_PREFIX) && file.endsWith(APP_STORAGE_SUFFIX)) {
					installApp(file, storage);
				}
			}
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, "IO error while listing storage files", e);
			}
		}
	}

	private static void installApp(String file, Storage storage) throws IOException {
		try (InputStream inputStream = storage.load(file)) {
			Kernel.install(inputStream);
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine("Successfully installed application from file: " + file);
			}
		} catch (IncompatibleFeatureException | AlreadyLoadedFeatureException | InvalidFormatException e) {
			// Handle exceptions related to incompatible or already loaded features
			// Remove the feature from the storage if it's incompatible with the kernel
			if (LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, "Failed to install application from file: " + file, e);
				LOGGER.severe("Removing incompatible feature from storage: " + file);
			}
			storage.remove(file);
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, "IO error while loading application from file: " + file, e);
			}
		}
	}

	/**
	 * Example of starting a loaded (installed) application.
	 * <p>
	 * When an application is installed, it is in the INSTALLED state. You can retrieve the list of installed
	 * applications using Kernel.getAllLoadedFeatures().
	 */
	private static void startApplications() {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("Starting applications");
		}
		for (Feature app : Kernel.getAllLoadedFeatures()) {
			app.start();
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine("Started application: " + app.getName());
			}
		}
	}

	/**
	 * App Connect Provides a web app for App management: install, start, stop, uninstall.
	 */
	private static void startAppConnect() {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Starting App Connect");
		}

		try {
			AppConnectServer appConnectServer = new AppConnectServer();
			appConnectServer.start();
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, "Error Starting App Connect", e);
			}
		}
	}

	/**
	 * Start Resource Monitoring.
	 * <p>
	 * This feature monitors CPU and RAM usage and provides insights into the resource usage of the application.
	 * <p>
	 * To enable or disable monitoring, modify the `monitoring.check.enabled` property in the configuration file
	 * located at `src/main/resources/kernel.properties.list`.
	 * <p>
	 * The monitoring interval, which determines how often resource usage is checked, can be set using the
	 * `monitoring.check.interval.ms` property in the same configuration file. The value should be specified in
	 * milliseconds (ms) to control the frequency of monitoring.
	 */
	private static void startResourceMonitoring() {
		if (Boolean.getBoolean("monitoring.check.enabled")) {
			long interval = Long.getLong("monitoring.check.interval.ms");
			boolean forceGC = Boolean.getBoolean("monitoring.check.gc.force");
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("Resource Monitoring is enabled, interval: " + interval + "ms , forceGC: " + forceGC);
			}

			final Timer timer = ServiceFactory.getRequiredService(Timer.class);

			// Start monitoring CPU and RAM usage at configured intervals as specified in the properties file.
			final ResourceMonitoringServiceImpl resourceMonitoringService = new ResourceMonitoringServiceImpl(timer, interval, forceGC);
			resourceMonitoringService.start();

			// Log collected CPU usage periodically, with a 1-second delay after data collection.
			final MonitoringLoggerTimerTask healthLoggerTimerTask = new MonitoringLoggerTimerTask(resourceMonitoringService);
			timer.scheduleAtFixedRate(healthLoggerTimerTask, interval + 1000, interval);

			// Register the Resource Monitoring service with the registry
			// This service is internal to the Kernel and inaccessible to applications
			final ServiceRegistryKF serviceRegistry = (ServiceRegistryKF) ServiceFactory.getServiceRegistry();
			serviceRegistry.register(ResourceMonitoringService.class, resourceMonitoringService, true);
		}
	}

	/**
	 * Listener triggered when the application state changes.
	 */
	public static class OnAppStateChanged implements FeatureStateListener {

		@Override
		public void stateChanged(Feature app, @Nullable State previousState) {

			switch (app.getState()) {
			case STARTED:
				onStarted(app);
				break;

			case STOPPED:
				onStopped(app);
				break;

			case INSTALLED:
				if (previousState == null) {
					onInstalled(app);
				} else if (State.STOPPED.equals(previousState)) {
					onStopCompleted(app);
				}
				break;

			case UNINSTALLED:
				onUninstalled(app);
				break;

			default:
				break;
			}
		}

		private void onInstalled(Feature app) {
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("New application installation detected: " + app.getName());
			}
		}

		private void onStarted(Feature app) {
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("Application running: " + app.getName());
			}
		}

		private void onStopped(Feature app) {
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("Application stopped: " + app.getName());
			}
			// Handles the screen state when an application stops.
			// Ensures that if an application stops and no other application or the Kernel has a Displayable,
			// a blank black screen is shown to indicate the absence of active displays.
			if (Display.getDisplay().getDisplayable() == null && !hasRunningAppWithDisplay()) {
				Display.getDisplay().requestShow(new BlackScreenDisplayable());
			}
		}

		private void onStopCompleted(Feature app) {
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("Application completely stopped, and can be uninstalled: " + app.getName());
			}
		}

		private void onUninstalled(Feature app) {
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("Application uninstalled: " + app.getName());
			}
		}

		/**
		 * @return true if an app has a GUI, false otherwise
		 */
		private boolean hasRunningAppWithDisplay() {
			// Iterate over each loaded feature to check if a running app has a displayable component.
			for (Feature app : Kernel.getAllLoadedFeatures()) {

				// Skip apps that are not in the STARTED state.
				if (!State.STARTED.equals(app.getState())) {
					continue;
				}

				// Define a runnable to check if the app has a Displayable object available.
				RunnableWithResult<Boolean> displayableExistsRunnable = new RunnableWithResult<Boolean>() {

					@Override
					protected Boolean runWithResult() {
						// Check if a display is associated with the current app.
						return Display.getDisplay().getDisplayable() != null;
					}
				};

				// Execute the check within the app's context and evaluate the result.
				Kernel.runUnderContext(app, displayableExistsRunnable);
				if (Boolean.TRUE.equals(displayableExistsRunnable.getResult())) {
					return true; // Return true if a displayable is found for any app.
				}
			}

			// Return false if no running app with a Displayable object was found.
			return false;
		}
	}
}

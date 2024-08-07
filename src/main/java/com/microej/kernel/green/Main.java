/*
 * Java
 *
 * Copyright 2021-2024 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microej.kernel.green.gui.GUIManager;
import com.microej.kernel.green.localdeploy.CommandServer;
import com.microej.kernel.green.ntp.NTPService;
import com.microej.kernel.green.security.SecurityInit;
import com.microej.kernel.green.storage.StorageKfFs;
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
import com.microej.kf.util.ShortConverter;
import com.microej.kf.util.StringConverter;
import com.microej.kf.util.service.ServiceRegistryKF;
import com.microej.wadapps.connectivity.ConnectivityManagerKF;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import ej.bon.Timer;
import ej.kf.AlreadyLoadedFeatureException;
import ej.kf.Feature;
import ej.kf.Feature.State;
import ej.kf.FeatureStateListener;
import ej.kf.IncompatibleFeatureException;
import ej.kf.InvalidFormatException;
import ej.kf.Kernel;
import ej.net.HttpPollerConnectivityManager;
import ej.service.ServiceFactory;
import ej.storage.Storage;

/**
 * Main class for the kernel, any code executed in the Kernel is called from this class.
 */
public class Main {

	/** The Constant LOGGER for any log in the Kernel. */
	public static final Logger LOGGER = Logger.getLogger("KERNEL");

	/**
	 * Simple main.
	 *
	 * @param args
	 *            command line arguments
	 * @throws IOException
	 *             in case of an IO error
	 * @throws InvalidFormatException
	 *             in case a @link ej.kf.Feature stored on the storage has an invalid content
	 *
	 */
	public static void main(String[] args) throws IOException, InvalidFormatException {

		LOGGER.info("Kernel startup");

		// Initialize security management policy
		SecurityInit.initSecurityManager();

		// Start MicroUI and show a black screen until an application requests the display
		// Also register a FeatureStateListener to handle the display on feature stop
		GUIManager.initUI();

		// Register official kernel converters, see the documentation:
		// https://docs.microej.com/en/latest/KernelDeveloperGuide/featuresCommunication.html?highlight=converter#kernel-types-converter
		// for more information
		registerConverters();

		// Instantiate classes that will be used as services for features and kernel code
		Storage storage = new StorageKfFs();
		Timer timer = new Timer();
		ConnectivityManager connectivityManager = new ConnectivityManagerKF(new HttpPollerConnectivityManager(timer));

		LOGGER.info("Registering mandatory services");
		// register required services.

		// use generic service factory that register service in local
		ServiceFactory.register(Timer.class, timer);

		// use kf implementation for kernel to allow to specify the registry context
		final ServiceRegistryKF serviceRegistryKF = (ServiceRegistryKF) ServiceFactory.getServiceRegistry();

		// store in local context
		serviceRegistryKF.register(ConnectivityManager.class, connectivityManager, true);

		// store in shared context
		serviceRegistryKF.register(Storage.class, storage, false);

		// register a network connectivity callback that logs available network interfaces
		registerLogConnectivity();

		LOGGER.info("Registering featureStateListener");
		// Create a new FeatureStateListener to trigger actions on feature state changes
		Kernel.addFeatureStateListener(new FeatureStateListener() {

			@Override
			public void stateChanged(Feature feature, State previousState) {

				// Log any feature state change
				StringBuilder sbLogMsg = new StringBuilder();
				State currentState = feature.getState();
				if (previousState != null) {

					sbLogMsg.append("State update: ").append(feature.getName()).append(" (v")
							.append(feature.getVersion()).append(") has changed from state ")
							.append(previousState.name()).append(" to ").append(currentState);

				} else {
					sbLogMsg.append("State update: ").append(feature.getName()).append(" (v")
							.append(feature.getVersion()).append(") has changed from state UNKNOWN to ")
							.append(currentState);
				}

				LOGGER.info(sbLogMsg.toString());
			}
		});

		LOGGER.info("Loading and installing features from the storage (FS)");
		// Load and install all features from the storage
		for (String token : listApplications(storage.getIds())) {
			try (InputStream stream = storage.load(token)) {

				Kernel.install(stream);

			} catch (IncompatibleFeatureException | AlreadyLoadedFeatureException e) {
				// Remove feature from the storage if its not compatible with the kernel
				LOGGER.severe(
						"A feature has been removed from the storage because it was not compatible with the kernel or already loaded ");
				storage.remove(token);
			}
		}

		LOGGER.info("Starting NTP and CommandServer");
		// Start the CommandServer and the NTP services
		new NTPService().start();
		new CommandServer().startServer();

		// Start all features loaded in the kernel
		for (Feature feature : Kernel.getAllLoadedFeatures()) {
			feature.start();
		}
	}

	private static void registerLogConnectivity() {
		NetworkRequest request = new NetworkRequest.Builder().build();
		ConnectivityManager connectivityManager = ServiceFactory.getService(ConnectivityManager.class);

		connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
			@Override
			public void onAvailable(Network network) {
				logNetworkInterfaces();
			}

			@Override
			public void onLost(Network network) {
				logNetworkInterfaces();
			}
		});
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info.isConnected()) {
			logNetworkInterfaces();
		}
	}

	private static String[] listApplications(String[] ids) {
		// gets all the known storage ids
		// filter the list by only keeping app tokens.
		List<String> appStorages = new ArrayList<>();
		for (String id : ids) {
			if (id.startsWith("app_")) {
				appStorages.add(id);
			}
		}
		return appStorages.toArray(new String[appStorages.size()]);
	}

	/**
	 * Log the list of registered {@link NetworkInterface}.
	 */
	private static void logNetworkInterfaces() {
		LOGGER.info("Available Network interfaces:");
		boolean noInterface = true;
		Enumeration<NetworkInterface> interfaces = null;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			if (Main.LOGGER.isLoggable(Level.FINEST)) {
				Main.LOGGER.log(Level.FINEST, e.getMessage(), e); // NOSONAR log if log level is finest or above
			}
			return;
		}

		// No interfaces found
		if (interfaces == null) {
			return;
		}

		while (interfaces.hasMoreElements()) {

			NetworkInterface iface = interfaces.nextElement();

			// Log IP valid addresses from the interface and return true if valid addresses are available
			boolean hasValidIPAdresses = logInterface(iface);

			if (!hasValidIPAdresses) {
				noInterface = hasValidIPAdresses;
			}
		}

		if (noInterface) {
			LOGGER.info("(none)");
		}
	}

	/**
	 * Log interface.
	 *
	 * @param iface
	 *            the network interface
	 * @return true if no valid interface is found, false otherwise
	 */
	private static boolean logInterface(NetworkInterface iface) {
		boolean interfaceIsValid = true;

		// filters out 127.0.0.1 and inactive interfaces
		try {
			if (!iface.isUp() || iface.isLoopback()) {
				interfaceIsValid = false;
			}
		} catch (SocketException e) {
			if (Main.LOGGER.isLoggable(Level.FINEST)) {
				Main.LOGGER.log(Level.FINEST, e.getMessage(), e); // NOSONAR log if log level is finest or above
			}
			interfaceIsValid = false;
		}

		// If the interface is not valid, continue looping
		if (!interfaceIsValid) {
			return true;
		}

		Enumeration<InetAddress> addresses = iface.getInetAddresses();
		while (addresses.hasMoreElements()) {
			InetAddress addr = addresses.nextElement();
			LOGGER.info("- " + addr.getHostAddress());
			return false;
		}

		return true;
	}

	/**
	 * Register Kernel converters.
	 * <p>
	 * If overriding this method, sub-classes MUST this implementation.
	 *
	 * @see Kernel#addConverter(ej.kf.Converter)
	 */
	protected static void registerConverters() {

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
}

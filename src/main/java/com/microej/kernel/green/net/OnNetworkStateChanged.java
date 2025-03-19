/*
 * Java
 *
 * Copyright 2021-2024 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.net.ConnectivityManager;
import android.net.Network;

/**
 * Listen to network status changes. When connected list available network interfaces.
 */
public class OnNetworkStateChanged extends ConnectivityManager.NetworkCallback {

	private static final Logger LOGGER = Logger.getLogger("OnNetworkStateChanged");

	public OnNetworkStateChanged() {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("Listening on network interface changes");
		}
	}

	@Override
	public void onAvailable(Network network) {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Network UP");
		}

		listNetworkInterfaces();
	}

	@Override
	public void onLost(Network network) {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Network Down");
		}
		listNetworkInterfaces();
	}

	/**
	 * Log the list of registered {@link NetworkInterface}.
	 */
	private void listNetworkInterfaces() {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Available Network interfaces:");

			boolean netifFound = false;

			try {
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				while (interfaces.hasMoreElements()) {
					NetworkInterface netif = interfaces.nextElement();
					if (isValidNetworkInterface(netif)) {
						netifFound = true;
						logInterfaceInformation(netif);
					}
				}

				if (!netifFound) {
					LOGGER.info("(none)");
				}
			} catch (SocketException e) {
				if (LOGGER.isLoggable(Level.FINEST)) {
					LOGGER.log(Level.FINEST, e.getMessage(), e); // NOSONAR log if log level is finest or above
				}
			}
		}
	}

	/**
	 *
	 * @param netif
	 *            to validate
	 * @return true if interface is UP and not loop back
	 */
	private boolean isValidNetworkInterface(final NetworkInterface netif) {
		try {
			return netif.isUp() && !netif.isLoopback();
		} catch (SocketException e) {
			if (LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.log(Level.FINEST, e.getMessage(), e); // NOSONAR log if log level is finest or above
			}
		}
		return false;
	}

	private void logInterfaceInformation(NetworkInterface netif) throws SocketException {
		LOGGER.info("Network Name: " + netif.getName());
		LOGGER.info("--- MTU: " + netif.getMTU());
		Enumeration<InetAddress> addresses = netif.getInetAddresses();
		while (addresses.hasMoreElements()) {
			InetAddress address = addresses.nextElement();
			LOGGER.info("--- IP Address: " + address.getHostAddress());
		}
	}
}

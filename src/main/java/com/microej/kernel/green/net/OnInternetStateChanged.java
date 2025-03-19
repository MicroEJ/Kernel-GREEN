/*
 * Java
 *
 * Copyright 2021-2024 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green.net;

import java.util.logging.Level;
import java.util.logging.Logger;

import ej.bon.Timer;
import ej.net.util.connectivity.SimpleNetworkCallback;

public class OnInternetStateChanged implements SimpleNetworkCallback {

	private static final Logger LOGGER = Logger.getLogger("onInternetStateChanged");

	private final Timer timer;

	public OnInternetStateChanged(final Timer timer) {
		this.timer = timer;
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("Listening on internet connectivity changes");
		}
	}

	@Override
	public void onInternet(boolean hasInternet) {
		if (hasInternet) {
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("Internet Connection Detected");
			}

			this.timer.schedule(new NtpSyncTimerTask(), 0);
		} else {
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("Internet Connection Lost");
			}
		}
	}

	@Override
	public void onConnectivity(boolean isConnected) {
		// no-op
	}
}

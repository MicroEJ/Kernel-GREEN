/*
 * Java
 *
 * Copyright 2023 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green.ntp;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;

import com.microej.kernel.green.Main;

import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import ej.bon.Util;
import ej.net.util.NtpUtil;
import ej.net.util.connectivity.ConnectivityUtil;
import ej.net.util.connectivity.SimpleNetworkCallback;
import ej.net.util.connectivity.SimpleNetworkCallbackAdapter;
import ej.service.ServiceFactory;

/**
 * Class that handles the process of updating the NTP.
 */
public class NTPService implements SimpleNetworkCallback {

	private static final long NTP_UPDATE_PERIOD = 2 * 60 * 60 * 1000L;
	private static final long NTP_DEFAULT_DELAY = 20 * 1000L;
	private static final int RETRY_PERIOD = 1000;
	private static final int MAX_RETRY = 10;
	private static final long THRESHOLD = 1563791681000l;

	private final String url;
	private final int port;

	private NetworkCallback ntpNetworkCallback;
	private final RetryHelper retryHelper;

	/**
	 * Instantiates an {@link NTPService}.
	 */
	public NTPService() {
		this.retryHelper = new RetryHelper(this, RETRY_PERIOD, MAX_RETRY);
		this.url = System.getProperty(NtpUtil.NTP_URL_PROPERTY, NtpUtil.NTP_DEFAULT_SERVER);
		this.port = Integer.getInteger(NtpUtil.NTP_PORT_PROPERTY, NtpUtil.NTP_DEFAULT_PORT).intValue();
	}

	/**
	 * Starts the NTP client.
	 */
	public void start() {
		Main.LOGGER.info("Start the ntp client");
		ConnectivityManager connectivityManager = ServiceFactory.getService(ConnectivityManager.class);
		if (connectivityManager != null) {
			Main.LOGGER.info("Use the connectivity manager");
			ConnectivityUtil.registerAndCall(connectivityManager, new SimpleNetworkCallbackAdapter(this));
		} else {
			this.retryHelper.scheduleRetryUpdate(NTP_DEFAULT_DELAY, NTP_UPDATE_PERIOD);
		}
	}

	/**
	 * Stops the NTP client.
	 */
	public void stop() {
		Main.LOGGER.info("Stop the ntp client");
		ConnectivityManager connectivityManager = ServiceFactory.getService(ConnectivityManager.class);
		if (connectivityManager != null) {
			connectivityManager.unregisterNetworkCallback(this.ntpNetworkCallback);
		}
		this.retryHelper.stop();
	}

	/**
	 * Tries an update.
	 *
	 * @return <code>true</code> if the update is successful.
	 */
	public boolean update() {
		boolean updated = false;
		try {
			NtpUtil.updateLocalTime(this.url, this.port, 10000);
			updated = Util.currentTimeMillis() > THRESHOLD;
			if (updated) {
				Main.LOGGER.info("Updated time: " + new Date(System.currentTimeMillis()));
			}
		} catch (IOException e) {
			Main.LOGGER.log(Level.INFO, e.getMessage(), e);
		}

		return updated;
	}

	@Override
	public void onInternet(boolean hasInternet) {
		if (hasInternet) {
			this.retryHelper.scheduleRetryUpdate(0, NTP_UPDATE_PERIOD);
		} else {
			this.retryHelper.stop();
		}
	}

	@Override
	public void onConnectivity(boolean isConnected) {
		if (!isConnected) {
			this.retryHelper.stop();
		}
	}
}

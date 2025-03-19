/*
 * Java
 *
 * Copyright 2023-2024 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green.net;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import ej.bon.Timer;
import ej.bon.TimerTask;
import ej.bon.Util;
import ej.net.util.NtpUtil;
import ej.service.ServiceFactory;

/**
 * {@link NtpSyncTimerTask} periodically synchronizes the system time with a Network Time Protocol (NTP) server.
 * 
 * This task retries failed synchronization attempts up to a maximum limit and schedules a recurring update every two
 * hours if successful.
 */
public class NtpSyncTimerTask extends TimerTask {

	private static final Logger LOGGER = Logger.getLogger("NTPUpdateTimerTask");

	// Constants for NTP synchronization

	private static final long NTP_THRESHOLD = 1_563_791_681_000L; // Saturday, October 5, 2019, 02:08:01 UTC
	private static final int NTP_RETRY_DELAY_MS = 15_000;
	private static final int NTP_MAX_RETRY = 20; // Maximum retries for time synchronization
	private static final long NTP_UPDATE_PERIOD_MS = 2 * 60 * 60 * 1000L; // Update period

	private final String ntpUrl;
	private final int ntpPort;
	private final int ntpTimeout;
	private long retries;

	/**
	 * Initializes NTPUpdateTask with server URL and port, either from system properties or defaults.
	 */
	public NtpSyncTimerTask() {
		ntpUrl = System.getProperty(NtpUtil.NTP_URL_PROPERTY, NtpUtil.NTP_DEFAULT_SERVER);
		ntpPort = Integer.getInteger(NtpUtil.NTP_PORT_PROPERTY, NtpUtil.NTP_DEFAULT_PORT);
		ntpTimeout = Integer.getInteger(NtpUtil.NTP_TIMEOUT_PROPERTY, NtpUtil.NTP_DEFAULT_TIMEOUT);
		retries = 0;
	}

	@Override
	public void run() {
		Timer timer = ServiceFactory.getRequiredService(Timer.class);
		// Attempt to update the system time via NTP and check result
		if (updateTime()) {
			// Log the updated time if synchronization is successful
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("Updated time: " + new Date(System.currentTimeMillis()));
				LOGGER.info("Scheduling a new time sync in " + NTP_UPDATE_PERIOD_MS / 60_000 + " minutes");
			}

			// Schedule recurring synchronization in next two hours
			timer.schedule(new NtpSyncTimerTask(), NTP_UPDATE_PERIOD_MS);

		} else {
			// Increment retry counter if the update failed
			retries++;
			if (retries >= NTP_MAX_RETRY) {
				if (LOGGER.isLoggable(Level.SEVERE)) {
					LOGGER.severe("Failed to update time after maximum retries");
				}
			} else {
				// schedule a new attempt by increasing delay with retry to have a backoff
				timer.schedule(new NtpSyncTimerTask(), NTP_RETRY_DELAY_MS * retries);
			}
		}
	}

	/**
	 * Updates the system time by querying the NTP server. Returns true if the update is successful and the time is
	 * valid.
	 *
	 * @return true if the time update is successful and valid; false otherwise
	 */
	private boolean updateTime() {
		try {
			// Attempt to synchronize local time with the NTP server
			NtpUtil.updateLocalTime(this.ntpUrl, this.ntpPort, ntpTimeout);

			// Confirm that the updated time exceeds a predefined threshold (ensures validity)
			return Util.currentTimeMillis() > NTP_THRESHOLD;
		} catch (IOException e) {
			// Log error if the NTP update fails due to an IOException
			if (LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, "Time update failed", e);
			}
		}

		return false;
	}
}

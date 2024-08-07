/*
 * Java
 *
 * Copyright 2023-2024 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green.ntp;

import java.util.logging.Level;

import com.microej.kernel.green.Main;

import ej.annotation.NonNull;
import ej.bon.Timer;
import ej.bon.TimerTask;
import ej.service.ServiceFactory;

/**
 * Utility class to handle fail and retry process in case time can't be retrieved or connectivityManager is null.
 */
public class RetryHelper {

	private final int period;
	private final int maxAttempt;
	private TimerTask retryTask;
	private TimerTask globalTask;
	private final NTPService retryUpdate;
	private final boolean shouldRetryInfinitely;

	/**
	 * Instantiates a {@link RetryHelper}.
	 *
	 * @param retryUpdate
	 *            the callback to be called to try updating the time.
	 * @param period
	 *            the period in milliseconds between two retry.
	 * @param maxAttempt
	 *            the max attempts (0 for infinite).
	 */
	public RetryHelper(@NonNull NTPService retryUpdate, int period, int maxAttempt) {
		super();
		this.retryUpdate = retryUpdate;
		this.period = period;
		this.maxAttempt = maxAttempt;
		this.shouldRetryInfinitely = maxAttempt == 0;
	}

	/**
	 * Stops the retry.
	 */
	public void stop() {
		cancelRetryTask();
		cancelGlobalRetryTask();
	}

	/**
	 * Schedule a an update.
	 *
	 * @param delay
	 *            delay in milliseconds before first update.
	 * @param period
	 *            period in milliseconds between successive update
	 */
	public synchronized void scheduleRetryUpdate(long delay, long period) {
		cancelGlobalRetryTask();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				synchronized (RetryHelper.this) {
					if (RetryHelper.this.retryTask == null) {
						retry();
					}
				}
			}
		};
		Timer timer = ServiceFactory.getService(Timer.class);
		this.globalTask = task;
		timer.schedule(task, delay, period);
	}

	/**
	 * Update calendar time by requesting time to the NTP server. If NTP server request failed, a new request will be
	 * scheduled.
	 */
	private void retry() {
		TimerTask task = new TimerTask() {
			private int remainingAttempts = RetryHelper.this.maxAttempt;

			@Override
			public void run() {
				boolean updateResult = RetryHelper.this.retryUpdate.update();
				if (updateResult) {
					// update successful, can stop retry
					cancelRetryTask();
				} else if (RetryHelper.this.shouldRetryInfinitely) {
					// update fail but infinite retry
					Main.LOGGER.severe("Error when updating time");
					Main.LOGGER.info("Update remaining retries : infinite");
				} else {
					// update fail, consume an attempt and stop retry if remaining is less than 0
					Main.LOGGER.severe("Error when updating time");
					this.remainingAttempts--;
					if (this.remainingAttempts < 0) {
						Main.LOGGER.warning("too many attempts, cancelling");
						// all try failed, do not schedule update time anymore
						cancelRetryTask();
					} else {
						// update fail, remaining attempt is strictly positive, don't cancel the retry task
						if (Main.LOGGER.isLoggable(Level.INFO)) {
							// Log if log level is info or above
							Main.LOGGER.info("Update remaining retries : " + this.remainingAttempts); // NOSONAR
						}
					}
				}
			}
		};
		scheduleRetryTask(task, 0, this.period);
	}

	private void scheduleRetryTask(TimerTask task, long delay, long period) {
		if (this.retryTask == null) {
			Timer timer = ServiceFactory.getService(Timer.class);
			this.retryTask = task;
			timer.schedule(task, delay, period);
			Main.LOGGER.info("Scheduled update time task");
		}
	}

	private synchronized void cancelRetryTask() {
		TimerTask timerTask = this.retryTask;
		this.retryTask = null;
		if (timerTask != null) {
			timerTask.cancel();
			Main.LOGGER.info("Stopped retry task");
		}
	}

	private synchronized void cancelGlobalRetryTask() {
		TimerTask globalTask = this.globalTask;
		this.globalTask = null;
		if (globalTask != null) {
			globalTask.cancel();
		}
	}
}

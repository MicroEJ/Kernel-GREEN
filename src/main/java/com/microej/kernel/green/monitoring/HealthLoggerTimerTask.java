/*
 * Java
 *
 * Copyright 2024 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green.monitoring;

import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import ej.basictool.map.PackedMap;
import ej.bon.TimerTask;

/**
 * Log CPU usage periodically
 * <p>
 * This logger generate the following log:
 * <p>
 * <code>
 healthloggertimertask INFO: ----------------------------
 healthloggertimertask INFO: Health Monitoring:
 healthloggertimertask INFO: ----------------------------
 healthloggertimertask INFO: Name	%CPU	%MEM	MEM(KB)
 healthloggertimertask INFO: App1	30.46	0.37	3.28
 healthloggertimertask INFO: App2	36.66	1.22	10.68
 healthloggertimertask INFO: GREEN	0.14	1.94	17.04
 healthloggertimertask INFO: %CPU: 67.25, KB Mem: 878 total, 847 free, 31.00 used
 healthloggertimertask INFO: Apps: 2
 * </code>
 */
public class HealthLoggerTimerTask extends TimerTask {

	public static final float ONE_KB = 1024f;
	private static final Logger LOGGER = Logger.getLogger("HealthLoggerTimerTask");
	private final HealthService healthMonitoringService;

	public HealthLoggerTimerTask(HealthService healthMonitoringService) {
		this.healthMonitoringService = healthMonitoringService;

	}

	@Override
	public void run() {
		if (LOGGER.isLoggable(Level.INFO)) {
			PackedMap<String, Float> cpuUsagePercent = healthMonitoringService.getUsedCpuPercentPerModule();
			PackedMap<String, Long> ramUsage = healthMonitoringService.getUsedMemoryPerModule();
			PackedMap<String, Float> ramUsagePercent = healthMonitoringService.getUsedMemoryPercentPerModule();
			if (cpuUsagePercent.isEmpty()) {
				return;
			}

			LOGGER.info("----------------------------");
			LOGGER.info("Health Monitoring:");
			LOGGER.info("----------------------------");
			LOGGER.info("Name\t%CPU\t%MEM\tMEM(KB)");

			float usedCPU = 0;
			float usedRAM = 0;
			for (String moduleName : cpuUsagePercent.keySet()) {
				float cpuPercent = cpuUsagePercent.get(moduleName) * 100.0f;
				float ramKB = ramUsage.get(moduleName) / ONE_KB;
				float ramPercent = ramUsagePercent.get(moduleName) * 100.0f;
				usedCPU += cpuPercent;
				usedRAM += ramKB;
				StringBuilder output = new StringBuilder();
				try (Formatter formatter = new Formatter(output)) {
					formatter.format("%s\t%.2f\t%.2f\t%.2f", moduleName, cpuPercent, ramPercent, ramKB);
				}
				LOGGER.info(output.toString());
			}

			long freeMemory = healthMonitoringService.getFreeMemory() / 1024;
			long totalMemory = healthMonitoringService.getTotalMemory() / 1024;

			StringBuilder totalLineLog = new StringBuilder();
			try (Formatter formatter = new Formatter(totalLineLog)) {
				formatter.format("%%CPU: %.2f, KB Mem: %d total, %d free, %.2f used", usedCPU, totalMemory, freeMemory,
						usedRAM);
			}

			LOGGER.info(totalLineLog.toString());
			LOGGER.info("Apps: " + (cpuUsagePercent.size() - 1));
		}
	}

}

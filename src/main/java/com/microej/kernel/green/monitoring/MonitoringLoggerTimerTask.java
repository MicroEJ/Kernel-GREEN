/*
 * Java
 *
 * Copyright 2024-2025 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green.monitoring;

import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microej.kf.util.monitoring.ResourceMonitoringService;
import ej.basictool.map.PackedMap;
import ej.bon.TimerTask;

/**
 * Logs CPU and RAM usage periodically
 * <p>
 * This logger generate the following log:
 * <p>
 * <code>
 monitoringloggertimertask INFO: ----------------------------
 monitoringloggertimertask INFO: Resource Monitoring:
 monitoringloggertimertask INFO: ----------------------------
 monitoringloggertimertask INFO: Name	%CPU	%MEM	MEM(KB)
 monitoringloggertimertask INFO: App1	30.46	0.37	3.28
 monitoringloggertimertask INFO: App2	36.66	1.22	10.68
 monitoringloggertimertask INFO: GREEN	0.14	1.94	17.04
 monitoringloggertimertask INFO: %CPU: 67.25, KB Mem: 878 total, 847 free, 31.00 used
 monitoringloggertimertask INFO: Apps: 2
 * </code>
 */
public class MonitoringLoggerTimerTask extends TimerTask {

	public static final float ONE_KB = 1024f;
	private static final Logger LOGGER = Logger.getLogger("MonitoringLoggerTimerTask");
	private final ResourceMonitoringService resourceMonitoringService;

	public MonitoringLoggerTimerTask(ResourceMonitoringService resourceMonitoringService) {
		this.resourceMonitoringService = resourceMonitoringService;
	}

	@Override
	public void run() {
		if (LOGGER.isLoggable(Level.INFO)) {
			PackedMap<String, Float> cpuUsagePercent = resourceMonitoringService.getUsedCpuPercentPerModule();
			PackedMap<String, Long> ramUsage = resourceMonitoringService.getUsedMemoryPerModule();
			PackedMap<String, Float> ramUsagePercent = resourceMonitoringService.getUsedMemoryPercentPerModule();
			if (cpuUsagePercent.isEmpty()) {
				return;
			}

			LOGGER.info("----------------------------");
			LOGGER.info("Resource Monitoring:");
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

			long freeMemory = resourceMonitoringService.getFreeMemory() / 1024;
			long totalMemory = resourceMonitoringService.getTotalMemory() / 1024;

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

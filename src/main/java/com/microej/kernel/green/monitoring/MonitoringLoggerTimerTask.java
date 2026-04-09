/*
 * Java
 *
 * Copyright 2024-2025 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green.monitoring;

import com.microej.kf.util.control.fs.FileSystemResourcesController;
import com.microej.kf.util.module.SandboxedModule;
import com.microej.kf.util.module.SandboxedModuleHelper;
import com.microej.kf.util.monitoring.ResourceMonitoringService;
import ej.basictool.map.PackedMap;
import ej.bon.TimerTask;
import ej.service.ServiceFactory;

import java.util.Formatter;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logs CPU and RAM usage periodically
 * <p>
 * This logger generate the following log:
 * <p>
 * <code>
 monitoringloggertimertask INFO: -------------------------------------------------
 monitoringloggertimertask INFO: Resource Monitoring:
 monitoringloggertimertask INFO: -------------------------------------------------
 monitoringloggertimertask INFO: Name	%CPU	%MEM	MEM(KB)  FS(KB)
 monitoringloggertimertask INFO: App1	30.46	0.37	3.28     0.21
 monitoringloggertimertask INFO: App2	36.66	1.22	10.68    5.32
 monitoringloggertimertask INFO: GREEN	0.14	1.94	17.04    12.51
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

			int maxNameLength = getMaxNameLength(cpuUsagePercent.keySet());

			LOGGER.info("-------------------------------------------------");
			LOGGER.info("Resources Monitoring:");
			LOGGER.info("-------------------------------------------------");
			LOGGER.info("Name" + getNamePadding(maxNameLength, "Name") + "\t%CPU\t%MEM\tMEM(KB)\tFS(KB)");

			SandboxedModuleHelper moduleManager = ServiceFactory.getRequiredService(SandboxedModuleHelper.class);

			float usedCPU = 0;
			float usedRAM = 0;
			float flashKB = 0;
			for (String moduleName : cpuUsagePercent.keySet()) {
				float cpuPercent = cpuUsagePercent.get(moduleName) * 100.0f;
				float ramKB = ramUsage.get(moduleName) / ONE_KB;
				float ramPercent = ramUsagePercent.get(moduleName) * 100.0f;
				usedCPU += cpuPercent;
				usedRAM += ramKB;

				SandboxedModule sandboxedModule = moduleManager.getModule(moduleName);
				if (sandboxedModule != null) {
					FileSystemResourcesController fsController = sandboxedModule.getFileSystemResourceController();
					flashKB = fsController.getStorageSize() / ONE_KB;
				}

				StringBuilder output = new StringBuilder();
				try (Formatter formatter = new Formatter(output)) {
					formatter.format("%s%s\t%.2f\t%.2f\t%.2f\t%.2f", moduleName,
							getNamePadding(maxNameLength, moduleName), cpuPercent, ramPercent, ramKB, flashKB);
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

	private int getMaxNameLength(Set<String> names) {
		int longest = 0;
		for (String name : names) {
			if (longest < name.length()) {
				longest = name.length();
			}
		}
		return longest;
	}

	private String getNamePadding(int maxNameLength, String name) {
		StringBuilder padding = new StringBuilder();

		for (int i = 0; i < (maxNameLength - name.length()); i++) {
			padding.append(" ");
		}

		return padding.toString();
	}
}
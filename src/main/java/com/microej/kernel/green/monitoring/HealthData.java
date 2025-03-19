/*
 * Java
 *
 * Copyright 2024 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green.monitoring;

import ej.basictool.map.PackedMap;
import ej.kf.Module;

public class HealthData {

	private final PackedMap<Module, ExecutionCounter> executionCounterPerModuleMap;
	private final PackedMap<Module, Long> ramUsagePerModuleMap;
	private final PackedMap<Module, Integer> originalQuotaPerModuleMap;
	private final boolean runGcBeforeCollecting;
	private final long totalMemory;
	private final long maxMemory;

	public HealthData(boolean runGcBeforeCollecting) {
		this.ramUsagePerModuleMap = new PackedMap<>();
		this.executionCounterPerModuleMap = new PackedMap<>();
		this.originalQuotaPerModuleMap = new PackedMap<>();
		// Init Memory Monitoring data
		this.runGcBeforeCollecting = runGcBeforeCollecting;
		if (runGcBeforeCollecting) {
			System.gc();// NOSONAR
		}
		this.totalMemory = Runtime.getRuntime().totalMemory();
		this.maxMemory = Runtime.getRuntime().maxMemory();
	}

	public PackedMap<Module, ExecutionCounter> getExecutionCounterPerModuleMap() {
		return executionCounterPerModuleMap;
	}

	public PackedMap<Module, Long> getRamUsagePerModuleMap() {
		return ramUsagePerModuleMap;
	}

	public PackedMap<Module, Integer> getOriginalQuotaPerModuleMap() {
		return originalQuotaPerModuleMap;
	}

	public long getTotalMemory() {
		return totalMemory;
	}

	public long getMaxMemory() {
		return maxMemory;
	}

	public long getFreeMemory() {
		if (runGcBeforeCollecting) {
			System.gc(); // NOSONAR
		}
		return Runtime.getRuntime().freeMemory();
	}

	public static class ExecutionCounter {
		private long current;
		private long last;

		public ExecutionCounter(long execCounter, long lastExecCounter) {
			this.current = execCounter;
			this.last = lastExecCounter;
		}

		public long getCurrent() {
			return current;
		}

		public void setCurrent(long current) {
			this.current = current;
		}

		public long getLast() {
			return last;
		}

		public void setLast(long last) {
			this.last = last;
		}
	}
}

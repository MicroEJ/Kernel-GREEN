/*
 * Java
 *
 * Copyright 2024 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green.monitoring;

import ej.annotation.Nullable;
import ej.basictool.map.PackedMap;
import ej.bon.Timer;
import ej.bon.TimerTask;
import ej.kf.Feature;
import ej.kf.FeatureStateListener;
import ej.kf.Kernel;
import ej.kf.Module;

public class HealthServiceImpl implements HealthService, FeatureStateListener {

	private static final int UNLIMITED_QUOTA = Integer.MAX_VALUE;
	private static final int NO_QUOTA = -1;

	private final Timer timer;
	private final long intervalMS;
	private final boolean runGcBeforeCollecting;
	private final HealthData healthData;
	private final long maxCpuLoadPerMonitoringPeriod;

	@Nullable
	private TimerTask monitoringTask;
	private boolean running;

	public HealthServiceImpl(Timer timer, long intervalMS, boolean runGcBeforeCollecting) {
		this.timer = timer;
		this.intervalMS = intervalMS;
		this.runGcBeforeCollecting = runGcBeforeCollecting;
		long maxCpuPerSecond = Long.getLong("health.check.cpu.calibration", 731010); // default for RT1170
		this.maxCpuLoadPerMonitoringPeriod = maxCpuPerSecond * intervalMS / 1000;

		Kernel.addFeatureStateListener(this);

        this.healthData = new HealthData(runGcBeforeCollecting);
		this.monitoringTask = null;
	}

	public void start() {
		if (running) {
			return; // monitoring task already running
		}

		enableModuleMonitoring(Kernel.getInstance());
		for (Feature module : Kernel.getAllLoadedFeatures()) {
			if (Feature.State.STARTED.equals(module.getState())) {
				enableModuleMonitoring(module);
			}
		}

		monitoringTask = new MonitoringTask(healthData, runGcBeforeCollecting);
		timer.scheduleAtFixedRate(monitoringTask, 0, intervalMS);

		running = true;
	}

	public void stop() {
		if (!running) {
			return;
		}

		monitoringTask.cancel();

		disableModuleMonitoring(Kernel.getInstance());
		for (Feature module : Kernel.getAllLoadedFeatures()) {
			if (Feature.State.STARTED.equals(module.getState())) {
				disableModuleMonitoring(module);
			}
		}

		running = false;
	}

	@Override
	public void stateChanged(Feature app, @Nullable Feature.State state) {
		final Feature.State currentState = app.getState();
		if (Feature.State.STARTED.equals( currentState)) {
			enableModuleMonitoring(app);
		} else if (Feature.State.STOPPED.equals(currentState)) {
			disableModuleMonitoring(app);
		}
	}

	@Override
	public PackedMap<String, Float> getUsedCpuPercentPerModule() {
		PackedMap<String, Float> result = new PackedMap<>();
		synchronized (healthData) {
			for (Module module : healthData.getExecutionCounterPerModuleMap().keySet()) {
				HealthData.ExecutionCounter execCounter = healthData.getExecutionCounterPerModuleMap().get(module);
				float percentCpu = (float) (execCounter.getCurrent() - execCounter.getLast())
						/ maxCpuLoadPerMonitoringPeriod;
				result.put(module.getName(), percentCpu);
			}
		}

		return result;
	}

	@Override
	public PackedMap<String, Long> getUsedMemoryPerModule() {
		PackedMap<String, Long> result = new PackedMap<>();
		synchronized (healthData) {
			for (Module module : healthData.getRamUsagePerModuleMap().keySet()) {
				result.put(module.getName(), healthData.getRamUsagePerModuleMap().get(module));
			}
		}

		return result;
	}

	@Override
	public PackedMap<String, Float> getUsedMemoryPercentPerModule() {
		PackedMap<String, Float> result = new PackedMap<>();
		synchronized (healthData) {
			for (Module module : healthData.getRamUsagePerModuleMap().keySet()) {
				long value = healthData.getRamUsagePerModuleMap().get(module);
				result.put(module.getName(), (float) (value) / healthData.getTotalMemory());
			}
		}
		return result;
	}

	@Override
	public long getFreeMemory() {
		return healthData.getFreeMemory();
	}

	@Override
	public long getMaxMemory() {
		return healthData.getMaxMemory();
	}

	@Override
	public long getTotalMemory() {
		return healthData.getTotalMemory();
	}

	private void enableModuleMonitoring(Module module) {
		int previousQuota = module.getExecutionQuota();
		boolean noQuota = previousQuota == NO_QUOTA;
		if (noQuota) {
			module.setExecutionQuota(UNLIMITED_QUOTA); // Activate cpu monitoring
			synchronized (healthData) {
				healthData.getOriginalQuotaPerModuleMap().put(module, previousQuota);
				healthData.getExecutionCounterPerModuleMap().put(module, new HealthData.ExecutionCounter(0, 0));
				healthData.getRamUsagePerModuleMap().put(module, 0L);
			}
		}
	}

	private void disableModuleMonitoring(Module module) {
		synchronized (healthData) {
			healthData.getExecutionCounterPerModuleMap().remove(module);
			healthData.getRamUsagePerModuleMap().remove(module);
			Integer originalQuota = healthData.getOriginalQuotaPerModuleMap().remove(module);
			if (originalQuota != null) {
				module.setExecutionQuota(originalQuota);
			}
		}
	}

	public static final class MonitoringTask extends TimerTask {

		private final HealthData healthData;
		private final boolean runGcBeforeCollecting;

		public MonitoringTask(final HealthData healthData, boolean runGcBeforeCollecting) {
			this.healthData = healthData;
			this.runGcBeforeCollecting = runGcBeforeCollecting;
		}

		@Override
		public void run() {
			if (runGcBeforeCollecting) {
				Runtime.getRuntime().gc(); // NOSONAR Call used to increase heap monitoring accuracy
			}

			for (Feature app : Kernel.getAllLoadedFeatures()) {
				if (app.getState() == Feature.State.STARTED) {
					computeModuleCpuUsage(app);
					computeModuleMemoryUsage(app);
				}
			}

			computeModuleCpuUsage(Kernel.getInstance());
			computeModuleMemoryUsage(Kernel.getInstance());

		}

		private void computeModuleCpuUsage(Module module) {
			synchronized (healthData) {
				HealthData.ExecutionCounter exec = healthData.getExecutionCounterPerModuleMap().get(module);
				long newExecutionCounter = module.getExecutionCounter();
				// if new exec counter is less than last. exec counter was reset
				exec.setLast(newExecutionCounter < exec.getCurrent() ? 0 : exec.getCurrent());
				exec.setCurrent(newExecutionCounter);
			}
		}

		private void computeModuleMemoryUsage(Module module) {
			synchronized (healthData) {
				healthData.getRamUsagePerModuleMap().put(module, module.getAllocatedMemory());
			}
		}
	}
}

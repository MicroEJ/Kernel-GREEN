/*
 * Java
 *
 * Copyright 2024 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green.monitoring;

import ej.bon.Util;
import ej.kf.Kernel;

/**
 * Calculate `health.check.cpu.calibration` value representing the mapping of CPU load to the MicroEJ execution counter
 * per second.
 */
public class CpuCalibration {

	private static final int CALIBRATION_PERIOD_MS = 2 * 60 * 1000;

	private CpuCalibration() {
		// no-op
	}

	public static void main(String[] args) {
		System.out.println("Starting CPU Calibration. Duration: " + CALIBRATION_PERIOD_MS / 1000 + " seconds"); // NOSONAR
		Kernel.getInstance().setExecutionQuota(Integer.MAX_VALUE); // Activate execution counter
		long start = System.currentTimeMillis();
		while (true) {
			basicFibonacci();
			if (Util.currentTimeMillis() - start > CALIBRATION_PERIOD_MS) {
				break;
			}
		}
		long effectiveDurationMS = Util.currentTimeMillis() - start;
		long counter = Kernel.getInstance().getExecutionCounter();
		long execCounterPerSecond = counter / (effectiveDurationMS / 1000);
		System.out.println("Execution Counter  Per second: " + execCounterPerSecond); // NOSONAR
		System.out.println("Done.");// NOSONAR
	}

	private static void basicFibonacci() {
		long a = 0;
		long b = 1;
		long temp;
		long n = 10;
		for (int i = 0; i < n; i++) {
			temp = a + b;
			a = b;
			b = temp;
		}
	}
}

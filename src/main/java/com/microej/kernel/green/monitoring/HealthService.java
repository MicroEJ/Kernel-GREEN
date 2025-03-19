/*
 * Java
 *
 * Copyright 2024 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green.monitoring;

import ej.basictool.map.PackedMap;

/**
 * CPU Monitoring Service
 */
public interface HealthService {

	/**
	 * Retrieves the CPU usage for each module from the previous monitoring cycle, expressed as a percentage.
	 *
	 * @return A map where each entry contains a module name as the key and its CPU usage percentage (0.0 to 1.0) as the
	 *         value.
	 */
	PackedMap<String, Float> getUsedCpuPercentPerModule();

	/**
	 * Retrieves the RAM usage values expressed as a percentage (a value between 0 and 1.0).
	 * <p>
	 * The percentage indicates the ratio between a module's current heap consumption and the total heap.
	 *
	 * @return A map where each entry contains the module name as the key and its current RAM usage percentage as the
	 *         value.
	 */
	PackedMap<String, Float> getUsedMemoryPercentPerModule();

	/**
	 * Retrieves the current RAM usage for each module.
	 *
	 * @return A map where each entry contains a module identifier as the key and its current RAM usage in bytes as the
	 *         value.
	 */
	PackedMap<String, Long> getUsedMemoryPerModule();

	/**
	 * 
	 * @return Free Memory
	 */
	long getFreeMemory();

	/**
	 * 
	 * @return return max memory
	 */
	long getMaxMemory();

	/**
	 * 
	 * @return total memory
	 */
	long getTotalMemory();

}

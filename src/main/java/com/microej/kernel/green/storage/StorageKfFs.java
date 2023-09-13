/*
 * Java
 *
 * Copyright 2019-2023 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */

package com.microej.kernel.green.storage;

import java.io.IOException;

import ej.kf.Kernel;
import ej.kf.Module;
import ej.storage.fs.StorageFs;

/**
 * Extends the storage on file system to add sandboxing for the applications.
 */
public class StorageKfFs extends StorageFs {

	private static final String KERNEL_PARENT_PREFIX = "kernel";
	private static final String FEATURES_PARENT_PREFIX = "feature";

	/**
	 * Creates a sandboxed storage on file system.
	 * <p>
	 * The root folder is determined dynamically depending on the KF context. Each module has a different one.
	 *
	 *
	 * @throws IOException
	 *             if the root cannot be created.
	 * @see Kernel#getContextOwner()
	 * @see StorageFs#StorageFs()
	 */
	public StorageKfFs() throws IOException {
		this(getRootFolderPropertyOrDefault());
	}

	/**
	 * Creates a storage on file system specifying the parent folder where the root folder will be created.
	 * <p>
	 * The root folder is determined dynamically depending on the KF context. Each module has a different one.
	 *
	 * @param parent
	 *            the parent folder.
	 * @throws IllegalArgumentException
	 *             if the root already exists and is not a directory.
	 * @throws IOException
	 *             if the root cannot be created.
	 */
	public StorageKfFs(String parent) throws IOException {
		super(parent + '/' + getContextName());
	}

	private static String getContextName() {
		if (Kernel.isInKernelMode()) {
			return KERNEL_PARENT_PREFIX;
		} else {
			Module contextOwner = Kernel.getContextOwner();
			return FEATURES_PARENT_PREFIX + contextOwner.getName();
		}
	}

}
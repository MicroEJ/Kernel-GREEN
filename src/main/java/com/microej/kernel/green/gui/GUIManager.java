/*
 * Java
 *
 * Copyright 2023-2024 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green.gui;

import com.microej.kf.util.RunnableWithResult;

import ej.kf.Feature;
import ej.kf.Feature.State;
import ej.kf.FeatureStateListener;
import ej.kf.Kernel;
import ej.microui.MicroUI;
import ej.microui.display.Display;

/**
 * Class to handle UI initialization.
 */
public class GUIManager {

	private GUIManager() {
		// disable the public constructor
	}

	/**
	 * Starts MicroUI and initializes a black screen.
	 */
	public static void initUI() {
		MicroUI.start();
		Display.getDisplay().requestShow(new BlackScreenDisplayable());
		handleScreenOnFeatureStop();
	}

	// This method handles the screen when an application stops
	// It will make sure that in case an application stops, if no other application nor the Kernel have a Displayable,
	// it shows a
	// new black screen.
	private static void handleScreenOnFeatureStop() {

		Kernel.addFeatureStateListener(new FeatureStateListener() {

			@Override
			public void stateChanged(Feature feature, State previousState) {
				if ((feature.getState() == State.STOPPED)) {

					// Do not show the black screen if the Kernel has a Displayable.
					if (Display.getDisplay().getDisplayable() != null) {
						return;
					}

					if (!checkForExistingDisplayableInFeatures(feature)) {
						// The Kernel or a feature has a Displayable, do not show any black screen
						Display.getDisplay().requestShow(new BlackScreenDisplayable());
					}
				}

			}
		});
	}

	private static boolean checkForExistingDisplayableInFeatures(Feature stoppedFeature) {
		// Iterate over each started feature (except the one stopping) and check if they have a Displayable.
		for (Feature f : Kernel.getAllLoadedFeatures()) {
			if (f == stoppedFeature || f.getState() != State.STARTED) {
				continue;
			}

			RunnableWithResult<Boolean> displayableExists = new RunnableWithResult<Boolean>() {

				@Override
				protected Boolean runWithResult() {

					// Check if the feature has a display, if so, no black screen is needed
					return Boolean.valueOf(Display.getDisplay().getDisplayable() != null);

				}
			};

			// Execute code under the feature context to check if it has a Displayable
			Kernel.runUnderContext(f, displayableExists);

			// At least one application has a Displayable therefore we know we won't need the black screen.

			if (displayableExists.getResult().booleanValue()) {
				return true;
			}
		}

		// No feature with Displayable found
		return false;
	}
}

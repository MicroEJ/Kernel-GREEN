/*
 * Java
 *
 * Copyright 2021-2023 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green.gui;

import ej.microui.display.Colors;
import ej.microui.display.Displayable;
import ej.microui.display.GraphicsContext;
import ej.microui.display.Painter;

/**
 * Class that represents a black screen to be displayed by the Kernel
 */
public class BlackScreenDisplayable extends Displayable {

	@Override
	protected void render(GraphicsContext gc) {
		// clean
		gc.setColor(Colors.BLACK);
		int width = gc.getWidth();
		int height = gc.getHeight();
		Painter.fillRectangle(gc, 0, 0, width, height);
	}

	@Override
	public boolean handleEvent(int event) {
		return false;
	}
}

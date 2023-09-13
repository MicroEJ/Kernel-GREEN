/*
 * Java
 *
 * Copyright 2023 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.kernel.green.localdeploy;

import java.io.IOException;
import java.util.logging.Level;

import com.microej.kernel.green.Main;
import com.microej.library.kernel.rcommand.local.DeviceInfoCommand;
import com.microej.library.kernel.rcommand.local.InstallCommand;
import com.microej.library.kernel.rcommand.local.ListCommand;
import com.microej.library.kernel.rcommand.local.ListCommandCommand;
import com.microej.library.kernel.rcommand.local.StartCommand;
import com.microej.library.kernel.rcommand.local.StopCommand;
import com.microej.library.kernel.rcommand.local.UninstallCommand;

import ej.rcommand.RemoteCommandManager;
import ej.rcommand.impl.DefaultRemoteCommandManager;
import ej.rcommand.serversocket.RemoteCommandServer;

/**
 * CommandServerEntryPoint class is responsible for registering the listener, the logger and start the listener of
 * remote commands.
 */
public class CommandServer {

	private static final int COMMAND_SERVER_NB_MAX_CONNECTION = 2;
	private static final int COMMAND_SERVER_PORT = 4000;
	private static final String ADMIN_SERVER = "admin server";
	private final RemoteCommandServer adminServer;
	private final RemoteCommandManager commandManager;

	/**
	 * Default constructor that registers the listeners, the logger and instantiates the RemoteCommandServer.
	 */
	public CommandServer() {
		this.commandManager = new DefaultRemoteCommandManager();
		this.commandManager.registerListener(new InstallCommand());
		this.commandManager.registerListener(new DeviceInfoCommand());
		this.commandManager.registerListener(new ListCommand());
		this.commandManager.registerListener(new StartCommand());
		this.commandManager.registerListener(new StopCommand());
		this.commandManager.registerListener(new UninstallCommand());
		this.commandManager.registerListener(new ListCommandCommand(this.commandManager));
		this.adminServer = new RemoteCommandServer(this.commandManager, COMMAND_SERVER_PORT,
				COMMAND_SERVER_NB_MAX_CONNECTION);

	}

	/**
	 * Starts the remote command server in a new thread.
	 */
	public synchronized void startServer() {
		Main.LOGGER.info("Start the " + ADMIN_SERVER);
		new Thread(this.adminServer, ADMIN_SERVER).start();
	}

	/**
	 * Stop the remote command server
	 */
	public synchronized void stopServer() {
		Main.LOGGER.info("Stop the " + ADMIN_SERVER);
		this.commandManager.stopAll();
		try {
			this.adminServer.stopRunning();
		} catch (IOException e) {
			Main.LOGGER.log(Level.INFO, ADMIN_SERVER, e);
		}
	}

}

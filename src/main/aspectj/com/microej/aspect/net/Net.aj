/*
 * Java
 *
 * Copyright 2025 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */

package com.microej.aspect.net;

import java.net.*;
import javax.net.ssl.*;
import java.io.*;
import com.microej.kf.util.control.net.*;

public aspect Net {

	/////
	// Socket creation
	/////
	
	private static Object Socket.lock = new Object();

	void around(Socket socket): execution(Socket.new()) && target(socket) {
		synchronized (socket.lock) {
			OpenSocket openSocket = new OpenSocket(socket);
			openSocket.onStart();
			try {
				proceed(socket);
				openSocket.onEnd(true);
			} catch(SecurityException e) {
				openSocket.onEnd(false);
				throw e;
			}
		}
	}
	  
	void around(SocketAddress address, SocketAddress localAddr, boolean stream, Socket socket) throws IOException: execution(Socket.new(SocketAddress, SocketAddress, boolean)) && args(address, localAddr, stream) && target(socket) {
		synchronized (socket.lock) {
			OpenSocket openSocket = new OpenSocket(socket);
			openSocket.onStart();
			try {
				proceed(address, localAddr, stream, socket);
				openSocket.onEnd(true);
			} catch(IOException | IllegalArgumentException | NullPointerException | SecurityException e) {
				openSocket.onEnd(false);
				throw e;
			}
		}
	}
	  
	void around(SocketImpl impl, Socket socket) throws SocketException: execution(Socket.new(SocketImpl)) && args(impl) && target(socket) {
		synchronized (socket.lock) {
			OpenSocket openSocket = new OpenSocket(socket);
			openSocket.onStart();
			try {
				proceed(impl, socket);
				openSocket.onEnd(true);
			} catch(SocketException | SecurityException e) {
				openSocket.onEnd(false);
				throw e;
			}
		}
	}
	  
	/////
	// SocketInputStream (Net and SSL)
	/////
	 
	InputStream around(Socket socket) throws IOException: execution(InputStream Socket.getInputStream()) && target(socket) {
		InputStream in = proceed(socket);
		return new SocketInputStream(socket, in);
	}
  	
	InputStream around(SSLSocket sslSocket) throws IOException: execution(InputStream SSLSocket.getInputStream()) && target(sslSocket) {
		InputStream in = proceed(sslSocket);
		return new SocketInputStream(sslSocket, in);
	}
  	
  	/////
	// SocketOutputStream (Net and SSL)
	/////
	 
	OutputStream around(Socket socket) throws IOException: execution(OutputStream Socket.getOutputStream()) && target(socket) {
		OutputStream out = proceed(socket);
		return new SocketOutputStream(socket, out);
	}
  	
	OutputStream around(SSLSocket sslSocket) throws IOException: execution(OutputStream SSLSocket.getOutputStream()) && target(sslSocket) {
		OutputStream out = proceed(sslSocket);
		return new SocketOutputStream(sslSocket, out);
	}
  	
	/////
	// Socket close
	/////
	 	
	void around(Socket socket) throws IOException: execution(void Socket.close()) && target(socket) {
		boolean wasClosed = socket.isClosed();
		proceed(socket);
		if (!wasClosed) {
			new CloseSocket(socket).onEnd(true);
		}
	}
}

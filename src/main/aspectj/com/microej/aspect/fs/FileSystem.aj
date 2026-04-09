/*
 * Java
 *
 * Copyright 2025 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */

package com.microej.aspect.fs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import com.microej.kf.util.control.fs.*;

public aspect FileSystem {

	/////
	// File
	/////
	private static Object File.lock = new Object(); //global lock for File operations.

	/////
	// FileInputStream
	/////
	private boolean FileInputStream.isOpen = false;
	private File FileInputStream.file = null;
	private static Object FileInputStream.lock = new Object(); // global lock for FileInputStream operations.

	void around(File file) throws FileNotFoundException: execution(FileInputStream.new(File)) && args(file) {
		synchronized (file.lock) {
			OpenFile openFile = new OpenFile(file);
			openFile.onStart();
			try {
				proceed(file);
				openFile.onEnd(true);
			} catch(FileNotFoundException | SecurityException e) {
				openFile.onEnd(false);
				throw e;
			}
		}
	}
	  
	after(File file, FileInputStream fis) : execution(FileInputStream.new(File)) && args(file) && target(fis) {
	 	fis.isOpen = true;
	  	fis.file = file;
	}
	    
	void around(FileInputStream fis): execution(void FileInputStream.close()) && target(fis) {
		proceed(fis);
		if (fis.isOpen) { // sanity check
			new CloseFile(fis.file).onEnd(true);
	  		fis.isOpen = false;
		}
	}
	  
	/////
	// FileOutputStream
	/////

	private boolean FileOutputStream.isOpen = false;
	private File FileOutputStream.file = null;
	private static Object FileOutputStream.lock = new Object(); // global lock for FileOutputStream operations.

	void around(File file, boolean append) throws FileNotFoundException: execution(FileOutputStream.new(File,boolean)) && args(file, append) {
		synchronized (file.lock) {
	        OpenFile openFile = new OpenFile(file, true, !append);
			openFile.onStart();
	        try {
	        	proceed(file, append);
	        	openFile.onEnd(true);
	        } catch(FileNotFoundException | SecurityException e) {
	        	openFile.onEnd(false);
	        	throw e;
	        }
		}
	}
	  
	after(File file, boolean append, FileOutputStream fos) : execution(FileOutputStream.new(File,boolean)) && args(file, append) && target(fos) {
		fos.isOpen = true;
		fos.file = file;
	}
	    
	void around(FileOutputStream fos): execution(void FileOutputStream.close()) && target(fos) {
		proceed(fos);
		if (fos.isOpen) { // sanity check
			new CloseFile(fos.file).onEnd(true);
	  		fos.isOpen = false;
		}
	}
	  
	void around(FileOutputStream fos, byte[] bytes, int offset, int length) throws IOException: execution(void FileOutputStream.write(byte[],int,int)) && target(fos) && args(bytes,offset,length) {
		synchronized (fos.lock) {
	  		WriteFile writeFile = new WriteFile(fos.file, length);
	  		writeFile.onStart();
	  		try {
		  		proceed(fos, bytes, offset, length);
		  		writeFile.onEnd(true);
	  		} catch (IOException e) {
		  		writeFile.onEnd(false);
	  			throw e;
	  		}
		}
	}
	  
	void around(FileOutputStream fos,int b) throws IOException: execution(void FileOutputStream.write(int)) && target(fos) && args(b) {
		synchronized (fos.lock) {
	  		WriteFile writeFile = new WriteFile(fos.file, 1);
	  		writeFile.onStart();
	  		try {
		  		proceed(fos, b);
		  		writeFile.onEnd(true);
	  		} catch (IOException e) {
		  		writeFile.onEnd(false);
	  			throw e;
	  		}
		}
	}
	  
	boolean around(File file): execution(boolean File.delete()) && target(file) {
		synchronized (file.lock) {
	        DeleteFile deleteFile = new DeleteFile(file);
	        deleteFile.onStart();
	        try {
				boolean deleted = proceed(file);
				deleteFile.onEnd(deleted);
				return deleted;
	        } catch(SecurityException e) {
	        	deleteFile.onEnd(false);
	        	throw e;
	        }
		}
	}
	  
	/////
	// RandomAccessFile
	/////
	
	private boolean RandomAccessFile.isOpen = false;
	private File RandomAccessFile.file = null;
	private static Object RandomAccessFile.lock = new Object(); // global lock for RandomAccessFile operations.
	  
	void around(File file, String mode) throws FileNotFoundException: execution(RandomAccessFile.new(File, String)) && args(file, mode) {
		synchronized(file.lock) {
			boolean writeMode = mode.contains("w");
	       	OpenFile openFile = new OpenFile(file, writeMode, false);
	        openFile.onStart();
	        try {
				proceed(file, mode);
				openFile.onEnd(true);
	        } catch(FileNotFoundException | SecurityException e) {
	        	openFile.onEnd(false);
	        	throw e;
	        }
		}
	}
	  
	after(File file, String mode, RandomAccessFile randomAccessFile) : execution(RandomAccessFile.new(File, String)) && args(file, mode) && target(randomAccessFile) {
    	randomAccessFile.isOpen = true;
    	randomAccessFile.file = file;
    }
	  
	void around(RandomAccessFile file, byte[] bytes, int offset, int length) throws IOException: execution(void RandomAccessFile.write(byte[],int,int)) && target(file) && args(bytes,offset,length) {
		synchronized(file.lock) {
			WriteRandomAccessFile writeFile = new WriteRandomAccessFile(file, length);
			writeFile.onStart();
			try {
				proceed(file, bytes, offset, length);
				
				writeFile.onEnd(true);
			} catch (IOException e) {
				writeFile.onEnd(false);
				throw e;
			}
		}
	}
	  
	void around(RandomAccessFile file, int b) throws IOException: execution(void RandomAccessFile.write(int)) && target(file) && args(b) {
		synchronized(file.lock) {
			WriteRandomAccessFile writeFile = new WriteRandomAccessFile(file, 1);
			writeFile.onStart();
			try {
				proceed(file, b);
				
				writeFile.onEnd(true);
			} catch (IOException e) {
				writeFile.onEnd(false);
				throw e;
			}
		}
	}
	  
	void around(RandomAccessFile file, String s) throws IOException: execution(void RandomAccessFile.writeBytes(String)) && target(file) && args(s) {
		synchronized(file.lock) {
			WriteRandomAccessFile writeFile = new WriteRandomAccessFile(file, s.length());
			writeFile.onStart();
			try {
				proceed(file, s);
				
				writeFile.onEnd(true);
			} catch (IOException e) {
				writeFile.onEnd(false);
				throw e;
			}
		}
	}
	  
	void around(RandomAccessFile file, String s) throws IOException: execution(void RandomAccessFile.writeChars(String)) && target(file) && args(s) {
		synchronized(file.lock) {
			WriteRandomAccessFile writeFile = new WriteRandomAccessFile(file, 2 * s.length());
			writeFile.onStart();
			try {
				proceed(file, s);
				
				writeFile.onEnd(true);
			} catch (IOException e) {
				writeFile.onEnd(false);
				throw e;
			}
		}
	}
	  
	void around(RandomAccessFile file, long newLength) throws IOException: execution(void RandomAccessFile.setLength(long)) && target(file) && args(newLength) {
		synchronized(file.lock) {
			SetLengthRandomAccessFile setLengthFile = new SetLengthRandomAccessFile(file, newLength);
			setLengthFile.onStart();
			try {
				proceed(file, newLength);
				
				setLengthFile.onEnd(true);
			} catch (IOException e) {
				setLengthFile.onEnd(false);
				throw e;
			}
		}
	}
	  
	void around(RandomAccessFile file) throws IOException: execution(void RandomAccessFile.close()) && target(file) {
		synchronized(file.lock) {
			CloseFile closeFile = new CloseFile(file.file);
			closeFile.onStart();
		  
			try {
				proceed(file);
				if (file.isOpen) { // sanity check
					closeFile.onEnd(true);
					file.isOpen = false;
				}
			} catch (IOException e) {
				closeFile.onEnd(false);
				throw e;
			}
		}
	}
}

/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.core.test.bridge;

import java.io.File;
import java.io.IOException;

import com.axmor.eclipse.typescript.core.internal.TypeScriptBridge;

/**
 * @author Konstantin Zaitcev
 */
public class TSBridge10Test {

	//@Test
	public void testCompile() throws IOException {
		File dir = createTempDirectory();
		TypeScriptBridge bridge = new TypeScriptBridge(dir);
		Thread t = new Thread(bridge); 
		t.start();
		
		t.interrupt();
	}

	public static File createTempDirectory() throws IOException {
		final File temp;

		temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

		if (!(temp.delete())) {
			throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
		}

		if (!(temp.mkdir())) {
			throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
		}

		return (temp);
	}
}

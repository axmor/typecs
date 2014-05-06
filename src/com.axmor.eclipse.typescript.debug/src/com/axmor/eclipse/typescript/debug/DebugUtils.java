/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug;

import static com.axmor.eclipse.typescript.debug.launching.TypeScriptDebugConstants.TS_DEBUG_MODEL;
import static org.eclipse.core.runtime.IStatus.ERROR;

import java.io.IOException;
import java.net.ServerSocket;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;

/**
 * @author Konstantin Zaitcev
 */
public final class DebugUtils {

    /** Protect from initialization */
    private DebugUtils() {
        // empty block
    }

    /**
     * Find free unused TCP port.
     * 
     * @return free port or <code>-1</code> if port not found
     */
    public static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            return -1;
        }
    }
    
    public static void error(String message) throws CoreException {
        error(message, null);
    }

    public static void error(String message, Exception ex) throws CoreException {
        throw new CoreException(new Status(ERROR, TS_DEBUG_MODEL, 0, message, ex));
    }
}

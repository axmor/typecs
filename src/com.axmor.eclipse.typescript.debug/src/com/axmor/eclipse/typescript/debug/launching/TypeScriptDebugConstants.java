/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.debug.launching;

import com.axmor.eclipse.typescript.debug.Activator;

/**
 * @author Konstantin Zaitcev
 */
public interface TypeScriptDebugConstants {
    public static final String TS_DEBUG_MODEL = "com.axmor.eclipse.typescript.debug";
    
    public static final String TS_LAUNCH_STANDALONE_PROJECT = Activator.PLUGIN_ID + ".launchStandaloneProject";
    public static final String TS_LAUNCH_STANDALONE_FILE = Activator.PLUGIN_ID + ".launchStandaloneFile";

    public static final String TS_LAUNCH_WEB_HOST = Activator.PLUGIN_ID + ".launchWebHost";
    public static final String TS_LAUNCH_WEB_PORT = Activator.PLUGIN_ID + ".launchWebPort";
    public static final String TS_LAUNCH_WEB_WIP = Activator.PLUGIN_ID + ".launchWebWip";
}

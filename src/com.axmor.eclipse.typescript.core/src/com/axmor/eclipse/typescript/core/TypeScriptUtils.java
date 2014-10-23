/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.core;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import com.google.common.base.Strings;

/**
 * @author Konstantin Zaitcev
 */
public final class TypeScriptUtils {
    /** Protect from initialization. */
    private TypeScriptUtils() {
        // empty block
    }
    
    /**
     * @return path to nodejs runtime.
     * @throws FileNotFoundException
     *             if cannot found nodejs in any locaiton
     */
    public static String findNodeJS() throws FileNotFoundException {
        String[] paths = new String[] { "node", "/usr/local/bin/node" };
        switch (Platform.getOS()) {
        case Platform.OS_WIN32:
            paths = new String[] { "node.exe", "c:\\Program Files\\nodejs\\node.exe",
                    "c:\\Program Files (x86)\\nodejs\\node.exe" };
            break;
        default:
            break;
        }
        for (String path : paths) {
            if (checkNodeJS(path)) {
                return path;
            }
        }
        throw new FileNotFoundException();
    }

    /**
     * @return version of TypeScript compiler
     */
    public static String getTypeScriptVersion() {
        String version = Activator.getDefault().getPreferenceStore().getString("compiler_version");
        
        if (Strings.isNullOrEmpty(version)) {
            version = TypeScriptAPI.DEFAULT_TS_VERSION;
        }
        // backward compatibility
        if ("1.0.0".equals(version)) {
        	version = "1.0";
        }
        return version;
    }

    /**
     * @param path
     *            path to check
     * @return <code>true</code> if nodejs exist in this location
     */
    private static boolean checkNodeJS(String path) {
        try {
            File file = FileLocator.getBundleFile(Activator.getDefault().getBundle());
            ProcessBuilder ps = new ProcessBuilder(path, "-v").directory(file);
            return ps.start().waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}

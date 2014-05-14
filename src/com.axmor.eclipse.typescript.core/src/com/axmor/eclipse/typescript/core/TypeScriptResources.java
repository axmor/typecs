/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.google.common.base.Strings;

/**
 * This class contains different constants and utility methods.
 * 
 * @author Konstantin Zaitcev
 */
public final class TypeScriptResources {
    /** TypeScript file extension. */
    public static final String TS_EXT = "ts";
    /** TypeScript file extension with dot. */
    public static final String TS_EXT_DOT = ".ts";

    /** TypeScript definition file extension. */
    public static final String TS_DEF_EXT = "d.ts";

    /** TypeScript definition file extension with dot. */
    public static final String TS_DEF_EXT_DOT = ".d.ts";

    /** TypeScript standard library definition file. */
    public static final String TS_STD_LIB = "lib.d.ts";

    /** TypeScript standard library definition folder name. */
    public static final String TS_STD_LIB_FOLDER = "std-lib";

    /**
     * Hide from initialization.
     */
    private TypeScriptResources() {
        // empty constructor
    }

    /**
     * @param path
     *            file path
     * @return <code>true</code> if file is TypeScript source or definition
     */
    public static boolean isTypeScriptFile(String path) {
        if (path != null) {
            String lc = path.toLowerCase();
            return lc.endsWith(TS_EXT_DOT) || lc.endsWith(TS_DEF_EXT_DOT);
        }
        return false;
    }

    /**
     * @param path
     *            file path
     * @return <code>true</code> if file is TypeScript definition
     */
    public static boolean isTypeScriptDefinitionFile(String path) {
        if (path != null) {
            return path.toLowerCase().endsWith(TS_DEF_EXT_DOT);
        }
        return false;
    }

    /**
     * @param path
     *            full workspace relatively TypeScript source file path
     * @return source map file path for given TypeScript path based on compilation settings.
     */
    public static String getSourceMapFilePath(String path) {
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
        if (file != null && file.exists()) {
            TypeScriptCompilerSettings settings = TypeScriptCompilerSettings.load(file.getProject());
            if (settings.isSourceMap()) {
                IPath mapFile = new Path(Strings.isNullOrEmpty(settings.getMapRoot()) ? settings.getTarget() : settings
                        .getMapRoot());
                mapFile = file.getProject().getFolder(mapFile).getFullPath();
                if (!settings.isTargetFile()) {
                    IPath relative = new Path(path).makeRelativeTo(file.getProject().getFolder(settings.getTarget()).getFullPath());
                    mapFile = mapFile.append(relative);
                }
                return mapFile.removeFileExtension().addFileExtension("js.map").toString();
            }
        }
        return null;
    }
}

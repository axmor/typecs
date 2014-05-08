/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.launching;

import static com.axmor.eclipse.typescript.debug.DebugUtils.error;
import static com.axmor.eclipse.typescript.debug.launching.TypeScriptDebugConstants.TS_LAUNCH_STANDALONE_FILE;
import static com.axmor.eclipse.typescript.debug.launching.TypeScriptDebugConstants.TS_LAUNCH_STANDALONE_PROJECT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;

import com.axmor.eclipse.typescript.core.TypeScriptUtils;
import com.axmor.eclipse.typescript.debug.DebugUtils;
import com.axmor.eclipse.typescript.debug.model.TypeScriptDebugTarget;

/**
 * Launcher for TypeScript standalone application using NodeJS runtime in debug and run mode.
 * 
 * @author Konstantin Zaitcev
 */
public class LaunchStandaloneConfigurationDelegate implements ILaunchConfigurationDelegate {

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
            throws CoreException {
        List<String> cmd = new ArrayList<>();
        try {
            cmd.add(TypeScriptUtils.findNodeJS());
        } catch (FileNotFoundException ex) {
            error(ex.getMessage(), ex);
        }

        int port = -1;
        if (ILaunchManager.DEBUG_MODE.equals(mode)) {
            port = DebugUtils.findFreePort();
            if (port == -1) {
                error("Unable to find free port");
            }
            cmd.add("--debug-brk=" + port);
        }

        File workingDir = null;
        String mainFile = null;
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
                .getProject(configuration.getAttribute(TS_LAUNCH_STANDALONE_PROJECT, ""));
        if (project != null && project.isAccessible()) {
            workingDir = project.getLocation().toFile();
            String filePath = configuration.getAttribute(TS_LAUNCH_STANDALONE_FILE, (String) null);
            if (filePath != null) {
                IFile file = project.getFile(filePath);
                if (file.exists() && file.isAccessible()) {
                    try {
                        mainFile = file.getLocation().toFile().getCanonicalPath();
                        // FIXME: KOS need find correct JS file
                        mainFile = mainFile.substring(0, mainFile.length() - 2) + "js";
                    } catch (IOException e) {
                        error(e.getMessage(), e);
                    }
                }
            }
        }

        if (workingDir == null || mainFile == null) {
            error("'Project' or 'Main File' does not exist or unavailable.");
        }
        cmd.add(mainFile);

        Process process = DebugPlugin.exec((String[]) cmd.toArray(new String[cmd.size()]), workingDir);
        IProcess p = DebugPlugin.newProcess(launch, process, "NodeJS Runtime");
        if (mode.equals(ILaunchManager.DEBUG_MODE)) {
            IDebugTarget target = new TypeScriptDebugTarget(launch, p, port);
            launch.addDebugTarget(target);
        }
    }
}

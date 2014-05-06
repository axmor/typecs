/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.debug.launching;

import static com.axmor.eclipse.typescript.debug.DebugUtils.error;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

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
 * @author Konstantin Zaitcev
 */
public class LaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

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

        // FIXME: for testing purpose only
        cmd.add("d:\\Programs\\eclipse-4.3.1\\runtime-ts\\warship_sample\\web\\NodeApp.js");
        
        Process process = DebugPlugin.exec((String[]) cmd.toArray(new String[cmd.size()]), null);
        IProcess p = DebugPlugin.newProcess(launch, process, "NodeApp");
        if (mode.equals(ILaunchManager.DEBUG_MODE)) {
            IDebugTarget target = new TypeScriptDebugTarget(launch, p, port);
            launch.addDebugTarget(target);
        }
    }
}

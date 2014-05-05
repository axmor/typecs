/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.debug.launching;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.RuntimeProcess;

import com.axmor.eclipse.typescript.debug.model.TypeScriptDebugTarget;

/**
 * @author Konstantin Zaitcev
 */
public class LaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
            throws CoreException {
        //new TypeScriptDebugProcess(launch, monitor);
        try {
            Process process = Runtime.getRuntime().exec("node --debug-brk=9222 d:\\Programs\\eclipse-4.3.1\\runtime-ts\\warship_sample\\web\\NodeApp.js");
            RuntimeProcess rprocess = new RuntimeProcess(launch, process, "asdasd", null);
            launch.addDebugTarget(new TypeScriptDebugTarget(rprocess));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

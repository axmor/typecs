/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.debug;

import java.net.InetSocketAddress;

import org.chromium.sdk.BrowserFactory;
import org.chromium.sdk.JavascriptVm;
import org.chromium.sdk.StandaloneVm;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;

/**
 * @author Konstantin Zaitcev
 */
public class LaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
            throws CoreException {
        TypeScriptDebugProcess debugProcess = new TypeScriptDebugProcess(launch);
        System.out.println("!!!!");
//        JavascriptVm standaloneVm =
//                BrowserFactory.getInstance().createStandalone(new InetSocketAddress("localhost", 9222), null);
//        standaloneVm.attach(new );
        //JavascriptVm javascriptVm =  standaloneVm;
    }
}

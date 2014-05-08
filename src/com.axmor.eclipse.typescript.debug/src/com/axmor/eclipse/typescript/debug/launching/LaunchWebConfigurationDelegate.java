/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.launching;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import org.chromium.sdk.BrowserFactory;
import org.chromium.sdk.DebugEventListener;
import org.chromium.sdk.JavascriptVm;
import org.chromium.sdk.StandaloneVm;
import org.chromium.sdk.TabDebugEventListener;
import org.chromium.sdk.wip.WipBackend;
import org.chromium.sdk.wip.WipBrowser;
import org.chromium.sdk.wip.WipBrowser.WipTabConnector;
import org.chromium.sdk.wip.WipBrowserFactory;
import org.chromium.sdk.wip.WipBrowserTab;
import org.chromium.sdk.wip.eclipse.BackendRegistry;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

/**
 * Launcher for debug TypeScript application from Web browser remote connection.
 * 
 * @author Konstantin Zaitcev
 */
public class LaunchWebConfigurationDelegate implements ILaunchConfigurationDelegate {

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
            throws CoreException {
        String host = "localhost";
        int port = 9222; 
        int backendId = 0;
        int tabId = 0;
        WipBrowser browser = WipBrowserFactory.INSTANCE.createBrowser(new InetSocketAddress(host, port), null);
        WipBackend backend = BackendRegistry.INSTANCE.getBackends().get(backendId);
        System.out.println(backend.getId());
        WipBrowserTab tab;
        try {
            List<? extends WipTabConnector> tabs = browser.getTabs(backend);
            for (WipTabConnector connector : tabs) {
                System.out.println(connector);
            }
//            tab = browser.getTabs(backend).get(tabId).attach(new TabDebugEventListener() {
//                
//                @Override
//                public void navigated(String arg0) {
//                    System.out.println("asdasd" + arg0);
//                }
//                
//                @Override
//                public DebugEventListener getDebugEventListener() {
//                    System.out.println("get listener");
//                    return null;
//                }
//                
//                @Override
//                public void closed() {
//                    System.out.println("closed");
//                }
//            });
//            JavascriptVm javascriptVm = tab.getJavascriptVm();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // FIXME : KOS need implement
        //IDebugTarget target = new TypeScriptDebugTarget(launch, p, port);
        //launch.addDebugTarget(target);
    }
}

/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.launching;

import static com.axmor.eclipse.typescript.debug.launching.TypeScriptDebugConstants.TS_LAUNCH_WEB_HOST;
import static com.axmor.eclipse.typescript.debug.launching.TypeScriptDebugConstants.TS_LAUNCH_WEB_PORT;
import static com.axmor.eclipse.typescript.debug.launching.TypeScriptDebugConstants.TS_LAUNCH_WEB_WIP;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.chromium.sdk.ConnectionLogger;
import org.chromium.sdk.ConnectionLogger.StreamListener;
import org.chromium.sdk.DebugEventListener;
import org.chromium.sdk.JavascriptVm;
import org.chromium.sdk.TabDebugEventListener;
import org.chromium.sdk.wip.WipBackend;
import org.chromium.sdk.wip.WipBrowser;
import org.chromium.sdk.wip.WipBrowser.WipTabConnector;
import org.chromium.sdk.wip.WipBrowserFactory;
import org.chromium.sdk.wip.WipBrowserFactory.LoggerFactory;
import org.chromium.sdk.wip.WipBrowserTab;
import org.chromium.sdk.wip.eclipse.BackendRegistry;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;

/**
 * Launcher for debug TypeScript application from Web browser remote connection.
 * 
 * @author Konstantin Zaitcev
 */
public class LaunchWebConfigurationDelegate implements ILaunchConfigurationDelegate {

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
            throws CoreException {
        String host = configuration.getAttribute(TS_LAUNCH_WEB_HOST, "localhost");
        int port = configuration.getAttribute(TS_LAUNCH_WEB_PORT, 9222);
        int backendId = configuration.getAttribute(TS_LAUNCH_WEB_WIP, 0);
        WipBrowser browser = WipBrowserFactory.INSTANCE.createBrowser(new InetSocketAddress(host, port),
                new LoggerFactory() {

                    @Override
                    public ConnectionLogger newBrowserConnectionLogger() {
                        return new WebConnectionLogger();
                    }

                    @Override
                    public ConnectionLogger newTabConnectionLogger() {
                        return new WebConnectionLogger();
                    }

                });
        WipBackend backend = BackendRegistry.INSTANCE.getBackends().get(backendId);
        WipBrowserTab tab;
        final AtomicReference<WipTabConnector> tabConnector = new AtomicReference<>();
        try {
            final List<? extends WipTabConnector> tabs = browser.getTabs(backend);
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                @Override
                public void run() {
                    ListDialog dlg = new ListDialog(new Shell());
                    dlg.setInput(tabs);
                    dlg.setContentProvider(new IStructuredContentProvider() {
                        @Override
                        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                        }
                        
                        @Override
                        public void dispose() {
                        }
                        
                        @SuppressWarnings("unchecked")
                        @Override
                        public Object[] getElements(Object inputElement) {
                            List<? extends WipTabConnector> items = (List<? extends WipTabConnector>) inputElement;
                            return (WipTabConnector[]) tabs.toArray(new WipTabConnector[tabs.size()]);
                        }
                    });
                    dlg.setLabelProvider(new LabelProvider() {
                        @Override
                        public String getText(Object element) {
                            if (element instanceof WipTabConnector) {
                                WipTabConnector tab = (WipTabConnector) element;
                                return tab.getTitle() + " - " + tab.getUrl();
                            }
                            return "";
                        }
                    });
                    dlg.setMessage("Select the browser tab to attach:");
                    dlg.setTitle("Tab Selection");
                    if (dlg.open() == Window.OK && dlg.getResult() != null) {
                        tabConnector.set((WipTabConnector) (dlg.getResult()[0]));
                    }
                }
            });

            if (tabConnector.get() != null) {
                tab = tabConnector.get().attach(new TabDebugEventListener() {

                    @Override
                    public void navigated(String arg0) {
                        System.out.println("asdasd" + arg0);
                    }

                    @Override
                    public DebugEventListener getDebugEventListener() {
                        System.out.println("get listener");
                        return null;
                    }

                    @Override
                    public void closed() {
                        System.out.println("closed");
                    }
                });
                JavascriptVm javascriptVm = tab.getJavascriptVm();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // FIXME : KOS need implement
        // IDebugTarget target = new TypeScriptDebugTarget(launch, p, port);
        // launch.addDebugTarget(target);
    }

    private class WebConnectionLogger implements ConnectionLogger {

        @Override
        public StreamListener getIncomingStreamListener() {
            return new WebStreamListener();
        }

        @Override
        public StreamListener getOutgoingStreamListener() {
            return new WebStreamListener();
        }

        @Override
        public void setConnectionCloser(ConnectionCloser connectionCloser) {
        }

        @Override
        public void start() {
        }

        @Override
        public void handleEos() {
        }
    }

    private class WebStreamListener implements StreamListener {

        @Override
        public void addContent(CharSequence text) {
            System.out.println(text);
        }

        @Override
        public void addSeparator() {
            System.out.println("-------------------");
        }
    }
}

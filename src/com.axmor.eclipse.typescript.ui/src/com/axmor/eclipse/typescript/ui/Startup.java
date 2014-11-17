/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.ui;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @author Konstantin Zaitcev
 */
public class Startup implements IStartup {

    /** First start indicator to show TypeScript perspective. */
    private static final String CFG_SHOW_PERSPECTIVE_KEY = "show_perspective";

    /** TypeScript perspective identifier. */
    private static final String TYPESCRIPT_PERSPECTIVE_ID = "com.axmor.eclipse.typescript.perspective";

    @Override
    public void earlyStartup() {
        IEclipsePreferences pref = ConfigurationScope.INSTANCE.getNode(this.getClass().getName());
        if (pref.getBoolean(CFG_SHOW_PERSPECTIVE_KEY, true)) {
            pref.putBoolean(CFG_SHOW_PERSPECTIVE_KEY, false);
            try {
                pref.flush();
            } catch (BackingStoreException e) {
                Activator.error(e);
            }
            final IWorkbench workbench = PlatformUI.getWorkbench();
            workbench.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                    if (window != null && window.getActivePage() != null) {
                        IPerspectiveDescriptor desc = PlatformUI.getWorkbench().getPerspectiveRegistry()
                                .findPerspectiveWithId(TYPESCRIPT_PERSPECTIVE_ID);
                        window.getActivePage().setPerspective(desc);
                    }
                }
            });
        }
    }
}

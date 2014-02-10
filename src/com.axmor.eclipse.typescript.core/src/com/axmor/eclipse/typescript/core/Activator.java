/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.axmor.eclipse.typescript.core;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends AbstractUIPlugin {

    /** The plug-in ID. */
    public static final String PLUGIN_ID = "com.axmor.eclipse.typescript.bridge"; //$NON-NLS-1$

    /** The shared instance. */
    private static Activator plugin;

    /**
     * The constructor.
     */
    public Activator() {
    }

    @Override
    public final void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public final void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
        TypeScriptAPIFactory.stopTypeScriptAPIs();
    }

    /**
     * Returns the shared instance.
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative path.
     * 
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /**
     * Print error message to Error log.
     * 
     * @param e
     *            exception
     */
    public static void error(final Exception e) {
        plugin.getLog().log(new Status(Status.ERROR, PLUGIN_ID, e.getMessage(), e));
    }
}

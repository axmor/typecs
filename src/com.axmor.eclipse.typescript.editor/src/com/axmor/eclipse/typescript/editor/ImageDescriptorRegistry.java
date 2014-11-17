/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Standard class for ui purposes, a registry that maps <code>ImageDescriptors</code> to <code>Image</code>. 
 *
 * @see org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry
 * @author Asya Vorobyova
 *
 */
public class ImageDescriptorRegistry {
    
    /** Initial map size */
    private static final int SIZE = 10;

    /**
     * A store for images
     */
    private HashMap<ImageDescriptor, Image> fRegistry = new HashMap<ImageDescriptor, Image>(SIZE);
    
    /**
     * The display the images managed by this registry are allocated for 
     */
    private Display fDisplay;
    
    /**
     * Creates a new image descriptor registry for the given display. All images
     * managed by this registry will be disposed when the display gets disposed.
     */
    public ImageDescriptorRegistry() {
        fDisplay = Activator.getStandardDisplay();
        Assert.isNotNull(fDisplay);
        hookDisplay();
    }

    /**
     * Returns the image associated with the given image descriptor.
     *
     * @param descriptor the image descriptor for which the registry manages an image,
     *  or <code>null</code> for a missing image descriptor
     * @return the image associated with the image descriptor or <code>null</code>
     *  if the image descriptor can't create the requested image.
     */
    public Image get(ImageDescriptor descriptor) {
        if (descriptor == null) {
            descriptor = ImageDescriptor.getMissingImageDescriptor();
        }    

        Image result = fRegistry.get(descriptor);
        if (result != null) {
            return result;
        }    

        result = descriptor.createImage();
        if (result != null) {
            fRegistry.put(descriptor, result);
        }    
        return result;
    }
    
    /**
     * Disposes all images managed by this registry.
     */
    public void dispose() {
        for (Iterator<Image> iter = fRegistry.values().iterator(); iter.hasNext();) {
            Image image = iter.next();
            image.dispose();
        }
        fRegistry.clear();
    }
    
    /**
     * Makes asynchronous disposing
     */
    private void hookDisplay() {
        fDisplay.disposeExec(new Runnable() {
            public void run() {
                dispose();
            }
        });
    }
}

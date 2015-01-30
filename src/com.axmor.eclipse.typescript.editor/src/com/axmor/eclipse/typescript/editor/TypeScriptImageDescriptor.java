/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

import com.axmor.eclipse.typescript.editor.parser.TypeScriptImageKeys;

/**
 * 
 * @author Asya Vorobyova
 * 
 */
/**
 * @author Asya Vorobyova
 *
 */
public class TypeScriptImageDescriptor extends CompositeImageDescriptor {

    // /** Flag to render the abstract adornment. */
    // public final static int ABSTRACT= 0x001;
    //
    // /** Flag to render the final adornment. */
    // public final static int FINAL= 0x002;
    //
    // /** Flag to render the synchronized adornment. */
    // public final static int SYNCHRONIZED= 0x004;
    
    /** Flag to render the error adornment. */
    public static final int RECURSIVE = 0x004;

    /** Flag to render the static adornment. */
    public static final int STATIC = 0x008;

    /** Flag to render the runnable adornment. */
    public static final int RUNNABLE = 0x010;

    /** Flag to render the warning adornment. */
    public static final int WARNING = 0x020;

    /** Flag to render the error adornment. */
    public static final int ERROR = 0x040; 

    /** Flag to render the 'override' adornment. */
    public static final int OVERRIDES = 0x080;

    /** Flag to render the 'implements' adornment. */
    public static final int IMPLEMENTS = 0x100;

    /** Flag to render the 'constructor' adornment. */
    public static final int CONSTRUCTOR = 0x200;

    /**
     * An image descriptor used as the base image 
     */
    private ImageDescriptor baseImage;
    
    /**
     * Flags indicating which adornments are to be rendered
     */
    private int flags;
    
    /**
     * A size of this composite image 
     */
    private Point size;

    /**
     * Creates a new TypeScriptImageDescriptor.
     * 
     * @param baseImage
     *            an image descriptor used as the base image
     * @param flags
     *            flags indicating which adornments are to be rendered. See
     *            {@link #setAdornments(int)} for valid values.
     */
    public TypeScriptImageDescriptor(ImageDescriptor baseImage, int flags) {
        super();
        this.baseImage = baseImage;
        this.flags = flags;
    }

    @Override
    protected Point getSize() {
        if (size == null) {
            ImageData data = getBaseImage().getImageData();
            setSize(new Point(data.width, data.height));
        }
        return size;
    }

    /**
     * @param size
     *            the size to set
     */
    public void setSize(Point size) {
        this.size = size;
    }

    /**
     * Sets the descriptors adornments. Valid values are: {@link #STATIC}, {@link #CONSTRUCTOR}, or
     * any combination of those.
     * 
     * @param adornments
     *            the image descriptors adornments
     */
    public void setFlags(int adornments) {
        Assert.isTrue(adornments >= 0);
        flags = adornments;
    }

    /**
     * Returns the current adornments.
     * 
     * @return the current adornments
     */
    public int getFlags() {
        return flags;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TypeScriptImageDescriptor)) {
            return false;
        }

        TypeScriptImageDescriptor other = (TypeScriptImageDescriptor) object;
        return (getBaseImage().equals(other.getBaseImage()) && getFlags() == other.getFlags());
    }

    @Override
    public int hashCode() {
        return getBaseImage().hashCode() | getFlags();
    }

    @Override
    protected void drawCompositeImage(int width, int height) {
        ImageData bg = getBaseImage().getImageData();
        if (bg == null) {
            bg = DEFAULT_IMAGE_DATA;
        }
        drawImage(bg, 0, 0);
        drawTopRight();
    }

    /**
     * Gets image data for a given descriptor
     *  
     * @param descriptor 
     * @return the image data
     */
    private ImageData getImageData(ImageDescriptor descriptor) {
        ImageData data = descriptor.getImageData();
        if (data == null) {
            data = DEFAULT_IMAGE_DATA;
        }
        return data;
    }

    /**
     * Gets the base image
     * 
     * @return the base image
     */
    private ImageDescriptor getBaseImage() {
        return baseImage;
    }

    /**
     * Adds an adornment corresponding to a given descriptor to a given point
     * 
     * @param desc an image descriptor
     * @param pos a point to add image
     */
    private void addTopRightImage(ImageDescriptor desc, Point pos) {
        ImageData data = getImageData(desc);
        int x = pos.x - data.width;
        if (x >= 0) {
            drawImage(data, x, pos.y);
            pos.x = x;
        }
    }

    /** 
     * Draws adornment in a top right position
     */
    private void drawTopRight() {
        Point pos = new Point(getSize().x, 0);
        if ((flags & CONSTRUCTOR) != 0) {
            addTopRightImage(TypeScriptUIImages.getImageDescriptor(TypeScriptImageKeys.IMG_CONSTRUCTOR), pos);
        }
        if ((flags & STATIC) != 0) {
            addTopRightImage(TypeScriptUIImages.getImageDescriptor(TypeScriptImageKeys.IMG_STATIC), pos);
        }
        if ((flags & RECURSIVE) != 0) {
            addTopRightImage(TypeScriptUIImages.getImageDescriptor(TypeScriptImageKeys.IMG_RECURSIVE), pos);
        }
    }
    
}

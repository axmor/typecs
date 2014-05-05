/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.debug.ui.model;

import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptModelPresentation implements IDebugModelPresentation {

    @Override
    public void addListener(ILabelProviderListener listener) {
        System.out.println("addListener");
    }

    @Override
    public void dispose() {
        System.out.println("dispose");
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        System.out.println("isLabelProperty");
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        System.out.println("removeListener");
    }

    @Override
    public IEditorInput getEditorInput(Object element) {
        System.out.println("getEditorInput");
        return null;
    }

    @Override
    public String getEditorId(IEditorInput input, Object element) {
        System.out.println("getEditorId");
        return null;
    }

    @Override
    public void setAttribute(String attribute, Object value) {
        System.out.println("setAttribute");
    }

    @Override
    public Image getImage(Object element) {
        System.out.println("getImage");
        return null;
    }

    @Override
    public String getText(Object element) {
        System.out.println("getText");
        return null;
    }

    @Override
    public void computeDetail(IValue value, IValueDetailListener listener) {
        System.out.println("computeDetail");
    }
}

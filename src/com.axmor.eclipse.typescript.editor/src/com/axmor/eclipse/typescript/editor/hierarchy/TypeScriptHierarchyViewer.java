/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor.hierarchy;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * @author kudrin
 *
 */
public class TypeScriptHierarchyViewer extends TreeViewer {
    
    public static final Object EMPTY_ROOT = new Object();
    
    private final CallHierarchyViewPart fPart;    

    private TypeScriptHierarchyContentProvider fContentProvider;
    
    TypeScriptHierarchyViewer(Composite parent, CallHierarchyViewPart part) {
        super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);        
        fPart = part;
        
        fContentProvider = new TypeScriptHierarchyContentProvider(fPart);
        setContentProvider(fContentProvider);
        setLabelProvider(new ViewLabelProvider());
        setAutoExpandLevel(2);
    }    
    
    class ViewLabelProvider extends LabelProvider {
        @Override
        public Image getImage(Object element) {
            TreeRoot obj = (TreeRoot) element;
            return obj.getImage();
        }

        @Override
        public String getText(Object element) {
            TreeRoot obj = (TreeRoot) element;
            return obj.getName() + " : " + obj.getLine();
        }
    }
}

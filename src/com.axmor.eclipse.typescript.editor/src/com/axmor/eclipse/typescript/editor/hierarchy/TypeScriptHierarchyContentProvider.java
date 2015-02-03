/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor.hierarchy;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author kudrin
 *
 */
public class TypeScriptHierarchyContentProvider implements ITreeContentProvider {
    
    static final Object[] NO_CHILDREN = new Object[0];
    
    public TypeScriptHierarchyContentProvider(CallHierarchyViewPart part) {
        super();
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return (Object[]) inputElement;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof TreeRoot) {
            TreeRoot obj = (TreeRoot) parentElement;
            return obj.getChildren();
        }
        return NO_CHILDREN;
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof TreeRoot) {
            TreeRoot obj = (TreeRoot) element;
            return obj.hasChildren();
        }
        return false;
    }  

}

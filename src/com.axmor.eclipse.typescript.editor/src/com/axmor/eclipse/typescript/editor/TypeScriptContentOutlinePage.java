/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import com.axmor.eclipse.typescript.core.TypeScriptUtils;

import us.monoid.json.JSONArray;

/**
 * @author Asya Vorobyova
 * 
 */
public class TypeScriptContentOutlinePage extends ContentOutlinePage implements IAdaptable, IShowInSource {

    /** empty array for content provider assistance */
    static final Object[] NO_CHILDREN = new Object[0];

    /** Current model of TypeScript document */
    private JSONArray model;

    /**
     * @return the model
     */
    public JSONArray getModel() {
        return model;
    }

    /** List of selection change listeners */
    private ListenerList postSelectionChangedListeners = new ListenerList();

    /** A constructor */
    public TypeScriptContentOutlinePage() {
    }

    @Override
    public ShowInContext getShowInContext() {
        return null;
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        return null;
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        TreeViewer viewer = getTreeViewer();

		if ("1.0".equals(TypeScriptUtils.getTypeScriptVersion())) {
			viewer.setContentProvider(new TypeScriptOutlineContentProvider_10(model));
		} else {
			viewer.setContentProvider(new TypeScriptOutlineContentProvider(model));
		}
        viewer.setLabelProvider(new TypeScriptOutlineLabelProvider());
        if (model != null) {
            setViewerInput(model);
        }

        // add a listener for page navigation
        viewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                firePostSelectionChanged(event.getSelection());
            }
        });
    }

    /**
     * Sets a model for a viewer
     * 
     * @param newInput a model of document
     */
    private void setViewerInput(Object newInput) {
        TreeViewer tree = getTreeViewer();

        tree.setInput(newInput);

        if (newInput instanceof List) {
            updateTreeExpansion();
        }
    }

    /**
     * Adds a selection changed listener
     * 
     * @param listener a listener to add
     */
    public void addPostSelectionChangedListener(ISelectionChangedListener listener) {
        postSelectionChangedListeners.add(listener);
    }

    /**
     * Removes a selection changed listener
     * 
     * @param listener a listener to remove
     */
    public void removePostSelectionChangedListener(ISelectionChangedListener listener) {
        postSelectionChangedListeners.remove(listener);
    }

    /**
     * Update tree viewer expansion
     */
    private void updateTreeExpansion() {
        if (model != null) {
            getTreeViewer().expandToLevel(2);
        }
    }

    /**
     * Notifies listeners about selection change
     * 
     * @param selection a changed selection
     */
    private void firePostSelectionChanged(ISelection selection) {
        // create an event
        SelectionChangedEvent event = new SelectionChangedEvent(this, selection);

        // fire the event
        Object[] listeners = postSelectionChangedListeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            ((ISelectionChangedListener) listeners[i]).selectionChanged(event);
        }
    }

    /**
     * Refreshes view on model changes
     * 
     * @param documentModel the changed model
     */
    public void refresh(final JSONArray documentModel) {
    	if (getControl() == null || getControl().isDisposed()) {
        	setPageInput(documentModel);
        	return;
    	}
        getControl().getDisplay().asyncExec(new Runnable() {
            public void run() {
                Control ctrl = getControl();
                if (ctrl != null && !ctrl.isDisposed()) {
                    setPageInput(documentModel);
                    getTreeViewer().refresh();
                    updateTreeExpansion();
                }
            }
        });
    }

    /**
     * Sets a model for the page
     * 
     * @param documentModel a document model
     */
    public void setPageInput(JSONArray documentModel) {
        model = documentModel;
        if (getTreeViewer() != null) {
            setViewerInput(model);
        }
    }
    
}

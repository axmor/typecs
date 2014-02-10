/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.editor.parser.TypeScriptModelKinds;
import com.google.common.base.Throwables;

/**
 * @author Asya Vorobyova
 * 
 */
public class TypeScriptContentOutlinePage extends ContentOutlinePage implements IAdaptable, IShowInSource {

    /** empty array for content provider assistance */
    private static final Object[] NO_CHILDREN = new Object[0];

    /** Current model of TypeScript document */
    private JSONArray model;

    /** List of selection change listeners */
    private ListenerList postSelectionChangedListeners = new ListenerList();

    /** A constructor */
    public TypeScriptContentOutlinePage() {
        // TODO Auto-generated constructor stub
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

        viewer.setContentProvider(new TypeScriptContentProvider());
        viewer.setLabelProvider(new TypeScriptLabelProvider());
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

    /**
     * A content provider mediates between the viewer's model and the viewer itself.
     * 
     * @author Asya Vorobyova
     *
     */
    private class TypeScriptContentProvider implements ITreeContentProvider {
        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof JSONObject) {
                JSONObject obj = (JSONObject) parentElement;
                try {
                    String kind = obj.getString("kind");
                    //we generate children only for interfaces, classes and methods
                    if (kind.equals(TypeScriptModelKinds.Kinds.CONSTRUCTOR.toString())
                            || kind.equals(TypeScriptModelKinds.Kinds.FUNCTION.toString())
                            || kind.equals(TypeScriptModelKinds.Kinds.METHOD.toString())
                            || kind.equals(TypeScriptModelKinds.Kinds.PROPERTY.toString())
                            || kind.equals(TypeScriptModelKinds.Kinds.VAR.toString())) {
                        return NO_CHILDREN;
                    }
                    String name = obj.getString("name");
                    if (obj.getString("containerKind").equals(TypeScriptModelKinds.Kinds.MODULE.toString())) {
                        name = obj.getString("containerName") + "." + name;
                    }
                    return getChildren(kind, name);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (parentElement instanceof JSONArray) {
                return getChildren("", "");
            }
            return NO_CHILDREN;
        }

        /**
         * Looks for children in document model for a given model object
         * 
         * @param kind a kind of the object
         * @param name a name of the object
         * @return an array of children
         */
        private Object[] getChildren(String kind, String name) {
            List<Object> children = new ArrayList<Object>();
            for (int i = 0; i < model.length(); i++) {
                if (!model.isNull(i)) {
                    try {
                        if (model.get(i) instanceof JSONObject) {
                            JSONObject obj = (JSONObject) model.get(i);
                            String parentKind = obj.getString("containerKind");
                            String parentName = obj.getString("containerName");
                            if (parentKind.equals(kind) && parentName.equals(name)) {
                                children.add(obj);
                            }
                        }
                    } catch (JSONException e) {
                        throw Throwables.propagate(e);
                    }
                }
            }
            return children.toArray();
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof JSONObject) {
                JSONObject obj = (JSONObject) element;
                try {
                    String parentKind = obj.getString("containerKind");
                    String parentName = obj.getString("containerName");
                    return getElement(parentKind, parentName);
                } catch (JSONException e) {
                    throw Throwables.propagate(e);
                }
            }
            return null;
        }

        /**
         * Looks in model for a given element
         * 
         * @param elKind a kind of the element
         * @param elName a name of the element
         * @return desired element
         */
        private Object getElement(String elKind, String elName) {
            for (int i = 0; i < model.length(); i++) {
                if (!model.isNull(i)) {
                    try {
                        if (model.get(i) instanceof JSONObject) {
                            JSONObject obj = (JSONObject) model.get(i);
                            String kind = obj.getString("kind");
                            String name = obj.getString("name");
                            if (elKind.equals(kind) && elName.equals(name)) {
                                return obj;
                            }
                        }
                    } catch (JSONException e) {
                        throw Throwables.propagate(e);
                    }
                }
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            return getChildren(element).length > 0;
        }

    }

    /**
     * A label provider to get images and texts for elements viewing
     * 
     * @author Asya Vorobyova
     *
     */
    private class TypeScriptLabelProvider extends LabelProvider {

        @Override
        public Image getImage(Object element) {
            JSONObject obj = (JSONObject) element;
            TypeScriptUIImages imagesFactory = new TypeScriptUIImages();
            return imagesFactory.getImageForModelObject(obj);
        }

        @Override
        public String getText(Object element) {
            try {
                JSONObject obj = (JSONObject) element;
                return obj.getString("name");
            } catch (JSONException e) {
                throw Throwables.propagate(e);
            }
        }
    }
}

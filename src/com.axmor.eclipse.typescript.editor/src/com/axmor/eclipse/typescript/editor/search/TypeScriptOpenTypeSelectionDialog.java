/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.search;

import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.Activator.TypeDocument;
import com.axmor.eclipse.typescript.editor.Activator;
import com.axmor.eclipse.typescript.editor.TypeScriptUIImages;
import com.google.common.base.Throwables;

/**
 * @author Asya Vorobyova
 * 
 */
public class TypeScriptOpenTypeSelectionDialog extends FilteredItemsSelectionDialog {

    /**
     * Section name for dialog settings
     */
    private static final String DIALOG_SETTINGS = "com.axmor.eclipse.typescript.editor.dialogs.OpenTypeSelectionDialog"; //$NON-NLS-1$

    /**
     * Images factory
     */
    private static TypeScriptUIImages imagesFactory = new TypeScriptUIImages();

    /**
     * @author Asya Vorobyova
     * 
     */
    private class OpenTypeLabelProvider implements ILabelProvider {
        /**
         * 
         */
        public OpenTypeLabelProvider() {
        }

        @Override
        public void addListener(ILabelProviderListener listener) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
        }

        @Override
        public Image getImage(Object element) {
            TypeDocument doc = (TypeDocument) element;
            if (doc == null) {
                return null;
            }
            JSONObject obj = new JSONObject();
            try {
                obj.put("kind", doc.getString("type"));
                obj.put("kindModifiers", doc.getString("visibility"));
                obj.put("containerKind", "");
                return imagesFactory.getImageForModelObject(obj);
            } catch (JSONException e) {
                throw Throwables.propagate(e);
            }
        }

        @Override
        public String getText(Object element) {
            TypeDocument doc = (TypeDocument) element;
            if (doc == null) {
                return null;
            }
            return doc.getString("name");
        }

    }

    /**
     * Label provider to implement detail message at the bottom of the dialog
     * 
     */
    private class DetailsLabelProvider implements ILabelProvider {

        @Override
        public void addListener(ILabelProviderListener listener) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
        }

        @Override
        public Image getImage(Object element) {
            return null;
        }

        @Override
        public String getText(Object element) {
            if (element instanceof String) {
                return (String) element;
            }
            TypeDocument doc = (TypeDocument) element;
            if (doc == null) {
                return null;
            }
            return doc.getString("project");
        }

    }

    /**
     * @param shell
     *            parent shell
     * @param multi
     *            multimode to open several editors
     */
    public TypeScriptOpenTypeSelectionDialog(Shell shell, boolean multi) {
        super(shell, multi);
		setInitialPattern("**");
        setListLabelProvider(new OpenTypeLabelProvider());
        setDetailsLabelProvider(new DetailsLabelProvider());
    }

    @Override
    protected Control createExtendedContentArea(Composite parent) {
        return null;
    }

    @Override
    protected IDialogSettings getDialogSettings() {
        IDialogSettings settings = Activator.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS);

        if (settings == null) {
            settings = Activator.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS);
        }

        return settings;
    }

    @Override
    protected IStatus validateItem(Object item) {
        return Status.OK_STATUS;
    }

    @Override
    protected ItemsFilter createFilter() {
        return new ItemsFilter() {
            @Override
			public boolean matchItem(Object item) {
                TypeDocument matchItem = (TypeDocument) item;
                return patternMatcher.matches(matchItem.getString("name"));
            }

            @Override
			public boolean isConsistentItem(Object item) {
                return true;
            }
        };
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Comparator getItemsComparator() {
        return new Comparator() {
            @Override
			public int compare(Object arg0, Object arg1) {
                return ((TypeDocument) arg0).getString("name").toString()
                        .compareTo(((TypeDocument) arg1).getString("name").toString());
            }
        };
    }

    @Override
    protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
            IProgressMonitor progressMonitor) throws CoreException {
        progressMonitor.beginTask("Searching", 100); //$NON-NLS-1$
		Iterable<TypeDocument> results = com.axmor.eclipse.typescript.core.Activator.getDefault().getSearchResults(
                itemsFilter.getPattern());

        for (TypeDocument res : results) {
            contentProvider.add(res, itemsFilter);
            progressMonitor.worked(1);
        }
        progressMonitor.done();
    }

    @Override
    public String getElementName(Object item) {
        return ((TypeDocument) item).getString("name");
    }

}

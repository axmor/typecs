/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.search;

import java.util.Comparator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

import com.axmor.eclipse.typescript.editor.Activator;

/**
 * @author Asya Vorobyova
 * 
 */
public class TypeScriptOpenTypeSelectionDialog extends FilteredItemsSelectionDialog {

    private static final String DIALOG_SETTINGS = "com.axmor.eclipse.typescript.editor.dialogs.OpenTypeSelectionDialog"; //$NON-NLS-1$

    private IProject project;

    /**
     * @param shell
     * @param multi
     * @param project
     */
    public TypeScriptOpenTypeSelectionDialog(Shell shell, boolean multi, IProject project) {
        super(shell, multi);
        this.project = project;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ItemsFilter createFilter() {
        return new ItemsFilter() {
            public boolean matchItem(Object item) {
                return matches(item.toString());
            }

            public boolean isConsistentItem(Object item) {
                return true;
            }
        };
    }

    @Override
    protected Comparator getItemsComparator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
            IProgressMonitor progressMonitor) throws CoreException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getElementName(Object item) {
        // TODO Auto-generated method stub
        return null;
    }

}

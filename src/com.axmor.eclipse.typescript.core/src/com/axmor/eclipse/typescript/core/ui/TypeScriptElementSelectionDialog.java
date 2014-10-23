/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.core.ui;

import static com.axmor.eclipse.typescript.core.TypeScriptResources.TS_EXT;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * This dialog allows select folder or TS file from specified project.
 * 
 * @author Konstantin Zaitcev
 */
public class TypeScriptElementSelectionDialog {

    /** Project. */
    private IProject project;
    /** Title. */
    private String title;
    /** Message. */
    private String message;
    /** Shell. */
    private Shell shell;

    /**
     * 
     * @param shell
     *            shell
     * @param title
     *            title
     * @param message
     *            message
     * @param project
     *            project
     */
    public TypeScriptElementSelectionDialog(Shell shell, String title, String message, IProject project) {
        this.shell = shell;
        this.title = title;
        this.message = message;
        this.project = project;
    }

    /**
     * Opens the dialog.
     * 
     * @param path
     *            initial path that will be selected on dialog open.
     * @param folderOnly
     *            <code>true</code> if display only folder
     * 
     * @return selected project root or resource. If user press <b>cancel</b> it returns
     *         <code>null</code>
     */
    public IResource open(String path, final boolean folderOnly) {
        ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell, new WorkbenchLabelProvider(),
                new BaseWorkbenchContentProvider() {
                    @Override
                    public Object[] getChildren(Object element) {
                        if (element instanceof IWorkspaceRoot) {
                            return new Object[] { project };
                        } else if (element instanceof IContainer) {
                            ArrayList<IResource> childrens = new ArrayList<>();

                            try {
                                for (IResource resource : ((IContainer) element).members()) {
                                    if (folderOnly) {
                                        if (resource.getType() == IResource.FOLDER && !resource.isVirtual()) {
                                            childrens.add(resource);
                                        }
                                    } else {
                                        if ((resource.getType() == IResource.FOLDER && !resource.isVirtual())
                                                || TS_EXT.equalsIgnoreCase(resource.getProjectRelativePath()
                                                        .getFileExtension())) {
                                            childrens.add(resource);
                                        }
                                    }
                                }
                            } catch (CoreException e) {
                                throw Throwables.propagate(e);
                            }

                            return (IResource[]) childrens.toArray(new IResource[childrens.size()]);
                        }
                        return new IResource[0];
                    }
                });
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());

        if (!Strings.isNullOrEmpty(path)) {
            if (project.getFile(path).exists()) {
                dialog.setInitialSelection(project.getFile(path));
            } else {
                dialog.setInitialSelection(project.getFolder(path));
            }
        }

        if (dialog.open() == Window.OK) {
            return (IResource) dialog.getFirstResult();
        }

        return null;

    }
}

/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.axmor.eclipse.typescript.core.Activator.TypeDocument;
import com.axmor.eclipse.typescript.editor.Activator;
import com.axmor.eclipse.typescript.editor.TypeScriptEditor;
import com.axmor.eclipse.typescript.editor.search.TypeScriptOpenTypeSelectionDialog;
import com.google.common.base.Throwables;

/**
 * Handler to create open type dialog to perform type opening
 * 
 * @author Asya Vorobyova
 */
public class OpenTypeHandler extends AbstractHandler {

    /**
     * Currently opened file
     */
    private IFile currentFile = null;

    /**
     * Currently opened project
     */
    private IProject currentProject = null;

    /**
     * Current TS editor
     */
    private TypeScriptEditor currentTSEditor = null;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell parent = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
        IEditorPart editorPart = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .getActiveEditor();
        if (editorPart instanceof TypeScriptEditor) {
            currentTSEditor = (TypeScriptEditor) editorPart;
        }
        currentFile = ((FileEditorInput) editorPart.getEditorInput()).getFile();
        currentProject = currentFile.getProject();
        SelectionDialog dialog = new TypeScriptOpenTypeSelectionDialog(parent, true);
        dialog.setTitle("Open Type");
        dialog.setMessage("Enter type name prefix or pattern (*, ?, or camel case):");

        int result = dialog.open();
        if (result != IDialogConstants.OK_ID) {
            return null;
        }

        Object[] types = dialog.getResult();
        if (types == null || types.length == 0) {
            return null;
        }
        TypeDocument type;
        if (types.length == 1) {
            type = (TypeDocument) types[0];
            openInEditor(type);
            return null;
        }
        for (int i = 0; i < types.length; i++) {
            type = (TypeDocument) types[i];
            openInEditor(type);
        }

        return null;
    }

    /**
     * Opens type in editor
     * 
     * @param type
     *            document type
     */
    private void openInEditor(TypeDocument type) {
        String project = type.getString("project");
        String file = type.getString("file");
        int offset = Integer.parseInt(type.getString("offset"));
        if (currentTSEditor != null) {
            if (currentProject.getName().equals(project)
                    && currentFile.getName().toString().equals(file.split("/")[file.split("/").length - 1])) {
                currentTSEditor.selectAndReveal(offset, 0);
                return;
            }
        }
        IPath path = new Path(file);
        IFile newFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
        if (newFile != null) {
            IEditorPart newEditor;
            try {
                newEditor = IDE.openEditor(Activator.getDefault().getWorkbench().getActiveWorkbenchWindow()
                        .getActivePage(), newFile, true);
                if (newEditor != null && newEditor instanceof AbstractTextEditor) {
                    ((AbstractTextEditor) newEditor).selectAndReveal(offset, 0);
                }
            } catch (PartInitException e) {
                throw Throwables.propagate(e);
            }
        } else {
            ErrorDialog.openError(Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(),
                    "Open Type", "Could not open a type " + type.getString("name"), Status.CANCEL_STATUS);
        }
    }
}

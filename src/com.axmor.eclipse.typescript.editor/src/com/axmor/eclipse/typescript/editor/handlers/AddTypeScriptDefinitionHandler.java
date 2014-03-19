/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;

import us.monoid.web.Resty;

import com.axmor.eclipse.typescript.editor.Activator;
import com.axmor.eclipse.typescript.editor.TypeScriptEditor;
import com.axmor.eclipse.typescript.editor.definitions.AddTypeScriptDefinitionDialog;
import com.axmor.eclipse.typescript.editor.definitions.TypeScriptDefinition;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;

/**
 * @author Konstantin Zaitcev
 */
public class AddTypeScriptDefinitionHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
        IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
        if (activeEditor == null || !(activeEditor instanceof TypeScriptEditor)) {
            return null;
        }
        TypeScriptEditor editor = (TypeScriptEditor) activeEditor;
        IFileEditorInput editorInput = (IFileEditorInput) HandlerUtil.getActiveEditorInput(event);
        IDocument document = editor.getDocumentProvider().getDocument(editorInput);

        AddTypeScriptDefinitionDialog dialog = new AddTypeScriptDefinitionDialog(shell, editorInput.getFile()
                .getProject());
        if (dialog.open() == Window.OK) {
            Object[] result = dialog.getResult();
            for (Object obj : result) {
                TypeScriptDefinition def = (TypeScriptDefinition) obj;
                try {
                    IFile file = dialog.getTargetDir().getFile(new Path(def.getId()));
                    if (!file.exists()) {
                        createFile(file, new Resty().bytes(def.getUrl()).stream());
                    }
                    IPath relativePath = file.getFullPath().makeRelativeTo(editorInput.getFile().getParent().getFullPath());
                    document.replace(0, 0, "/// <reference path=\"" + relativePath + "\"/>\n");
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }

            }
        }
        return null;
    }

    /**
     * @param file
     *            file
     * @param stream
     *            stream
     * @throws Exception
     */
    private void createFile(IFile file, InputStream stream) throws Exception {
        IContainer parent = file.getParent();
        if (parent != null && parent instanceof IFolder && !parent.exists()) {
            createFolder(parent);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteStreams.copy(stream, out);
        file.create(new ByteArrayInputStream(out.toByteArray()), true, null);
    }

    /**
     * @param container
     *            container
     * @throws CoreException
     */
    private void createFolder(IContainer container) throws CoreException {
        if (container != null && container instanceof IFolder && !container.exists()) {
            createFolder(container.getParent());
            ((IFolder) container).create(true, true, null);
        }
    }
}

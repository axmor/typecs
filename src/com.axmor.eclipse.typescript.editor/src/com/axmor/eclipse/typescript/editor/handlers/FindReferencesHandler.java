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
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.progress.IProgressService;

import com.axmor.eclipse.typescript.editor.TypeScriptEditor;
import com.axmor.eclipse.typescript.editor.search.TypeScriptSearchQuery;

/**
 * Handler to find references for a given selection
 * 
 * @author Asya Vorobyova
 *
 */
public class FindReferencesHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        TypeScriptEditor editor = (TypeScriptEditor) HandlerUtil.getActiveEditor(event);
        if (editor == null) {
            return null;
        }
        ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
        int start = selection.getOffset();
        IFile file = ((FileEditorInput) editor.getEditorInput()).getFile();
        TypeScriptSearchQuery query = new TypeScriptSearchQuery(file, start, editor.getApi());
        if (query.canRunInBackground()) {
            /*
             * This indirection with Object as parameter is needed to prevent the loading
             * of the Search plug-in: the VM verifies the method call and hence loads the
             * types used in the method signature, eventually triggering the loading of
             * a plug-in (in this case ISearchQuery results in Search plug-in being loaded).
             */
            NewSearchUI.runQueryInBackground(query);
        } else {
            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
            /*
             * This indirection with Object as parameter is needed to prevent the loading
             * of the Search plug-in: the VM verifies the method call and hence loads the
             * types used in the method signature, eventually triggering the loading of
             * a plug-in (in this case it would be ISearchQuery).
             */
            NewSearchUI.runQueryInForeground(progressService, query);
        }
        return null;
    }

}

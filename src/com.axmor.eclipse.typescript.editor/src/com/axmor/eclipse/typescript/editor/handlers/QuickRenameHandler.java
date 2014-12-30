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
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

import com.axmor.eclipse.typescript.core.TypeScriptAPI;
import com.axmor.eclipse.typescript.editor.TypeScriptEditor;
import com.axmor.eclipse.typescript.editor.rename.QuickRenameAssistProposal;

/**
 * @author kudrin
 *
 */
public class QuickRenameHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
        if (activeEditor == null || !(activeEditor instanceof TypeScriptEditor)) {
            return null;
        }
        TypeScriptEditor editor = (TypeScriptEditor) activeEditor;
        ITextSelection selection= (ITextSelection) editor.getSelectionProvider().getSelection();
        IFile file = ((FileEditorInput) editor.getEditorInput()).getFile();
        TypeScriptAPI api = editor.getApi();
        QuickRenameAssistProposal proposal = new QuickRenameAssistProposal(api, file);
        ((ICompletionProposalExtension2) proposal).apply(editor.getViewer(), (char) 0, 0, selection.getOffset());
        
        return null;
    }

}

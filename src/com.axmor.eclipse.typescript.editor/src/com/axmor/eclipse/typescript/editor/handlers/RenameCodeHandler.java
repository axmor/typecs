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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;

import com.axmor.eclipse.typescript.core.Activator;
import com.axmor.eclipse.typescript.editor.TypeScriptEditor;
import com.axmor.eclipse.typescript.editor.TypeScriptEditorUtils;
import com.axmor.eclipse.typescript.editor.rename.RenameInfo;
import com.axmor.eclipse.typescript.editor.rename.RenameProcessor;
import com.axmor.eclipse.typescript.editor.rename.RenameWizard;

/**
 * @author Konstantin Zaitcev
 */
public class RenameCodeHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
        if (activeEditor == null || !(activeEditor instanceof TypeScriptEditor)) {
            return null;
        }
        TypeScriptEditor editor = (TypeScriptEditor) activeEditor;
        IDocument document = editor.getDocumentProvider().getDocument(HandlerUtil.getActiveEditorInput(event));
        ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();

        IRegion region = TypeScriptEditorUtils.findWord(document, selection.getOffset());
        try {
            String name = document.get(region.getOffset(), region.getLength());
            RenameInfo info = new RenameInfo();
            info.setPath(((IFileEditorInput) editor.getEditorInput()).getFile().getFullPath().toString());
            info.setPosition(selection.getOffset());
            info.setOldName(name);
            RenameProcessor processor = new RenameProcessor(info);
            RenameWizard wizard = new RenameWizard(processor, info);

            RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);

            try {
                String titleForFailedChecks = "";
                op.run(HandlerUtil.getActiveShellChecked(event), titleForFailedChecks);
            } catch (final InterruptedException e) {
                // operation was cancelled
            }
        } catch (BadLocationException e) {
            Activator.error(e);
        }

        return null;
    }
}

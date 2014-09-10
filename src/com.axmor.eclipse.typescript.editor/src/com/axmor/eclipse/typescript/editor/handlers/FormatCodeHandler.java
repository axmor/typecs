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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

import com.axmor.eclipse.typescript.editor.TypeScriptEditor;
import com.google.common.base.Throwables;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * Handler to perform code formatting according to TypeScript API directives
 * 
 * @author Asya Vorobyova
 * 
 */
public class FormatCodeHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        TypeScriptEditor editor = (TypeScriptEditor) HandlerUtil.getActiveEditor(event);
        if (editor == null) {
            return null;
        }
        IDocument document = editor.getDocumentProvider().getDocument(HandlerUtil.getActiveEditorInput(event));
        ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
        int start = selection.getOffset();
        int end = start + selection.getLength();
        IFile file = ((FileEditorInput) editor.getEditorInput()).getFile();
        //if selection length equals 0 we will format the document till the end
        if (selection.getLength() == 0) {
            end = document.getLength();
        }
        editor.getApi().updateFileContent(file, document.get());
        JSONArray formatDetails = editor.getApi().getFormattingCode(file, start, end);

        MultiTextEdit textEdit = new MultiTextEdit();
        try {
            for (int i = 0; i < formatDetails.length(); i++) {
                JSONObject object = formatDetails.getJSONObject(i);
                int minChar = Integer.parseInt(object.getString("minChar"));
                int limChar = Integer.parseInt(object.getString("limChar"));
                if (minChar == end)//Ignore space that added by ets_host.js
                    continue;
                if ((object == null) || (minChar < start) || (minChar > end)) {
                    break;
                }
                if (object.has("text")) {
                    String text = object.getString("text");
                    if (text.isEmpty()) {
                        if (minChar < limChar) {
                            textEdit.addChild(new DeleteEdit(minChar, limChar - minChar));
                        }
                    } else {
                        if (minChar < limChar) {
                            textEdit.addChild(new ReplaceEdit(minChar, limChar - minChar, text));
                        } else if (minChar == limChar) {
                            textEdit.addChild(new InsertEdit(minChar, text));
                        }
                    }
                }
            }
            textEdit.apply(document, TextEdit.CREATE_UNDO);
        } catch (JSONException | MalformedTreeException | BadLocationException e) {
            throw Throwables.propagate(e);
        }
        return null;
    }

}

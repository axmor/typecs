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
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

import com.axmor.eclipse.typescript.editor.TypeScriptEditor;
import com.axmor.eclipse.typescript.editor.TypeScriptEditorUtils;
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
        formatCode(editor, document);
        return null;
    }

    public static void formatCode(TypeScriptEditor editor, IDocument document) {
        IDocumentExtension4 ex4 = (IDocumentExtension4) document;
        ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
        int start = selection.getOffset();
        int end = start + selection.getLength();
        IFile file = ((FileEditorInput) editor.getEditorInput()).getFile();
        // if selection length equals 0 we will format the document till the end
        if (selection.getLength() == 0) {
            start = 0;
            end = document.getLength();
        }
        editor.getApi().updateFileContent(file, document.get());
        JSONArray formatDetails = editor.getApi().getFormattingCode(file, start, end);

        MultiTextEdit textEdit = new MultiTextEdit();
        try {
            for (int i = 0; i < formatDetails.length(); i++) {
                JSONObject object = formatDetails.getJSONObject(i);
                Position position = TypeScriptEditorUtils.getPosition(object);
                if (position.offset == end)// Ignore space that added by ets_host.js
                    continue;
                if ((object == null) || (position.offset < start) || (position.offset > end)) {
                    break;
                }
                if (object.has("text") || object.has("newText")) {
                    String text = object.has("text") ? object.getString("text") : object.getString("newText");
                    if (text.isEmpty()) {
                        if (position.length > 0) {
                            textEdit.addChild(new DeleteEdit(position.offset, position.length));
                        }
                    } else {
                        if (position.length > 0) {
                            textEdit.addChild(new ReplaceEdit(position.offset, position.length, text));
                        } else if (position.length == 0) {
                            textEdit.addChild(new InsertEdit(position.offset, text));
                        }
                    }
                }
            }
            DocumentRewriteSession session = ex4.startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
            textEdit.apply(document, TextEdit.CREATE_UNDO);
            ex4.stopRewriteSession(session);
        } catch (JSONException | MalformedTreeException | BadLocationException e) {
            throw Throwables.propagate(e);
        }
    }
}

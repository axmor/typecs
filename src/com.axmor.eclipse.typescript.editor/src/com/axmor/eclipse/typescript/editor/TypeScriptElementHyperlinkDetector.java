/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.axmor.eclipse.typescript.editor.parser.TypeScriptKeywordRuler;

/**
 * Class that tries to find a hyperlink at a given location in a given text viewer.
 * 
 * @author Asya Vorobyova
 * 
 */
public class TypeScriptElementHyperlinkDetector extends AbstractHyperlinkDetector {

    @Override
    public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
        TypeScriptEditor textEditor = (TypeScriptEditor) getAdapter(ITextEditor.class);
        if (region == null) {
            return null;
        }    

        IDocumentProvider documentProvider = textEditor.getDocumentProvider();
        IEditorInput editorInput = textEditor.getEditorInput();
        IDocument document = documentProvider.getDocument(editorInput);
        IRegion wordRegion = TypeScriptEditorUtils.findWord(document, region.getOffset());
        if (wordRegion == null || wordRegion.getLength() == 0) {
            return null;
        }    
        try {
            String word = document.get(wordRegion.getOffset(), wordRegion.getLength());
            if (TypeScriptKeywordRuler.KEYWORDS.contains(word) && !word.equals("super") && !word.equals("this")) {
                return null;
            }
        } catch (BadLocationException e) {
            Activator.error(e);
        }
        return new IHyperlink[] { new TypeScriptElementHyperlink(textEditor, wordRegion) };
    }

}

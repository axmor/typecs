/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor.compare;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;

import com.axmor.eclipse.typescript.editor.Activator;

/**
 * @author kudrin
 *
 */
public class TypeScriptBracketInserter implements VerifyKeyListener {
    
    private static final char NON_BRACKET = '\u0000';
    
    private ISourceViewer viewer;
    private boolean insertCloseBrackets;

    @Override
    public void verifyKey(VerifyEvent event) {
        if (!event.doit || !insertCloseBrackets) return;

        char closingChar = getClosingChar(event.character);
        if (closingChar == NON_BRACKET) return;
        
        event.doit = processBracketKeyStroke(viewer.getDocument(), viewer.getSelectedRange(), event.character,
                closingChar);
    }
    
    public void setViewer(ProjectionViewer viewer)
    {
        this.viewer = viewer;
    }
    
    public ISourceViewer getViewer() {
        return this.viewer;
    }
    
    public void setInsertCloseBrackets(boolean insertCloseBrackets) {
        this.insertCloseBrackets = insertCloseBrackets;
    }
    
    /**
     * @return if <code>c</code> is one of the bracket characters for
     *         which bracket insertion is enabled, the correspnding
     *         closing bracket character; otherwise NON_BRACKET
     */
    private char getClosingChar(char c)
    {
        switch (c)
        {
        case ')':
        case '(':
            return ')';
        case '>':
        case '<':
            return '>';
        case '}':
        case '{':
            return '}';
        case ']':
        case '[':
            return ']';
        case '\'':
            return '\'';
        case '\"':
            return '"';
        default:
            return NON_BRACKET;
        }
    }
    
    /**
     * @param doc           document to be modified in result of the key stroke
     * @param selection     selection in the document at the time of the key stroke
     *                      (x = offset, y = length) or caret position if there was
     *                      no selection (x, y = 0)
     * @param keystrokeChar character entered by the user
     * @param closingChar   the corresponding "closing" character
     * @return true if the key stroke event should be processed,
     *         false if it should be discarded
     */
    private boolean processBracketKeyStroke(IDocument doc, Point selection, char keystrokeChar, char closingChar) {
        final int offset = selection.x;
        final int length = selection.y;        
        
        try {     
            if (isClosingChar(doc, offset, keystrokeChar)) {
                // The user has just typed a closing char
                
                if (offset + length < doc.getLength() && doc.getChar(offset + length) == closingChar) {
                    // There's already a closing char in front of us, so skip it
                    skipChar();                    
                    return false;
                }
            }
            else {
                // The user has just typed an opening char
                
                if (offset + length < doc.getLength() && doc.getChar(offset + length) == keystrokeChar) {
                    // There's already an opening char in front of us, so skip it
                    skipChar();
                    return false;
                }
                else {
                    doc.replace(offset, 0, String.valueOf(new char[] { keystrokeChar }));
                    doc.replace(offset + length + 1, 0, String.valueOf(new char[] { closingChar }));
                    // ...and position the caret after the entered char
                    skipChar();
                    return false;
                }
            }
            return true;
        } catch (BadLocationException e) {
            Activator.error(e);
            return true;
        }
    }
    
    private void skipChar()
    {
        StyledText text = viewer.getTextWidget();
        text.setCaretOffset(text.getCaretOffset() + 1);
    }
    
    /**
     * @return true if the given character inserted at the given offset
     *         in the document would act as a "closing" character;
     *         false otherwise
     */
    private boolean isClosingChar(IDocument doc, int offset, char c) {
        if (c == '}' || c == ']' || c == '>' || c == ')') {
            return true;
        } else {
            return false;            
        }
    }

}

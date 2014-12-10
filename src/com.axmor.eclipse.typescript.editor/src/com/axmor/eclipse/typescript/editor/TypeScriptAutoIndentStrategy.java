/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import static org.eclipse.swt.SWT.TAB;

/**
 * @author kudrin
 *
 */
public class TypeScriptAutoIndentStrategy extends DefaultIndentLineAutoEditStrategy {
    
    protected final static char[] BRACKETS= { '{', '}', '(', ')', '[', ']', '<', '>' };
    
    private DefaultCharacterPairMatcher bracketMatcher = new DefaultCharacterPairMatcher(BRACKETS);
    
    /*
     * @see org.eclipse.jface.text.IAutoIndentStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
     */
    @Override
    public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
        if (c.doit == false) {
            return;
        }        
        if (c.length == 0 && c.text != null && TextUtilities.endsWith(d.getLegalLineDelimiters(), c.text) != -1) {
            smartIndentAfterNewLine(d, c);
        }
    }

    /**
     * @param d
     * @param c
     */
    private void smartIndentAfterNewLine(IDocument document, DocumentCommand command) {
        int docLength = document.getLength();
        if (command.offset == -1 || docLength == 0) {
            return;
        }

        StringBuffer buf = new StringBuffer(command.text);
        if (newLineInsertedBeforeClosingBracket(document, command)) {
            handleNewLineBeforeClosingBracket(document, command, buf);                
        } else {
            handleNewLineWithinBlock(document, command, buf);
        }
        command.text = buf.toString();
    }
    
    private boolean newLineInsertedBeforeClosingBracket(IDocument document, DocumentCommand command) {
        try {
            return command.offset < document.getLength() && document.getChar(command.offset) == '}';
        } catch (BadLocationException e) {
            Activator.error(e);
            return false;
        }
    }
    
    private void handleNewLineBeforeClosingBracket(IDocument document, DocumentCommand command, 
            StringBuffer textToInsert) {
        try {            
            IRegion block = bracketMatcher.match(document, command.offset + 1);
            if (block == null) {
                return;
            }

            boolean bothBracketsWereOnSameLine = document.getLineOfOffset(block.getOffset()) == 
                    document.getLineOfOffset(command.offset);
            
            textToInsert.append(getIndentOfLine(document, document.getLineOfOffset(block.getOffset())));

            if (bothBracketsWereOnSameLine) {
                textToInsert.append(TAB);
                command.shiftsCaret = false;
                command.caretOffset = command.offset + textToInsert.length();
                textToInsert.append(TextUtilities.getDefaultLineDelimiter(document));
                textToInsert.append(getIndentOfLine(document, document.getLineOfOffset(block.getOffset())));
            }  
        } catch (BadLocationException e) {
            Activator.error(e);
        }
        
        
    }
    
    /**
     * Returns the String at line with the leading whitespace removed.
     * 
     * @returns the String at line with the leading whitespace removed.
     * @param document -
     *            the document being parsed
     * @param line -
     *            the line being searched
     */
    private String getIndentOfLine(IDocument document, int line) {
        if (line > -1) {
            int start;
            try {
                start = document.getLineOffset(line);
                int end = start + document.getLineLength(line) - 1;
                int whiteend = findEndOfWhiteSpace(document, start, end);
                return document.get(start, whiteend - start);
            } catch (BadLocationException e) {
                Activator.error(e);
                return "";
            }        
        } else {
            return "";
        }
    }   
    
    private void handleNewLineWithinBlock(IDocument document, DocumentCommand command, StringBuffer textToInsert) {
        int docLength = document.getLength();
        int p = command.offset == docLength ? command.offset - 1 : command.offset;
        int line;
        try {
            line = document.getLineOfOffset(p);
            int start = document.getLineOffset(line);
            int whiteend = findEndOfWhiteSpace(document, start, command.offset);              
            textToInsert.append(document.get(start, whiteend - start));
            
            String lineText = document.get(start, document.getLineLength(line));
            int openBracketIndex = lineText.indexOf('{'); 
            if (openBracketIndex == -1) {
                return;            
            }
            else {
                textToInsert.append(TAB);
            }
        } catch (BadLocationException e) {
            Activator.error(e);
        }
    }
}

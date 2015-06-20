/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor;

import java.util.Arrays;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.axmor.eclipse.typescript.core.TypeScriptEditorSettings;

/**
 * @author kudrin
 *
 */
public class SmartSemicolonAutoEditStrategy implements IAutoEditStrategy {

    /** String representation of a semicolon. */
    private static final String SEMICOLON = ";";
    /** Char representation of a semicolon. */
    private static final char SEMICHAR = ';';

    private char fCharacter;

    @Override
    public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
        if (command.text == null) {
            return;
        }
        if (command.text.equals(SEMICOLON)) {
            fCharacter = SEMICHAR;
        } else {
            return;
        }
        TypeScriptEditorSettings s = TypeScriptEditorSettings.load();
        if (fCharacter == SEMICHAR && !s.isInsertSemicolons()) {
            return;
        }
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page == null) {
            return;
        }
        IEditorPart part = page.getActiveEditor();
        if (!(part instanceof TypeScriptEditor)) {
            return;
        }
        if (isMultilineSelection(document, command)) {
            return;
        }

        // 1: find concerned line / position in java code, location in statement
        int pos = command.offset;
        ITextSelection line;
        try {
            IRegion l = document.getLineInformationOfOffset(pos);
            line = new TextSelection(document, l.getOffset(), l.getLength());
        } catch (BadLocationException e) {
            return;
        }

        // 2: choose action based on findings (is for-Statement?)
        // for now: compute the best position to insert the new character
        int positionInLine = computeCharacterPosition(document, line, pos - line.getOffset(), fCharacter);
        int position = positionInLine + line.getOffset();
        
        // never position before the current position!
        if (position < pos)
            return;
        // never double already existing content
        if (alreadyPresent(document, fCharacter, position))
            return;        

        try {          
            // 3: modify command
            command.offset= position;
            command.length= 0;
            command.caretOffset= position;
            command.doit= true;
            command.owner= null;
        } catch (MalformedTreeException e) {
            Activator.error(e);
        }
    }

    /**
     * Checks whether a character to be inserted is already present at the insert location (perhaps
     * separated by some whitespace from <code>position</code>.
     *
     * @param document the document we are working on
     * @param position the insert position of <code>ch</code>
     * @param ch the character to be inserted
     * @return <code>true</code> if <code>ch</code> is already present at <code>location</code>, <code>false</code> otherwise
     */
    private boolean alreadyPresent(IDocument document, char ch, int position) {
        //int pos= firstNonWhitespaceForward(document, position, document.getLength());
        try {
            if (position != -1 && document.getChar(position) == ch)
                return true;
        } catch (BadLocationException e) {
        }

        return false;
    }

    private int computeCharacterPosition(IDocument document, ITextSelection line, int offset, char character) {
        String text = line.getText();       
        if (text == null)
            return 0;

        int insertPos;
        if (isForStatement(text, offset)) {
            insertPos = -1; // don't do anything in for statements, as semis are vital part of
                            // these
        } else {
            int nextPartitionPos = line.getOffset() + line.getLength();
            insertPos = startOfWhitespaceBeforeOffset(text, nextPartitionPos);
            // if there is a semi present, return its location as alreadyPresent() will take it
            // out this way.
            if (insertPos > 0 && text.charAt(insertPos - 1) == character)
                insertPos = insertPos - 1;
            else if (insertPos > 0 && text.charAt(insertPos - 1) == '}') {
                int opening = scanBackward(document, insertPos - 1 + line.getOffset(), -1,
                        new char[] { '{' });
                if (opening > -1 && opening < offset + line.getOffset()) {
                    if (computeArrayInitializationPos(document, line, opening - line.getOffset()) == -1) {
                        insertPos = offset;
                    }
                }
            }
        }

        return insertPos;
    }
    
    /**
     * Computes an insert position for an opening brace if <code>offset</code> maps to a position in
     * <code>document</code> that looks like being the RHS of an assignment or like an array definition.
     *
     * @param document the document being modified
     * @param line the current line under investigation
     * @param offset the offset of the caret position, relative to the line start.
     * @return an insert position  relative to the line start if <code>line</code> looks like being an array initialization at <code>offset</code>, -1 otherwise
     */
    private static int computeArrayInitializationPos(IDocument document, ITextSelection line, int offset) {
        // search backward while WS, find = (not != <= >= ==) in default partition
        int pos= offset + line.getOffset();

        if (pos == 0)
            return -1;

        int p= firstNonWhitespaceBackward(document, pos - 1, -1);

        if (p == -1)
            return -1;

        try {

            char ch= document.getChar(p);
            if (ch != '=' && ch != ']')
                return -1;

            if (p == 0)
                return offset;

            p= firstNonWhitespaceBackward(document, p - 1, -1);
            if (p == -1)
                return -1;

            ch= document.getChar(p);
            if (Character.isJavaIdentifierPart(ch) || ch == ']' || ch == '[')
                return offset;

        } catch (BadLocationException e) {
        }
        return -1;
    }
    
    /**
     * Finds the highest position in <code>document</code> such that the position is &lt;= <code>position</code>
     * and &gt; <code>bound</code> and <code>Character.isWhitespace(document.getChar(pos))</code> evaluates to <code>false</code>
     * and the position is in the default partition.
     *
     * @param document the document being modified
     * @param position the first character position in <code>document</code> to be considered
     * @param bound the first position in <code>document</code> to not consider any more, with <code>bound</code> &lt; <code>position</code>
     * @return the highest position of one element in <code>chars</code> in [<code>position</code>, <code>scanTo</code>) that resides in a Java partition, or <code>-1</code> if none can be found
     */
    private static int firstNonWhitespaceBackward(IDocument document, int position, int bound) {
        Assert.isTrue(position < document.getLength());
        Assert.isTrue(bound >= -1);

        try {
            while (position > bound) {
                char ch= document.getChar(position);
                if (!Character.isWhitespace(ch))
                    return position;
                position--;
            }
        } catch (BadLocationException e) {
        }
        return -1;
    }

    /**
     * Finds the highest position in <code>document</code> such that the position is &lt;= <code>position</code>
     * and &gt; <code>bound</code> and <code>document.getChar(position) == ch</code> evaluates to <code>true</code> for at least one
     * ch in <code>chars</code> and the position is in the default partition.
     *
     * @param document the document being modified
     * @param position the first character position in <code>document</code> to be considered
     * @param bound the first position in <code>document</code> to not consider any more, with <code>scanTo</code> &gt; <code>position</code>
     * @param chars an array of <code>char</code> to search for
     * @return the highest position of one element in <code>chars</code> in (<code>bound</code>, <code>position</code>] that resides in a Java partition, or <code>-1</code> if none can be found
     */
    private static int scanBackward(IDocument document, int position, int bound, char[] chars) {
        Assert.isTrue(bound >= -1);
        Assert.isTrue(position < document.getLength() );

        Arrays.sort(chars);

        try {
            while (position > bound) {

                if (Arrays.binarySearch(chars, document.getChar(position)) >= 0)
                    return position;

                position--;
            }
        } catch (BadLocationException e) {
        }
        return -1;
    }

    /**
     * Returns the position in <code>text</code> after which there comes only whitespace, up to
     * <code>offset</code>.
     *
     * @param text the text being searched
     * @param offset the maximum offset to search for
     * @return the smallest value <code>v</code> such that <code>text.substring(v, offset).trim() == 0</code>
     */
    private static int startOfWhitespaceBeforeOffset(String text, int offset) {
        int i= Math.min(offset, text.length());
        for (; i >= 1; i--) {
            if (!Character.isWhitespace(text.charAt(i - 1)))
                break;
        }
        return i;
    }    

    /**
     * Determines whether the current line contains a for statement. Algorithm: any "for" word in
     * the line is a positive, "for" contained in a string literal will produce a false positive.
     *
     * @param line
     *            the line where the change is being made
     * @param offset
     *            the position of the caret
     * @return <code>true</code> if <code>line</code> contains <code>for</code>, <code>false</code>
     *         otherwise
     */
    private static boolean isForStatement(String line, int offset) {
        /* searching for (^|\s)for(\s|$) */
        int forPos = line.indexOf("for"); //$NON-NLS-1$
        if (forPos != -1) {
            if ((forPos == 0 || !Character.isJavaIdentifierPart(line.charAt(forPos - 1)))
                    && (line.length() == forPos + 3 || !Character.isJavaIdentifierPart(line.charAt(forPos + 3))))
                return true;
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the document command is applied on a multi line selection,
     * <code>false</code> otherwise.
     *
     * @param document
     *            the document
     * @param command
     *            the command
     * @return <code>true</code> if <code>command</code> is a multiline command
     */
    private boolean isMultilineSelection(IDocument document, DocumentCommand command) {
        try {
            return document.getNumberOfLines(command.offset, command.length) > 1;
        } catch (BadLocationException e) {
            // ignore
            return false;
        }
    }

}

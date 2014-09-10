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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.axmor.eclipse.typescript.editor.TypeScriptEditor;
import com.google.common.base.Throwables;

/**
 * A handler to toggle comment prefixes on the selected lines.
 * 
 * @author Asya Vorobyova
 *
 */
public class ToggleCommentHandler extends AbstractHandler {

    /** TypeScript document to be changed */
    private IDocument document;
    
    /** The text operation target */
    private ITextOperationTarget operationTarget;
    
    /** The document partitioning */
    private String documentPartitioning;
    
    /** The comment prefix */
    private static final String PREFIX = "//";
    
    
    // Checks if the selected lines are all commented or not and uncomments/comments them
    // respectively.
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        TypeScriptEditor editor = (TypeScriptEditor) HandlerUtil.getActiveEditor(event);
        if (editor == null) {
            return null;
        }
        operationTarget = (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);
        documentPartitioning = editor.getConfiguredDocumentPartitioning();
        document = editor.getDocumentProvider().getDocument(HandlerUtil.getActiveEditorInput(event));
        
        final int operationCode;
        if (isSelectionCommented(selection)) {
            operationCode = ITextOperationTarget.STRIP_PREFIX;
        } else {
            operationCode = ITextOperationTarget.PREFIX;
        }    
        
        Shell shell = HandlerUtil.getActiveShell(event);
        if (!operationTarget.canDoOperation(operationCode)) {
            if (shell != null) {
                MessageDialog.openError(shell, "Toggle comment", "An error occurred while toggling comments");
            }    
            return null;
        }

        Display display = null;
        if (shell != null && !shell.isDisposed()) {
            display = shell.getDisplay();
        }    

        BusyIndicator.showWhile(display, new Runnable() {
            public void run() {
                operationTarget.doOperation(operationCode);
            }
        });
        
        return null;
    }

    /**
     * Is the given selection single-line commented?
     *
     * @param selection Selection to check
     * @return <code>true</code> iff all selected lines are commented
     */
    private boolean isSelectionCommented(ISelection selection) {
        if (!(selection instanceof ITextSelection)) {
            return false;
        }

        ITextSelection textSelection = (ITextSelection) selection;
        if (textSelection.getStartLine() < 0 || textSelection.getEndLine() < 0) {
            return false;
        }

        try {
            IRegion block = getTextBlockFromSelection(textSelection);
            ITypedRegion[] regions = TextUtilities.computePartitioning(document, documentPartitioning,
                    block.getOffset(), block.getLength(), false);

            int[] lines = new int[regions.length * 2]; // [startline, endline, startline, endline,
                                                       // ...]
            for (int i = 0, j = 0; i < regions.length; i++, j += 2) {
                // start line of region
                lines[j] = getFirstCompleteLineOfRegion(regions[i]);
                // end line of region
                int length = regions[i].getLength();
                int offset = regions[i].getOffset() + length;
                if (length > 0) {
                    offset--;
                }    
                lines[j + 1] = (lines[j] == -1 ? -1 : document.getLineOfOffset(offset));
            }

            // Perform the check
            for (int i = 0, j = 0; i < regions.length; i++, j += 2) {
                if (lines[j] >= 0 && lines[j + 1] >= 0) {
                    if (!isBlockCommented(lines[j], lines[j + 1])) {
                        return false;
                    }    
                }    
            }

            return true;
        } catch (BadLocationException e) {
            // should not happen
            throw Throwables.propagate(e);
        }
    }

    /**
     * Creates a region describing the text block (something that starts at the beginning of a line)
     * completely containing the current selection.
     * 
     * @param selection
     *            The selection to use
     * @return the region describing the text block comprising the given selection
     */
    private IRegion getTextBlockFromSelection(ITextSelection selection) {

        try {
            IRegion line = document.getLineInformationOfOffset(selection.getOffset());
            int length = selection.getLength() == 0 ? line.getLength() : selection.getLength()
                    + (selection.getOffset() - line.getOffset());
            return new Region(line.getOffset(), length);

        } catch (BadLocationException e) {
            // should not happen
            throw Throwables.propagate(e);
        }
    }

    /**
     * Returns the index of the first line whose start offset is in the given text range.
     * 
     * @param region
     *            the text range in characters where to find the line
     * @return the first line whose start index is in the given range, -1 if there is no such line
     */
    private int getFirstCompleteLineOfRegion(IRegion region) {

        try {
            final int startLine = document.getLineOfOffset(region.getOffset());

            int offset = document.getLineOffset(startLine);
            if (offset >= region.getOffset()) {
                return startLine;
            }    

            final int nextLine = startLine + 1;
            if (nextLine == document.getNumberOfLines()) {
                return -1;
            }    

            offset = document.getLineOffset(nextLine);
            return (offset > region.getOffset() + region.getLength() ? -1 : nextLine);
        } catch (BadLocationException e) {
            // should not happen
            throw Throwables.propagate(e);
        }
    }

    /**
     * Determines whether each line is prefixed by one of the prefixes.
     * 
     * @param startLine
     *            Start line in document
     * @param endLine
     *            End line in document
     * @return <code>true</code> iff each line from <code>startLine</code> to and including
     *         <code>endLine</code> is prepended by one of the <code>prefixes</code>, ignoring
     *         whitespace at the begin of line
     */
    private boolean isBlockCommented(int startLine, int endLine) {

        try {
            // check for occurrences of prefixes in the given lines
            for (int i = startLine; i <= endLine; i++) {

                IRegion line = document.getLineInformation(i);
                String text = document.get(line.getOffset(), line.getLength());

                int found = text.indexOf(PREFIX);

                if (found == -1) {
                    // found a line which is not commented
                    return false;
                }    

                String s = document.get(line.getOffset(), found);
                s = s.trim();
                if (s.length() != 0) {
                    // found a line which is not commented
                    return false;
                }    
            }

            return true;
        } catch (BadLocationException e) {
            // should not happen
            throw Throwables.propagate(e);
        }
    }

}

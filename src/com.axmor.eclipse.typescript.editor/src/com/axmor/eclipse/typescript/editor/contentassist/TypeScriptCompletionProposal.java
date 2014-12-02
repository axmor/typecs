/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.contentassist;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptCompletionProposal implements ICompletionProposal, ICompletionProposalExtension,
		ICompletionProposalExtension2, ICompletionProposalExtension3,
		ICompletionProposalExtension6 {

    /** Original string for replacement. */
    private String replacementString;
    /** Replacement offset. */
    private int replacementOffset;
    /** Replacement length. */
    private int replacementLength;
    /** Cursor position. */
    private int cursorPosition;
    /** Item image. */
    private Image image;
    /** Replacement content. */
    private String content;
    /** Styled text to display. */
    private StyledString displayStyled;
	/** Additional context information. */
	private String info;
    
    /** Control creator. */
    private IInformationControlCreator controlCreator = new IInformationControlCreator() {
        @Override
        public IInformationControl createInformationControl(Shell parent) {
            return new DefaultInformationControl(parent, true);
        }
    };

    /**
	 * @param replacementString
	 *            replacementString
	 * @param replacementOffset
	 *            replacementOffset
	 * @param replacementLength
	 *            replacementLength
	 * @param cursorPosition
	 *            cursorPosition
	 * @param image
	 *            image
	 * @param content
	 *            content
	 * @param context
	 *            context
	 * @param info
	 *            info
	 */
    public TypeScriptCompletionProposal(String replacementString, int replacementOffset, int replacementLength,
			int cursorPosition, Image image, String content, String context, String info) {
        this.replacementString = replacementString;
        this.replacementOffset = replacementOffset;
        this.replacementLength = replacementLength;
        this.cursorPosition = cursorPosition;
        this.image = image;
        this.content = content;
		this.info = info;
        displayStyled = new StyledString(content + context);
        displayStyled.setStyle(content.length(), context.length(), StyledString.QUALIFIER_STYLER);
    }

    @Override
    public StyledString getStyledDisplayString() {
        return displayStyled;
    }

    @Override
    public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
        try {
            viewer.getDocument().replace(replacementOffset, replacementLength, replacementString);
        } catch (BadLocationException x) {
            // ignore
        }
    }

    @Override
    public void apply(IDocument document, char trigger, int offset) {
    }

    @Override
    public void apply(IDocument document) {
    }

    @Override
    public Point getSelection(IDocument document) {
        return new Point(replacementOffset + cursorPosition, 0);
    }

    @Override
    public String getAdditionalProposalInfo() {
		return info;
    }

    @Override
    public String getDisplayString() {
        return content;
    }

    @Override
    public Image getImage() {
        return image;
    }

    @Override
    public IContextInformation getContextInformation() {
        return null;
    }

    @Override
    public IInformationControlCreator getInformationControlCreator() {
        return controlCreator;
    }

    @Override
    public boolean validate(IDocument document, int offset, DocumentEvent event) {
        try {
            String subString = document.get(replacementOffset, offset - replacementOffset);
            if (subString.length() <= content.length()) {
                String start = content.substring(0, subString.length());
                boolean valid = start.equalsIgnoreCase(subString);
                if (valid) {
                    int delta = (event.fText == null ? 0 : event.fText.length()) - event.fLength;
                    int newLength = Math.max(replacementLength + delta, 0);
                    replacementLength = newLength;
                }
                return valid;
            }
        } catch (BadLocationException e) {
            // ignore exception
        }
        return false;
    }

    @Override
    public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
        return null;
    }

    @Override
    public int getPrefixCompletionStart(IDocument document, int completionOffset) {
        return 0;
    }

    @Override
    public void selected(ITextViewer viewer, boolean smartToggle) {
    }

    @Override
    public void unselected(ITextViewer viewer) {
    }


    @Override
    public boolean isValidFor(IDocument document, int offset) {
        return false;
    }

    @Override
    public char[] getTriggerCharacters() {
        return null;
    }

    @Override
    public int getContextInformationPosition() {
        return 0;
    }
}

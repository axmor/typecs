/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor.contentassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

import com.axmor.eclipse.typescript.core.Activator;

/**
 * @author kudrin
 *
 */
public class TypeScriptContextInformationValidator implements IContextInformationValidator, 
    IContextInformationPresenter {
    
    private int fPosition;
    private ITextViewer fViewer;
    private IContextInformation fInformation;
    private int fCurrentParameter = -1;
    
    public TypeScriptContextInformationValidator() {
        
    }

    @Override
    public void install(IContextInformation info, ITextViewer viewer, int offset) {
        if (fInformation instanceof TypeScriptContextInformation) {
            fPosition = ((TypeScriptContextInformation) fInformation).getPosition().offset;
        }
        else {
            fPosition = offset;
        }
        fViewer = viewer;
        fInformation = info;
        
        fCurrentParameter = -1;        
    }

    @Override
    public boolean isContextInformationValid(int position) {
        try {          
            if (position < fPosition) {
                return false;
            }
            IDocument document= fViewer.getDocument();
            IRegion line= document.getLineInformationOfOffset(fPosition);

            if (position < line.getOffset() || position >= document.getLength()) {
                return false;
            }            
            return getCharCount(document, fPosition, position, "()") == 0;

        } catch (BadLocationException x) {
            return false;
        }
    }

    @Override
    public boolean updatePresentation(int position, TextPresentation presentation) {        
        int currentParameter= -1;
        try {
            currentParameter = getCharCount(fViewer.getDocument(), fPosition, position, ",");
        } catch (BadLocationException e) {
            Activator.error(e);
        }        
        
        if (currentParameter == fCurrentParameter) {
            return false;
        }
        presentation.clear();
        fCurrentParameter = currentParameter;
        
        String s = fInformation.getInformationDisplayString();
        if (fCurrentParameter == -1) {
            presentation.addStyleRange(new StyleRange(0, s.length(), null, null, SWT.BOLD));
            return true;
        }
        int[] commas= computeCommaPositions(s);
        int start= commas[fCurrentParameter] + 1;
        int end= commas[fCurrentParameter + 1];
        if (start > 0) {
            presentation.addStyleRange(new StyleRange(0, start, null, null, SWT.NORMAL));
        }
        if (end > start) {
            presentation.addStyleRange(new StyleRange(start, end - start, null, null, SWT.BOLD));
        }
        if (end < s.length()) {
            presentation.addStyleRange(new StyleRange(end, s.length() - end, null, null, SWT.NORMAL));
        }
        return true;
    }
    
    private int[] computeCommaPositions(String code) {
        final int length= code.length();
        int pos= 0;
        List<Integer> positions= new ArrayList<Integer>();
        positions.add(new Integer(-1));
        while (pos < length) {
            char ch= code.charAt(pos);
            switch (ch) {
                case ',':
                    positions.add(new Integer(pos));
                    break;                
                default:
                    break;
            }
            pos++;
        }
        positions.add(new Integer(length));

        int[] fields= new int[positions.size()];
        for (int i= 0; i < fields.length; i++) {
            fields[i]= positions.get(i).intValue();
        }            
        return fields;
    }
    
    private int getCharCount(IDocument document, final int start, final int end, String delimeters)
            throws BadLocationException {
        int charCount= 0;
        int offset= start;
        while (offset < end) {
            char curr= document.getChar(offset++);
            if (delimeters.indexOf(curr) >= 0) {
                ++ charCount;
            }
        }

        return charCount;
    }

}

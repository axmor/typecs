/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * Provider for quick outline information
 * 
 * @author Asya Vorobyova
 */
public class TypeScriptSourceInfoProvider implements IInformationProvider, IInformationProviderExtension {

    /**
     * Current editor
     */
    private TextEditor editor;
    
    /**
     * Constructor
     * 
     * @param editor current editor
     */
    public TypeScriptSourceInfoProvider(TextEditor editor) {
        this.editor = editor;
    }

    @Override
    public Object getInformation2(ITextViewer textViewer, IRegion subject) {
        // Calls setInput on the quick outline popup dialog
        if ((textViewer == null) || (editor == null)) {
            return null;
        }

        Object selection;
        selection = editor.getSelectionProvider().getSelection();

        // If the input is null, then the dialog does not open
        // Define an empty object for no selection instead of null
        if (selection == null) {
            selection = new Object();
        }
        return selection;
    }

    @Override
    public IRegion getSubject(ITextViewer textViewer, int offset) {
        // Subject used in getInformation2
        if ((textViewer == null) || (editor == null)) {
            return null;
        }
        // Get the selected region
        IRegion region = TypeScriptEditorUtils.findWord(textViewer.getDocument(), offset);
        // Ensure the region is defined.  Define an empty one if it is not.
        if (region == null) {
            return new Region(offset, 0);
        }
        return region;
    }

    /**
     * @param textViewer the viewer in whose document the subject is contained
     * @param subject the text region constituting the information subject
     * @return the information about the subject
     * @deprecated
     */
    public String getInformation(ITextViewer textViewer, IRegion subject) {
        return getInformation2(textViewer, subject).toString();
    }

}

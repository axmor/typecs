/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor.rename;

import static com.axmor.eclipse.typescript.editor.TypeScriptEditorUtils.getPosition;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;

import com.axmor.eclipse.typescript.core.TypeScriptAPI;
import com.axmor.eclipse.typescript.editor.Activator;

/**
 * @author kudrin
 *
 */
public class QuickRenameAssistProposal implements ICompletionProposal, ICompletionProposalExtension2 {
    
    private TypeScriptAPI api;
    private IFile file;
    
    public QuickRenameAssistProposal(TypeScriptAPI api, IFile file) {
        this.api = api;
        this.file = file;
    }

    @Override
    public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
        Point selection = viewer.getSelectedRange();       
        
        JSONArray occurrences = api.getOccurrencesAtPosition(file, offset);
        if (occurrences == null || occurrences.length() == 0) {
            return;
        }
        IDocument document= viewer.getDocument();
        LinkedPositionGroup group= new LinkedPositionGroup();
        try {
            for (int i = 0; i < occurrences.length(); i++) {
                Position pos = getPosition(occurrences.getJSONObject(i));
                group.addPosition(new LinkedPosition(document, pos.getOffset(), pos.getLength(), i));
            }
            LinkedModeModel model= new LinkedModeModel();
            model.addGroup(group);
            model.forceInstall();            
            
            LinkedModeUI ui= new EditorLinkedModeUI(model, viewer);
            ui.setExitPosition(viewer, offset, 0, LinkedPositionGroup.NO_STOP);
            ui.enter();            

            viewer.setSelectedRange(selection.x, selection.y);
        } catch (JSONException | BadLocationException e) {
            Activator.error(e);
        }     

    }

    @Override
    public void selected(ITextViewer viewer, boolean smartToggle) {
    }

    @Override
    public void unselected(ITextViewer viewer) {
    }

    @Override
    public boolean validate(IDocument document, int offset, DocumentEvent event) {
        return false;
    }

    @Override
    public void apply(IDocument arg0) {
        
    }

    @Override
    public String getAdditionalProposalInfo() {
        return null;
    }

    @Override
    public IContextInformation getContextInformation() {
        return null;
    }

    @Override
    public String getDisplayString() {
        return "Rename in file..";
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public Point getSelection(IDocument arg0) {
        return null;
    }

}

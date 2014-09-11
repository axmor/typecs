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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.TypeScriptAPI;
import com.axmor.eclipse.typescript.editor.TypeScriptUIImages;
import com.axmor.eclipse.typescript.editor.parser.TypeScriptModelKinds;
import com.google.common.base.Throwables;

/**
 * A content assist processor which computes completions and sets code completion preferences
 * 
 * @author Asya Vorobyova
 */
public class TypeScriptAssistProcessor implements IContentAssistProcessor {

    /** TypeScript API. */
    private TypeScriptAPI api;
    /** Working file. */
    private IFile file;

    /**
     * @param api
     *            api
     * @param file
     *            file
     */
    public TypeScriptAssistProcessor(TypeScriptAPI api, IFile file) {
        this.api = api;
        this.file = file;
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        JSONObject completionList = api.getCompletion(file, offset);
        try {
            TypeScriptUIImages imagesFactory = new TypeScriptUIImages();
            String replacement = extractPrefix(viewer.getDocument().get(), offset);
            if (!completionList.has("entries")) {
                return new ICompletionProposal[0];
            }
            JSONArray completions = completionList.getJSONArray("entries");
            int completionsLength = completions.length();
            List<ICompletionProposal> result = new ArrayList<ICompletionProposal>(completionsLength);
            for (int i = 0; i < completionsLength; i++) {

                String original = completions.getJSONObject(i).getString("name");
                if (original.length() < replacement.length())
                    continue;
                String prefix = original.substring(0, replacement.length());
                if (prefix.equalsIgnoreCase(replacement)) {
                    String entryName = original;
                    JSONObject details = api.getCompletionDetails(file, offset, entryName);
                    String displayString = entryName;
                    String context = "";
                    if (details != null && details.has("kind")) {
                        String kind = details.getString("kind");
                        if (!TypeScriptModelKinds.Kinds.PRIMITIVE_TYPE.toString().equals(kind) 
                                && !TypeScriptModelKinds.Kinds.KEYWORD.toString().equals(kind)
                                && !TypeScriptModelKinds.Kinds.METHOD.toString().equals(kind)
                                && !TypeScriptModelKinds.Kinds.FUNCTION.toString().equals(kind)) {
                            displayString += " : ";
                        }
                        
                        displayString += details.getString("type");
                        String fullSymbolName = details.getString("fullSymbolName");
                        String[] parts = fullSymbolName.split("\\.");
                        if (parts.length > 1) {
                            String parentName = fullSymbolName.substring(0, fullSymbolName.length()
                                    - parts[parts.length - 1].length() - 1);
                            context = " - " + parentName;
                        }
                        
                        if (TypeScriptModelKinds.Kinds.METHOD.toString().equals(kind)) {
                            original += "()"; 
                        }
                    }
                    TypeScriptCompletionProposal proposal = new TypeScriptCompletionProposal(original, offset
                            - replacement.length(), replacement.length(), original.length(),
                            imagesFactory.getImageForModelObject(completions.getJSONObject(i)), displayString, context);
                    result.add(proposal);
                }
            }
            return (ICompletionProposal[]) result.toArray(new ICompletionProposal[result.size()]);

        } catch (JSONException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Calculates word part before a position corresponding to an offset
     * 
     * @param text
     *            a document to get word in
     * @param offset
     *            the given offset
     * @return the word
     */
    private String extractPrefix(String text, int offset) {
        String currentPrefix;
        int startOfWordToken = offset;

        char token = 'a';
        if (startOfWordToken > 0) {
            token = text.charAt(startOfWordToken - 1);
        }

        while (startOfWordToken > 0 && (Character.isJavaIdentifierPart(token)) && !('$' == token)) {
            startOfWordToken--;
            if (startOfWordToken == 0) {
                break; // word goes right to the beginning of the doc
            }
            token = text.charAt(startOfWordToken - 1);
        }

        if (startOfWordToken != offset) {
            currentPrefix = text.substring(startOfWordToken, offset);
        } else {
            currentPrefix = "";
        }
        return currentPrefix;
    }

    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        return null;
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[] { '.' };
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }

}
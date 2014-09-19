/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.search;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.search.core.text.TextSearchScope;
import org.eclipse.search.internal.ui.text.FileMatch;
import org.eclipse.search.internal.ui.text.LineElement;
import org.eclipse.search.internal.ui.text.SearchResultUpdater;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.TypeScriptAPI;
import com.axmor.eclipse.typescript.editor.Activator;
import com.google.common.base.Throwables;

/**
 * Represents TypeScript search to find all references on a selection in the current project
 * 
 * @author Asya Vorobyova
 * 
 */
@SuppressWarnings("restriction")
public class TypeScriptSearchQuery implements ISearchQuery {

    /**
     * Input file with selection
     */
    private IFile file;

    /**
     * Current project
     */
    private IProject project;

    /**
     * Offset of the selection
     */
    private int position;

    /**
     * TypeScript api
     */
    private TypeScriptAPI api;

    /**
     * Object containing search results to be used in search result page
     * 
     * @see TypeScriptSearchResultPage
     */
    private AbstractTextSearchResult searchResult;

    /**
     * list of matches to fill result
     */
    private ArrayList<Match> cachedMatches;

    /**
     * Constructor
     * 
     * @param file 
     * @param pos 
     * @param api 
     */
    public TypeScriptSearchQuery(IFile file, int pos, TypeScriptAPI api) {
        super();
        this.file = file;
        this.project = file.getProject();
        this.position = pos;
        this.api = api;
        this.cachedMatches = new ArrayList<Match>();
        this.searchResult = new TypeScriptSearchResult(this);
    }

    @Override
    public IStatus run(IProgressMonitor monitor) {
        JSONArray references = api.getReferencesAtPosition(file, position);
        IFile[] projectFiles = TextSearchScope.newSearchScope(new IResource[] { project }, Pattern.compile(".*\\.ts"),
                false).evaluateFilesInScope(null);
        IFile currentFile = projectFiles[0];
        IDocumentProvider provider = new TextFileDocumentProvider();
        IDocument currentDocument;
        for (int i = 0; i < references.length(); i++) {
            try {
                if (references.get(i) instanceof JSONObject) {
                    JSONObject obj = (JSONObject) references.get(i);
                    String fileName = obj.getString("fileName");
                    int segmentsCount = fileName.split("/").length;
                    currentFile = null;
                    for (int j = 0; j < projectFiles.length; j++) {
                        if (projectFiles[j].getFullPath().segmentCount() < segmentsCount)
                            continue;
                        if (projectFiles[j].getFullPath()
                                .removeFirstSegments(projectFiles[j].getFullPath().segmentCount() - segmentsCount)
                                .toString().equals(fileName)) {
                            currentFile = projectFiles[j];
                            break;
                        }
                    }
                    if (currentFile == null) {
                        continue;
                    }
                    int offset = Integer.parseInt(obj.getString("minChar"));
                    provider.connect(currentFile);
                    currentDocument = provider.getDocument(currentFile);
                    int lineNumber = currentDocument.getLineOfOffset(offset);
                    IRegion lineInfo = currentDocument.getLineInformationOfOffset(offset);
                    int lineStart = lineInfo.getOffset();
                    String lineContents = currentDocument.get(lineStart, lineInfo.getLength());
                    LineElement lineElement = getCachedLineElement(offset);
                    if (lineElement == null) {
                        lineElement = new LineElement(currentFile, lineNumber, lineStart, lineContents);
                    }
                    cachedMatches.add(new FileMatch(currentFile, offset, Integer.parseInt(obj.getString("limChar"))
                            - offset, lineElement));
                }
            } catch (JSONException | CoreException | BadLocationException e) {
                throw Throwables.propagate(e);
            }

        }
        Job fillResultJob = new Job("Fill Result") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                AbstractTextSearchResult result = (AbstractTextSearchResult) getSearchResult();
                result.removeAll();
                result.addMatches((Match[]) cachedMatches.toArray(new Match[cachedMatches.size()]));
                cachedMatches.clear();
                String message = "Found " + String.valueOf(result.getMatchCount()) + " matches.";
                return new Status(IStatus.OK, Activator.PLUGIN_ID, 0, message, null);
            }
        };
        fillResultJob.schedule();
        return Status.OK_STATUS;
    }

    /**
     * Tries to get cached line element from cached matches
     * 
     * @param offset current offset
     * @return corresponding line element or null if no such
     */
    private LineElement getCachedLineElement(int offset) {
        if (!cachedMatches.isEmpty()) {
            FileMatch last = (FileMatch) cachedMatches.get(cachedMatches.size() - 1);
            LineElement lineElement = last.getLineElement();
            if (lineElement.contains(offset)) {
                return lineElement;
            }
        }
        return null;
    }

    @Override
    public String getLabel() {
        return "TypeScript Search";
    }

    /**
     * Generates inform message for a search result page
     * 
     * @param nMatches number of results
     * @return the label
     */
    public String getResultLabel(int nMatches) {
        return nMatches + " matches in " + project.getFullPath().toString();
    }

    @Override
    public boolean canRerun() {
        return true;
    }

    @Override
    public boolean canRunInBackground() {
        return true;
    }

    @Override
    public ISearchResult getSearchResult() {
        if (searchResult == null) {
            TypeScriptSearchResult result = new TypeScriptSearchResult(this);
            new SearchResultUpdater(result);
            searchResult = result;
        }
        return searchResult;
    }

}

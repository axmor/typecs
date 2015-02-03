/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.services.IDisposable;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;

/**
 * Bridge to TypeScript Compiler API.
 * 
 * @author Konstantin Zaitcev
 */
public interface TypeScriptAPI extends IDisposable {
    /** The latest default TypeScript version. */
    String DEFAULT_TS_VERSION = "1.3";
    
    /**
     * @param file
     *            TS file.
     * @return list of syntax model elements
     */
    JSONArray getScriptModel(IFile file);

    /**
     * Update file content in case of file editing and dont save.
     * 
     * @param file
     *            file that currently editing
     * @param content
     *            a new content
     */
    void updateFileContent(IFile file, String content);

    /**
     * @param file
     *            for to search
     * @param position
     *            absolute offset in file
     * @return list of completions
     */
    JSONObject getCompletion(IFile file, int position);

    /**
     * @param file
     *            file
     * @param position
     *            absolute offset in file
     * @param entryName
     *            name for lookup
     * @return details about current selected completion
     */
    JSONObject getCompletionDetails(IFile file, int position, String entryName);

    /**
     * @param file
     *            for to search
     * @param position
     *            absolute offset in file
     * @return information of method under this position
     */
    JSONObject getSignature(IFile file, int position);

    /**
     * @param file
     *            file to search
     * @param position
     *            absolute offset in file
     * @return type definition based on position
     */
    JSONArray getTypeDefinition(IFile file, int position);

    /**
     * @param file
     *            file
     * @param settings
     *            compilation settings
     * @return arrays of errors or empty if compilation was successful
     */
    JSONObject compile(IFile file, TypeScriptCompilerSettings settings);

    /**
     * @param file
     *            file to format
     * @param start
     *            start position where formatting rules will be applied
     * @param end
     *            end position where formatting rules will be applied
     * @return arrays of text edits that should be performed according formatting rules
     */
    JSONArray getFormattingCode(IFile file, int start, int end);

    /**
     * @param file
     *            file to search
     * @param position
     *            absolute offset in file
     * @return array of references of declaration under given position
     */
    JSONArray getReferencesAtPosition(IFile file, int position);

    /**
     * @param file
     *            file to search
     * @return array of semantic diagnostics
     */
    JSONArray getSemanticDiagnostics(IFile file);

    /**
     * Notify TS api that a new file was added to project.
     * 
     * @param file
     *            resource that was added
     */
    void addFile(IFile file);
    
    /**
     * @param file file to search
     * @param position absolute offset in the file
     * @return array of references in this file
     */
    JSONArray getOccurrencesAtPosition(IFile file, int position);

	/**
	 * Return syntax tree of file
	 */
	JSONObject getSyntaxTree(IFile file);
}

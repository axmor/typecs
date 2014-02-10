/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.core.internal;

import org.eclipse.core.resources.IFile;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.TypeScriptAPI;
import com.axmor.eclipse.typescript.core.TypeScriptCompilerSettings;
import com.google.common.base.Throwables;

/**
 * Test Implementation for {@link TypeScriptAPI}.
 * 
 * @author Konstantin Zaitcev
 */
public class TypeScriptAPITestImpl implements TypeScriptAPI {

    @Override
    public JSONArray getScriptModel(IFile file) {
        StringBuilder sb = new StringBuilder();
        sb.append("[{");
        sb.append("'containerKind': 'interface',");
        sb.append("'containerName': 'Services.IOutputFile',");
        sb.append("'fileName': 'services/languageService.ts',");
        sb.append("'kind': 'property',");
        sb.append("'kindModifiers': '',");
        sb.append("'minChar': 1,");
        sb.append("'limChar': 100,");
        sb.append("'matchKind': 'exact',");
        sb.append("'name': 'name'");
        sb.append("},");
        sb.append("{");
        sb.append("'containerKind': 'interface',");
        sb.append("'containerName': 'Services.IOutputFile',");
        sb.append("'fileName': 'services/languageService.ts',");
        sb.append("'kind': 'property',");
        sb.append("'kindModifiers': '',");
        sb.append("'limChar': 100,");
        sb.append("'matchKind': 'exact',");
        sb.append("'minChar': 200,");
        sb.append("'name': 'writeByteOrderMark'");
        sb.append("}");
        sb.append("]");
        JSONArray a;
        try {
            a = new JSONArray(sb.toString());
        } catch (JSONException e) {
            throw Throwables.propagate(e);
        }
        return a;
    }

    @Override
    public void dispose() {
        // empty
    }

    @Override
    public void updateFileContent(IFile file, String content) {
        // empty
    }

    @Override
    public JSONObject getCompletion(IFile file, int position) {
        return null;
    }

    @Override
    public JSONObject getSignature(IFile file, int position) {
        return null;
    }

    @Override
    public JSONObject getCompletionDetails(IFile file, int position, String entryName) {
        return null;
    }

    @Override
    public JSONArray getTypeDefinition(IFile file, int position) {
        return null;
    }

    @Override
    public JSONObject compile(IFile file, TypeScriptCompilerSettings settings) {
        return null;
    }

    @Override
    public JSONArray getFormattingCode(IFile file, int start, int end) {
        return null;
    }

    @Override
    public JSONArray getReferencesAtPosition(IFile file, int position) {
        return null;
    }
}

/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.core.internal;

import java.io.IOException;
import java.util.concurrent.Executors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.TypeScriptAPI;
import com.axmor.eclipse.typescript.core.TypeScriptCompilerSettings;
import com.axmor.eclipse.typescript.core.TypeScriptEditorSettings;
import com.google.common.base.Throwables;

/**
 * Implementation for {@link TypeScriptAPI}.
 * 
 * @author Konstantin Zaitcev
 */
public class TypeScriptAPIImpl implements TypeScriptAPI {

    /** Bridge instance. */
    private TypeScriptBridge bridge;

    /**
     * @param project
     *            project
     */
    public TypeScriptAPIImpl(IProject project) {
        IPath location = null;
        if (project != null && project.exists() && project.isAccessible()) {
            location = project.getLocation();
        } else {
            location = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        }

        try {
            bridge = new TypeScriptBridge(location.toFile().getCanonicalFile());
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        Executors.newSingleThreadExecutor().execute(bridge);
    }

    @Override
    public JSONArray getScriptModel(IFile file) {
        JSONObject obj = bridge.invokeBridgeMethod("getScriptLexicalStructure", file, (String) null);
        try {
            return obj.getJSONArray("model");
        } catch (JSONException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void updateFileContent(IFile file, String content) {
        bridge.invokeBridgeMethod("setFileContent", file, content);
    }

    @Override
    public JSONObject getCompletion(IFile file, int position) {
        return bridge.invokeBridgeMethod("getCompletions", file, String.valueOf(position));
    }

    @Override
    public JSONObject getCompletionDetails(IFile file, int position, String entryName) {
        try {
            JSONObject params = new JSONObject();
            params.put("position", String.valueOf(position));
            params.put("entryName", entryName);
            return bridge.invokeBridgeMethod("getCompletionDetails", file, params);
        } catch (JSONException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public JSONObject getSignature(IFile file, int position) {
        return bridge.invokeBridgeMethod("getSignature", file, String.valueOf(position));
    }

    @Override
    public JSONArray getTypeDefinition(IFile file, int position) {
        JSONObject object = bridge.invokeBridgeMethod("getTypeDefinition", file, String.valueOf(position));
        try {
            if (!object.isNull("model")) {
                return object.getJSONArray("model");
            }
            return null;
        } catch (JSONException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public JSONObject compile(IFile file, TypeScriptCompilerSettings settings) {
        try {
            JSONObject params = new JSONObject();
            params.put("propagateEnumConstants", false);
            params.put("watch", false);
            params.put("removeComments", settings.isRemoveComments());
            params.put("noResolve", settings.isNoResolve());
            params.put("allowBool", false);
            params.put("allowAutomaticSemicolonInsertion", true);
            params.put("allowModuleKeywordInExternalModuleReference", false);
            params.put("noImplicitAny", settings.isNoImplicitAny());
            params.put("noLib", false);
            params.put("codeGenTarget", "ES3".equalsIgnoreCase(settings.getTargetVersion()) ? 0 : 1);
            if ("default".equalsIgnoreCase(settings.getModule())) {
                params.put("moduleGenTarget", 0);
            } else if ("commonjs".equalsIgnoreCase(settings.getModule())) {
                params.put("moduleGenTarget", 1);
            } else {
                params.put("moduleGenTarget", 2);
            }
            if (settings.getTarget() != null && settings.getTarget().toLowerCase().endsWith("js")) {
                params.put("outFileOption", settings.getTarget());
                params.put("outDirOption", "");
            } else {
                params.put("outFileOption", "");
                params.put("outDirOption", settings.getTarget());
            }
            params.put("mapSourceFiles", settings.isSourceMap());
            params.put("mapRoot", settings.getMapRoot());
            params.put("sourceRoot", "");
            params.put("generateDeclarationFiles", settings.isGenerateDeclaration());
            params.put("useCaseSensitiveFileResolution", false);
            params.put("gatherDiagnostics", false);
            params.put("updateTC", false);
            params.put("codepage", (String) null);

            JSONObject res = bridge.invokeBridgeMethod("compile", file, params);
            return res;
        } catch (JSONException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public JSONArray getFormattingCode(IFile file, int start, int end) {
        try {
            TypeScriptEditorSettings s = TypeScriptEditorSettings.load();

            JSONObject settings = new JSONObject();
            settings.put("IndentSize", s.getIndentSize());
            settings.put("TabSize", s.getTabSize());
            settings.put("NewLineCharacter", s.getNewLineCharacter());
            settings.put("ConvertTabsToSpaces", s.isConvertTabsToSpaces());

            settings.put("InsertSpaceAfterCommaDelimiter", s.isInsertSpaceAfterCommaDelimiter());
            settings.put("InsertSpaceAfterSemicolonInForStatements", s.isInsertSpaceAfterSemicolon());
            settings.put("InsertSpaceBeforeAndAfterBinaryOperators", s.isInsertSpaceBinaryOperators());
            settings.put("InsertSpaceAfterKeywordsInControlFlowStatements", s.isInsertSpaceAfterKeywords());
            settings.put("InsertSpaceAfterFunctionKeywordForAnonymousFunctions", s.isInsertSpaceAfterFunction());
            settings.put("InsertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis",
                    s.isInsertSpaceAfterNonemptyParenthesis());
            settings.put("PlaceOpenBraceOnNewLineForFunctions", s.isPlaceOpenBraceFunctions());
            settings.put("PlaceOpenBraceOnNewLineForControlBlocks", s.isPlaceOpenBraceControlBlocks());

            JSONObject params = new JSONObject();
            params.put("start", String.valueOf(start));
            params.put("end", String.valueOf(end));
            params.put("settings", settings);

            JSONObject res = bridge.invokeBridgeMethod("getFormattingCode", file, params);
            return res.getJSONArray("model");
        } catch (JSONException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public JSONArray getReferencesAtPosition(IFile file, int position) {
        JSONObject object = bridge.invokeBridgeMethod("getReferencesAtPosition", file, String.valueOf(position));
        try {
            if (!object.isNull("model")) {
                return object.getJSONArray("model");
            }
            return null;
        } catch (JSONException e) {
            throw Throwables.propagate(e);
        }
    }
    
    @Override
    public void dispose() {
        bridge.stop();
    }
}

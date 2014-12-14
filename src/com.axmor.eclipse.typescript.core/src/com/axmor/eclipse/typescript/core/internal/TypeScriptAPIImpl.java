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

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.PlatformUI;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.Activator;
import com.axmor.eclipse.typescript.core.TypeScriptAPI;
import com.axmor.eclipse.typescript.core.TypeScriptCompilerSettings;
import com.axmor.eclipse.typescript.core.TypeScriptEditorSettings;
import com.axmor.eclipse.typescript.core.TypeScriptResources;
import com.google.common.base.Throwables;

/**
 * Implementation for {@link TypeScriptAPI}.
 * 
 * @author Konstantin Zaitcev
 */
public class TypeScriptAPIImpl implements TypeScriptAPI {

    /** Bridge instance. */
    private TypeScriptBridge bridge;
    /** project location. */
    private IPath location;

    /**
     * @param project
     *            project
     */
    public TypeScriptAPIImpl(final IProject project) {
        if (project != null && project.exists() && project.isAccessible()) {
            location = project.getLocation();
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    IFolder libFolder = project.getFolder(TypeScriptResources.TS_STD_LIB_FOLDER);
                    if (!libFolder.exists()) {
                        try {
                            libFolder.create(IResource.VIRTUAL, true, null);
                            IFile libFile = libFolder.getFile(TypeScriptResources.TS_STD_LIB);
                            libFile.createLink(URIUtil.toURI(TypeScriptBridge.getStdLibPath()), IResource.NONE, null);
                        } catch (CoreException e) {
                            Activator.error(e);
                        }
                    }
                }
            });
        } else {
            location = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        }
        initBridge();
    }

    @Override
    public JSONArray getScriptModel(IFile file) {
        checkBridge();
        JSONObject obj = bridge.invokeBridgeMethod("getScriptLexicalStructure", file, (String) null);
        try {
            if (!obj.has("model")) {
                return new JSONArray();
            }
            return obj.getJSONArray("model");
        } catch (JSONException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void updateFileContent(IFile file, String content) {
        checkBridge();
        bridge.invokeBridgeMethod("setFileContent", file, content);
    }

    @Override
    public JSONObject getCompletion(IFile file, int position) {
        checkBridge();
        return bridge.invokeBridgeMethod("getCompletions", file, String.valueOf(position));
    }

    @Override
    public JSONObject getCompletionDetails(IFile file, int position, String entryName) {
        checkBridge();
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
        checkBridge();
		JSONObject object = bridge.invokeBridgeMethod("getSignature", file, String.valueOf(position));
		try {
			if (!object.isNull("model")) {
				return object.getJSONObject("model");
			}
			return new JSONObject();
		} catch (JSONException e) {
			throw Throwables.propagate(e);
		}
    }

    @Override
    public JSONArray getTypeDefinition(IFile file, int position) {
        checkBridge();
        JSONObject object = bridge.invokeBridgeMethod("getTypeDefinition", file, String.valueOf(position));
        try {
            if (!object.isNull("model")) {
                return object.getJSONArray("model");
            }
            return new JSONArray();
        } catch (JSONException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public JSONObject compile(IFile file, TypeScriptCompilerSettings settings) {
        checkBridge();
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

            String settingsTarget = settings.getTarget();
            boolean outputToFile = settingsTarget != null && settingsTarget.toLowerCase().endsWith(".js");
            if (outputToFile) {
                params.put("outFileOption", settingsTarget);
                params.put("outDirOption", "");
            } else {
                params.put("outFileOption", "");
                String outDirOption = settingsTarget;
                if (settings.isTargetRelativePathBasedOnSource()) {
                    IContainer inputFileDir = file.getParent();
                    IPath sourceDir = file.getProject().getFolder(settings.getSource()).getFullPath();
                    IPath relativePath = inputFileDir.getFullPath().makeRelativeTo(sourceDir);
                    outDirOption += "/" + relativePath.toString();
                }
                params.put("outDirOption", outDirOption);
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
        checkBridge();
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
            if (!res.isNull("model")) {
                return res.getJSONArray("model");
            }
            return new JSONArray();
        } catch (JSONException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public JSONArray getReferencesAtPosition(IFile file, int position) {
        checkBridge();
        JSONObject object = bridge.invokeBridgeMethod("getReferencesAtPosition", file, String.valueOf(position));
        try {
            if (!object.isNull("model")) {
                return object.getJSONArray("model");
            }
            return new JSONArray();
        } catch (JSONException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void addFile(IFile file) {
        checkBridge();
        bridge.invokeBridgeMethod("addFile", file, (String) null);
    }

    @Override
    public JSONArray getOccurrencesAtPosition(IFile file, int position) {
        checkBridge();
        JSONObject object = bridge.invokeBridgeMethod("getOccurrencesAtPosition", file, String.valueOf(position));
        try {
            if (!object.isNull("model")) {
                return object.getJSONArray("model");
            }
            return new JSONArray();
        } catch (JSONException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public JSONArray getSemanticDiagnostics(IFile file) {
        checkBridge();
        JSONObject object = bridge.invokeBridgeMethod("getSemanticDiagnostics", file, (String) null);
        try {
            if (!object.isNull("model")) {
                return object.getJSONArray("model");
            }
            return new JSONArray();
        } catch (JSONException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
	public JSONObject getSyntaxTree(IFile file) {
		checkBridge();
		JSONObject object = bridge.invokeBridgeMethod("getSyntaxTree", file, (String) null);
		try {
			if (!object.isNull("model")) {
				return object.getJSONObject("model");
			}
			return null;
		} catch (JSONException e) {
			return null;
		}
	}

	@Override
    public void dispose() {
        bridge.stop();
    }

    /**
     * Initializes TS bridge. 
     */
    private void initBridge() {
        try {
            bridge = new TypeScriptBridge(location.toFile().getCanonicalFile());
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        Executors.newSingleThreadExecutor().execute(bridge);
    }

    /**
     * Checks if bridge is available.
     */
    private void checkBridge() {
        if (bridge.isStopped()) {
            initBridge();
        }
    }
}

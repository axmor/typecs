/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.core.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.Activator;
import com.axmor.eclipse.typescript.core.TypeScriptAPI;
import com.axmor.eclipse.typescript.core.TypeScriptCompilerSettings;
import com.axmor.eclipse.typescript.core.TypeScriptEditorSettings;
import com.axmor.eclipse.typescript.core.TypeScriptResources;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;

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
		JSONObject obj = bridge.invokeBridgeMethod("getScriptLexicalStructure", file);
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
		bridge.invokeBridgeMethod("setFileContent", file, 0, content);
    }

    @Override
    public JSONObject getCompletion(IFile file, int position) {
        checkBridge();
		return bridge.invokeBridgeMethod("getCompletions", file, position);
    }

    @Override
    public JSONObject getCompletionDetails(IFile file, int position, String entryName) {
        checkBridge();
		return bridge.invokeBridgeMethod("getCompletionDetails", file, position, entryName);
    }

    @Override
    public JSONObject getSignature(IFile file, int position) {
        checkBridge();
		JSONObject object = bridge.invokeBridgeMethod("getSignature", file, position);
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
    public JSONObject getSignatureHelpItems(IFile file, int position) {
        checkBridge();
		JSONObject object = bridge.invokeBridgeMethod("getSignatureHelpItems", file, position);
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
		JSONObject object = bridge.invokeBridgeMethod("getTypeDefinition", file, position);
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
	public JSONObject compileTsConfig(IFile file) {
		checkBridge();
		try {
			JSONObject tsConfig = new JSONObject(CharStreams.toString(new InputStreamReader(file.getContents(),
					Charsets.UTF_8)));

			JSONObject res = bridge.invokeBridgeMethod("compile", file, 0, null);
			if (tsConfig.has("compilerOptions") && tsConfig.getJSONObject("compilerOptions") != null) {
				JSONObject options = tsConfig.getJSONObject("compilerOptions");
				final AtomicReference<IResource> outResource = new AtomicReference<>();
				if (options.has("out") && options.getString("out") != null) {
					String out = options.getString("out");
					outResource.set(file.getParent().getFile(new Path(out)));
				}
				if (options.has("outDir") && options.getString("outDir") != null) {
					String out = options.getString("outDir");
					outResource.set(file.getParent().getFolder(new Path(out)));
				}
				if (outResource.get() != null) {
					new UIJob("Refresh target resources") {
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							try {
								outResource.get().refreshLocal(IResource.DEPTH_INFINITE, monitor);
							} catch (CoreException e) {
								Activator.error(e);
								return new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
							}
							return Status.OK_STATUS;
						}
					}.schedule();
				}
			}
			return res;
		} catch (Exception e) {
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
			params.put(
					"codeGenTarget",
					"ES3".equalsIgnoreCase(settings.getTargetVersion()) ? 0 : ("ES6".equalsIgnoreCase(settings
							.getTargetVersion()) ? 2 : 1));
            if ("default".equalsIgnoreCase(settings.getModule())) {
                params.put("moduleGenTarget", 0);
            } else if ("commonjs".equalsIgnoreCase(settings.getModule())) {
                params.put("moduleGenTarget", 1);
            } else {
                params.put("moduleGenTarget", 2);
            }

            String settingsTarget = settings.getTarget();
			final AtomicReference<IResource> targetResource = new AtomicReference<>();
            boolean outputToFile = settingsTarget != null && settingsTarget.toLowerCase().endsWith(".js");
            if (outputToFile) {
                params.put("outFileOption", settingsTarget);
                params.put("outDirOption", "");
				targetResource.set(file.getProject().getFile(settingsTarget));
            } else {
                params.put("outFileOption", "");
                String outDirOption = settingsTarget;
                if (settings.isTargetRelativePathBasedOnSource()) {
                	String folder = Strings.isNullOrEmpty(settings.getSource()) ? "./" : settings.getSource();
                	final String rootDir = file.getProject().getFolder(folder).getLocation().toFile().getAbsolutePath().replace('\\', '/');
                   	params.put("rootDir", rootDir);
                }
				if (Strings.isNullOrEmpty(outDirOption)) {
					outDirOption = file.getParent().getProjectRelativePath().toString();
				}
				targetResource.set(Strings.isNullOrEmpty(settingsTarget) ? file.getProject() : file.getProject()
						.findMember(settingsTarget));
				params.put("outDirOption", Strings.isNullOrEmpty(outDirOption) ? "." : outDirOption);
            }

            params.put("mapSourceFiles", settings.isSourceMap());
            params.put("mapRoot", settings.isSourceMap() ? settings.getMapRoot() : "");
            params.put("sourceRoot", "");
            params.put("generateDeclarationFiles", settings.isGenerateDeclaration());
            params.put("useCaseSensitiveFileResolution", false);
            params.put("gatherDiagnostics", false);
            params.put("updateTC", false);
            params.put("codepage", (String) null);

			JSONObject res = bridge.invokeBridgeMethod("compile", file, 0, params.toString());
			if (targetResource.get() != null) {
				new UIJob("Refresh target resources") {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						try {
							targetResource.get().refreshLocal(IResource.DEPTH_INFINITE, monitor);
						} catch (CoreException e) {
							Activator.error(e);
							return new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
						}
						return Status.OK_STATUS;
					}
				}.schedule();
			}
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

			JSONObject res = bridge.invokeBridgeMethod("getFormattingCode", file, 0, params.toString());
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
		JSONObject object = bridge.invokeBridgeMethod("getReferencesAtPosition", file, position);
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
		bridge.invokeBridgeMethod("addFile", file, 0, file.getLocation().toFile().getAbsolutePath().replace('\\', '/'));
    }  

    @Override
    public JSONArray getOccurrencesAtPosition(IFile file, int position) {
        checkBridge();
		JSONObject object = bridge.invokeBridgeMethod("getOccurrencesAtPosition", file, position);
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
		JSONObject object = bridge.invokeBridgeMethod("getSemanticDiagnostics", file);
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
		JSONObject object = bridge.invokeBridgeMethod("getSyntaxTree", file);
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
	public JSONArray getReferences(IFile file) {
		checkBridge();
		JSONObject object = bridge.invokeBridgeMethod("getReferences", file);
		try {
			if (!object.isNull("model")) {
				return object.getJSONArray("model");
			}
			return null;
		} catch (JSONException e) {
			return null;
		}
	}
	
	@Override
	public JSONArray getIdentifiers(IFile file) {
		checkBridge();
		JSONObject object = bridge.invokeBridgeMethod("getIdentifiers", file);
		try {
			if (!object.isNull("model")) {
				return object.getJSONArray("model");
			}
			return new JSONArray();
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

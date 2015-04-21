/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.axmor.eclipse.typescript.builder.builder;

import static com.axmor.eclipse.typescript.core.TypeScriptResources.isTypeScriptDefinitionFile;
import static com.axmor.eclipse.typescript.core.TypeScriptResources.isTypeScriptFile;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.Activator;
import com.axmor.eclipse.typescript.core.TypeScriptAPIFactory;
import com.axmor.eclipse.typescript.core.TypeScriptCompilerSettings;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * TypeScript builder.
 * 
 * @author Konstantin Zaitcev
 */
public class TypescriptBuilder extends IncrementalProjectBuilder {
    private static final int WORK_TOTAL = 100;
    private static final int WORK_BEFORE_COMPILER = 2;
    private static final int WORK_IN_COMPILER = WORK_TOTAL - WORK_BEFORE_COMPILER;

    /** Constant for builder identifier. */
    public static final String BUILDER_ID = "com.axmor.eclipse.typescript.builder.typescriptBuilder";
    /** Constant for marker type. */
    private static final String MARKER_TYPE = "com.axmor.eclipse.typescript.builder.tsProblem";

    @Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
        getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
    }

    @Override
    protected IProject[] build(int kind, Map<String, String> args, final IProgressMonitor monitor) throws CoreException {
        SubMonitor progress = SubMonitor.convert(monitor, WORK_TOTAL);
        SubMonitor beforeCompileProgress = progress.newChild(WORK_BEFORE_COMPILER);
        beforeCompileProgress.setWorkRemaining(WORK_BEFORE_COMPILER);

        // check if no TS file was modified and incremental build
		Set<IFile> toCompile = null; // null when FULL_BUILD or CLEAN_BUILD
        if (kind == INCREMENTAL_BUILD || kind == AUTO_BUILD) {
			toCompile = getFilesToCompile(getDelta(getProject()));
			if (toCompile.isEmpty()) {
                return null;
            }
        }
        getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);

        final TypeScriptCompilerSettings settings = TypeScriptCompilerSettings.load(getProject());

        if (settings.isSourceFile()) {
            // single file compilation
            compileFile(getProject().getFile(settings.getSource()), settings, monitor);
        } else if (Strings.isNullOrEmpty(settings.getSource()) || getProject().getFolder(settings.getSource()).exists()) {
			if (toCompile == null) {
				// folder compilation
				IResource res = Strings.isNullOrEmpty(settings.getSource()) ? getProject() : getProject().getFolder(
						settings.getSource());
				toCompile = getFilesToCompile(res);
			}
            beforeCompileProgress.done();

			// remove all definition files
			Iterator<IFile> it = toCompile.iterator();
			while (it.hasNext()) {
				if (isTypeScriptDefinitionFile(it.next().getName())) {
					it.remove();
				}
			}

			SubMonitor compilerMonitor = progress.newChild(WORK_IN_COMPILER).setWorkRemaining(toCompile.size());
			for (IFile iFile : toCompile) {
				if (compilerMonitor.isCanceled()) {
					return null;
				}
				compileFile(iFile, settings, compilerMonitor.newChild(1));
			}
        }
        return null;
    }

	/**
	 * @param delta
	 * @return
	 * @throws CoreException
	 */
	private Set<IFile> getFilesToCompile(IResourceDelta delta) throws CoreException {
		final Set<IFile> files = new HashSet<>();
		delta.accept(new IResourceDeltaVisitor() {
			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {
				acceptTypeScriptFileWithDependencies(delta.getResource(), files);
				return true;
			}
		});
		return files;
	}

	/**
	 * @param delta
	 * @return
	 * @throws CoreException
	 */
	private Set<IFile> getFilesToCompile(IResource res) throws CoreException {
		final Set<IFile> files = new HashSet<>();
		res.accept(new IResourceVisitor() {
			@Override
			public boolean visit(IResource resource) throws CoreException {
				acceptTypeScriptFileWithDependencies(resource, files);
				return true;
			}
		});
		return files;
	}

	private void acceptTypeScriptFileWithDependencies(IResource resource, Set<IFile> files) {
		if (resource.getType() == IResource.FILE) {
			if (isTypeScriptFile(resource.getName())) {
				files.add((IFile) resource);
				for (String file : Activator.getDefault().getFileUsage(resource.getFullPath().toString())) {
					IFile iFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(file));
					if (iFile.exists() && !files.contains(iFile)) {
						acceptTypeScriptFileWithDependencies(iFile, files);
					}
				}
			}
		}
	}

    static boolean isTypeScriptFileToCompile(IResource resource) {
        return resource.getType() == IResource.FILE && isTypeScriptFile(resource.getName())
                && !isTypeScriptDefinitionFile(resource.getName());
    }

    /**
     * Compile one file use compiler settings.
     * 
     * @param file
     *            file to compile
     * @param settings
     *            compiler settings
     * @param monitor
     *            progress monitor
     */
    private void compileFile(IFile file, TypeScriptCompilerSettings settings, IProgressMonitor monitor) {
        monitor.setTaskName("Compiling file " + file.getFullPath());
        JSONObject json = TypeScriptAPIFactory.getTypeScriptAPI(getProject()).compile(file, settings);
        try {
            if (json.has("files")) {
                JSONArray files = json.getJSONArray("files");
                for (int i = 0; i < files.length(); i++) {
                    String fileName = files.getString(i);
                    IFile ifile = getFileByPath(fileName);
                    if (ifile != null) {
                        ifile.refreshLocal(IResource.DEPTH_ZERO, monitor);
                    }
                }
            }

            if (json.has("errors")) {
                JSONArray errors = json.getJSONArray("errors");
                for (int i = 0; i < errors.length(); i++) {
                    JSONObject error = errors.getJSONObject(i);
                    IMarker marker = null;
                    if (error.has("file") && !error.isNull("file")) {
                        IFile ifile = getFileByPath(error.getString("file"));
                        marker = (ifile != null ? ifile : getProject()).createMarker(MARKER_TYPE);
                    } else {
                        marker = getProject().createMarker(MARKER_TYPE);
                    }
                    marker.setAttribute(IMarker.MESSAGE, error.getString("text"));
                    marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
                    if (error.has("code")) {
                        marker.setAttribute(IMarker.LOCATION, "TS" + error.getInt("code"));
                    }
    
                    if (error.has("severity")) {
                        switch (error.getInt("severity")) {
                        case 0:
                            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
                            break;
                        case 1:
                            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                            break;
                        default:
                            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
                            break;
                        }
                    }
    
                    marker.setAttribute(IMarker.CHAR_START, error.getInt("start"));
                    marker.setAttribute(IMarker.CHAR_END, error.getInt("start") + error.getInt("length"));
                }
            }
        } catch (JSONException | CoreException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * @param path
     *            absolute or relative OS file path
     * @return IFile relative to string path if path not found it returns <code>null</code>
     */
    private IFile getFileByPath(String path) {
		URI uri = URIUtil.toURI(Path.fromOSString(path));
		if (!uri.isAbsolute()) {
			return getProject().getFile(Path.fromOSString(path));
		} else {
			IFile[] ifiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
			for (IFile ifile : ifiles) {
				if (ifile.getProject() == getProject()) {
					return ifile;
				}
			}
		}
        return null;
    }
}

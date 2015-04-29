/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.core;

import static com.axmor.eclipse.typescript.core.TypeScriptResources.TS_EXT_DOT;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import com.google.common.base.Strings;

/**
 * TypeScript compiler settings.
 * 
 * @author Konstantin Zaitcev
 */
public final class TypeScriptCompilerSettings {
    /** by default behave the same way as in older version */
    public static final boolean TARGET_RELATIVE_PATH_BASED_ON_SOURCE_DEFAULT = false;

    /** Source file or directory . */
    private String source;
    /** Target file or directory. */
    private String target;
    /**
     * relative path from {@link #target} to output file is detected based on relative path from
     * {@link #source} to input file
     */
    private boolean targetRelativePathBasedOnSource = TARGET_RELATIVE_PATH_BASED_ON_SOURCE_DEFAULT;
    /** NoResolve. */
    private boolean noResolve;
    /** NoImplicitAny. */
    private boolean noImplicitAny;
    /** Source Map. */
    private boolean sourceMap;
    /** Module. */
    private String module;
    /** Target version. */
    private String targetVersion;
    /** Generate declaration. */
    private boolean generateDeclaration;
    /** Remove comments. */
    private boolean removeComments;
    /** Folder to map root. */
    private String mapRoot;

    /** Instance to eclipse preference store. */
    private final IEclipsePreferences pref;
    /** Project. */
    private IProject project;

    /**
     * @param project
     *            project
     * @param pref
     *            preference store.
     */
    private TypeScriptCompilerSettings(IProject project, IEclipsePreferences pref) {
        this.project = project;
        this.pref = pref;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source
     *            the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param target
     *            the target to set
     */
    public void setTarget(String target) {
        this.target = target;
    }

    public boolean isTargetRelativePathBasedOnSource() {
        return targetRelativePathBasedOnSource;
    }

    public void setTargetRelativePathBasedOnSource(boolean targetRelativePathBasedOnSource) {
        this.targetRelativePathBasedOnSource = targetRelativePathBasedOnSource;
    }

    /**
     * @return the noResolve
     */
    public boolean isNoResolve() {
        return noResolve;
    }

    /**
     * @param noResolve
     *            the noResolve to set
     */
    public void setNoResolve(boolean noResolve) {
        this.noResolve = noResolve;
    }

    /**
     * @return the noImplicitAny
     */
    public boolean isNoImplicitAny() {
        return noImplicitAny;
    }

    /**
     * @param noImplicitAny
     *            the noImplicitAny to set
     */
    public void setNoImplicitAny(boolean noImplicitAny) {
        this.noImplicitAny = noImplicitAny;
    }

    /**
     * @return the sourceMap
     */
    public boolean isSourceMap() {
        return sourceMap;
    }

    /**
     * @param sourceMap
     *            the sourceMap to set
     */
    public void setSourceMap(boolean sourceMap) {
        this.sourceMap = sourceMap;
    }

    /**
     * @return the module
     */
    public String getModule() {
        return module;
    }

    /**
     * @param module
     *            the module to set
     */
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * @return the targetVersion
     */
    public String getTargetVersion() {
        return targetVersion;
    }

    /**
     * @param targetVersion
     *            the targetVersion to set
     */
    public void setTargetVersion(String targetVersion) {
        this.targetVersion = targetVersion;
    }

    /**
     * @return the declaration
     */
    public boolean isGenerateDeclaration() {
        return generateDeclaration;
    }

    /**
     * @param generateDeclaration
     *            the declaration to set
     */
    public void setGenerateDeclaration(boolean generateDeclaration) {
        this.generateDeclaration = generateDeclaration;
    }

    /**
     * @return the removeComments
     */
    public boolean isRemoveComments() {
        return removeComments;
    }

    /**
     * @param removeComments
     *            the removeComments to set
     */
    public void setRemoveComments(boolean removeComments) {
        this.removeComments = removeComments;
    }

    /**
     * @return the mapRoot
     */
    public String getMapRoot() {
        return mapRoot;
    }

    /**
     * @param mapRoot
     *            the mapRoot to set
     */
    public void setMapRoot(String mapRoot) {
        this.mapRoot = mapRoot;
    }

    /**
     * @return <code>true</code> if source point to single file
     */
    public boolean isSourceFile() {
		return !Strings.isNullOrEmpty(source) && project.findMember(source).exists()
				&& project.findMember(source).getType() == IResource.FILE;
    }

    /**
     * @return <code>true</code> if target point to single file
     */
    public boolean isTargetFile() {
        return !Strings.isNullOrEmpty(target) && target.trim().toLowerCase().endsWith(TS_EXT_DOT);
    }

    /**
     * Loads settings from preferences.
     * 
     * @param project
     *            project
     * @return settings
     */
    public static TypeScriptCompilerSettings load(IProject project) {
        ProjectScope scope = new ProjectScope(project);
        IEclipsePreferences pref = scope.getNode(Activator.PLUGIN_ID);
        TypeScriptCompilerSettings settings = new TypeScriptCompilerSettings(project, pref);

        settings.setSource(pref.get("source", ""));
        settings.setTarget(pref.get("target", ""));
        settings.setTargetRelativePathBasedOnSource(pref.getBoolean("targetRelativePathBasedOnSource",
                TypeScriptCompilerSettings.TARGET_RELATIVE_PATH_BASED_ON_SOURCE_DEFAULT));
        settings.setNoResolve(pref.getBoolean("noResolve", false));
        settings.setNoImplicitAny(pref.getBoolean("noImplicitAny", false));
        settings.setSourceMap(pref.getBoolean("sourceMap", true));
        settings.setModule(pref.get("module", "default"));
        settings.setTargetVersion(pref.get("targetVersion", "ES5"));
        settings.setGenerateDeclaration(pref.getBoolean("declaration", false));
        settings.setRemoveComments(pref.getBoolean("removeComments", false));
        settings.setMapRoot(pref.get("mapRoot", ""));

        return settings;
    }

    /**
     * Saves settings to project preferences store.
     */
    public void save() {
        try {
            pref.put("source", getSource());
            pref.put("target", getTarget());
            pref.putBoolean("targetRelativePathBasedOnSource", isTargetRelativePathBasedOnSource());
            pref.putBoolean("noResolve", isNoResolve());
            pref.putBoolean("noImplicitAny", isNoImplicitAny());
            pref.putBoolean("sourceMap", isSourceMap());
            pref.put("module", getModule());
            pref.put("targetVersion", getTargetVersion());
            pref.putBoolean("declaration", isGenerateDeclaration());
            pref.putBoolean("removeComments", isRemoveComments());
            pref.put("mapRoot", getMapRoot());
            pref.flush();
        } catch (BackingStoreException e) {
            Activator.error(e);
        }
    }
}

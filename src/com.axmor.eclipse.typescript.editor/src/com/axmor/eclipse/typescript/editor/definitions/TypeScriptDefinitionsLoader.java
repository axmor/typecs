/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.definitions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

import com.google.common.base.Throwables;

/**
 * Loads definitions from GitHub repositories.
 * 
 * @author Konstantin Zaitcev
 */
public enum TypeScriptDefinitionsLoader {
    /** Singleton instance. */
    INSTANCE;

    /** Rest handler. */
    private static final Resty R = new Resty();

    /** GitHub URL. */
    private static final String REPO_URL = "http://definitelytyped.github.io/tsd/data/repository.json";

    /** Url to download definition. */
    private static final String DEF_DOWNLOAD_URL = "https://github.com/borisyankov/DefinitelyTyped/raw/master/";
    
    /**
     * Loads modules.
     * 
     * @param progressMonitor
     *            progress monitor
     * @return modules
     */
    public static List<TypeScriptDefinition> load(IProgressMonitor progressMonitor) {
        try {
            progressMonitor.beginTask("Load list of modules", 1);
            JSONArray items = R.json(REPO_URL).object().getJSONArray("content");
            List<TypeScriptDefinition> modules = new ArrayList<TypeScriptDefinition>();
            for (int i = 0; i < items.length(); i++) {
                JSONObject obj = items.getJSONObject(i);
                JSONObject info = obj.getJSONObject("info");
                JSONArray authors = info.getJSONArray("authors");

                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < authors.length(); j++) {
                    JSONObject author = authors.getJSONObject(j);
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(author.getString("name"));
                } 
                
                TypeScriptDefinition module = new TypeScriptDefinition();
                module.setId(obj.getString("path"));
                module.setProject(obj.getString("project"));
                module.setName(obj.getString("name"));
                module.setAuthor(sb.toString());
                module.setVersion(info.getString("version"));
                module.setProjectUrl(info.getString("projectUrl"));
                module.setDescription(info.getString("name"));
                module.setUrl(DEF_DOWNLOAD_URL + obj.getString("path"));
                modules.add(module);
            }
            progressMonitor.done();
            return modules;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}

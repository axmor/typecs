/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.rename;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.TypeScriptAPI;
import com.axmor.eclipse.typescript.core.TypeScriptAPIFactory;
import com.axmor.eclipse.typescript.core.TypeScriptUtils;
import com.axmor.eclipse.typescript.editor.Activator;

/**
 * @author Konstantin Zaitcev
 */
public class RenameProcessor extends RefactoringProcessor {

    /** Rename info bean. */
    private final RenameInfo info;

    /**
     * Constructor.
     * 
     * @param info
     *            rename info
     */
    public RenameProcessor(RenameInfo info) {
        this.info = info;
    }

    @Override
    public Object[] getElements() {
        return new Object[] { info.getNewName() };
    }

    @Override
    public String getIdentifier() {
        return getClass().getName();
    }

    @Override
    public String getProcessorName() {
        return "Rename references";
    }

    @Override
    public boolean isApplicable() throws CoreException {
        return true;
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
        return null;
    }

    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws CoreException {
        return null;
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException {
    	boolean isOldTsVersion = TypeScriptUtils.getTypeScriptVersion().equals("1.0");
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(info.getPath()));
        TypeScriptAPI api = TypeScriptAPIFactory.getTypeScriptAPI(file.getProject());
        JSONArray json = api.getReferencesAtPosition(file, info.getPosition());
        CompositeChange changes = new CompositeChange("Rename");
        Map<IFile, TextEdit> mapEdits = new HashMap<>();
        try {
            for (int i = 0; i < json.length(); i++) {
                JSONObject obj = json.getJSONObject(i);
                IFile ifile = file.getProject().getFile(obj.getString("fileName"));
                if (!mapEdits.containsKey(ifile)) {
                    TextFileChange change = new TextFileChange(ifile.getName(), ifile);
                    change.setTextType("ts");
                    change.setEdit(new MultiTextEdit());
                    changes.add(change);
                    mapEdits.put(ifile, change.getEdit());
                }
                mapEdits.get(ifile).addChild(
                		isOldTsVersion ?
                		new ReplaceEdit(obj.getInt("minChar"), info.getOldName().length(), info.getNewName()) :
                		new ReplaceEdit(obj.getJSONObject("textSpan").getInt("start"), info.getOldName().length(), info.getNewName()));
            }
        } catch (JSONException e) {
            throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
        }
        return changes;
    }

    @Override
    public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants)
            throws CoreException {
        return new RefactoringParticipant[0];
    }
}
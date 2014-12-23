/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor.hierarchy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.graphics.Image;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.Activator;
import com.axmor.eclipse.typescript.core.TypeScriptAPI;
import com.axmor.eclipse.typescript.core.TypeScriptUtils;
import com.axmor.eclipse.typescript.editor.TypeScriptEditorUtils;
import com.axmor.eclipse.typescript.editor.TypeScriptUIImages;
import com.google.common.base.Throwables;

/**
 * @author kudrin
 *
 */
public class TreeRoot {
    
    private TreeRoot[] childItems = null;
    private String name = null;    
    private Image image;
    private int offset;
    private TypeScriptAPI api;
    private IFile file;
    private int callOffset;
    private int callLength;
    
    public TreeRoot(TypeScriptAPI api, JSONObject obj, int callOffset, int callLength, IFile currentFile) {
        this.name = getText(obj);
        this.image = createImage(obj);
        this.file = currentFile;
        this.api = api;
        this.callOffset = callOffset;
        this.callLength = callLength;
        try {
            Position position = TypeScriptEditorUtils.getPosition(obj);
            this.offset = position.offset;
            this.childItems = createChildren();
        } catch (JSONException e) {
            Activator.error(e);
        }
    }
    
    public TreeRoot[] getChildren() {
        return childItems;
    }
    
    public String getName() {
        return name;
    }
    
    public Image getImage() {        
        return image;
    }
    
    public int getOffset() {
        return this.offset;
    }
    
    public int getCallOffset() {
        return this.callOffset;
    }
    
    public int getCallLength() {
        return this.callLength;
    }
    
    public IFile getFile() {
        return this.file;
    }
    
    public boolean hasChildren() {
        return childItems != null && childItems.length > 0;
    }
    
    private String getText(JSONObject obj) {
        try {
            if (TypeScriptUtils.isTypeScriptLegacyVersion()) {
                return obj.getString("name");
            } else {
                return obj.getString("text");
            }
        } catch (JSONException e) {
            Activator.error(e);
            return "";
        }        
    }
    
    private TreeRoot[] createChildren() {
        JSONArray references = api.getReferencesAtPosition(file, offset);
        IFile currentFile = null;
        List<TreeRoot> roots = new ArrayList<TreeRoot>();
        for (int i = 0; i < references.length(); i++) {
            try {
                if (references.get(i) instanceof JSONObject) {
                    JSONObject obj = (JSONObject) references.get(i);
                    String fileName = obj.getString("fileName");
                    currentFile = file.getProject().getFile(fileName);
                    Position position = TypeScriptEditorUtils.getPosition(obj);
                    JSONArray model = api.getScriptModel(currentFile);                    
                    for (int j = 0; j < model.length(); j++) {
                        if (model.get(j) instanceof JSONObject) {
                            JSONObject item = (JSONObject) model.get(j);
                            roots.addAll(fetchChildren(item.getJSONArray("childItems"), position.offset, position.length, currentFile));                         
                        }
                    }
                }
            } catch (JSONException e) {                
                throw Throwables.propagate(e);
            }
        }         
        return roots.toArray(new TreeRoot[roots.size()]);
    }
    
    private List<TreeRoot> fetchChildren(JSONArray childs, int offset, int length, IFile currentFile) {
        List<TreeRoot> newRoots = new ArrayList<TreeRoot>();
        for (int i = 0; i < childs.length(); i++) {
            try {
                JSONObject obj = (JSONObject) childs.get(i);
                Position itemPosition = TypeScriptEditorUtils.getPosition(obj);
                if (offset > itemPosition.offset && offset < itemPosition.offset + itemPosition.length ) {
                    if (obj.getJSONArray("childItems").length() > 0) {
                        fetchChildren(obj.getJSONArray("childItems"), offset, length, currentFile);
                    }
                    else {
                        newRoots.add(new TreeRoot(api, obj, offset, length, currentFile));
                    }                    
                }
            } catch (JSONException e) {
                Activator.error(e);
            }
        }
        return newRoots;
    }
    
    private Image createImage(JSONObject element) {
        JSONObject obj = (JSONObject) element;
        TypeScriptUIImages imagesFactory = new TypeScriptUIImages();
        return imagesFactory.getImageForModelObject(obj);
    }     

}

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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.part.FileEditorInput;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.Activator;
import com.axmor.eclipse.typescript.core.TypeScriptAPI;
import com.axmor.eclipse.typescript.core.TypeScriptUtils;
import com.axmor.eclipse.typescript.editor.TypeScriptDocumentProvider;
import com.axmor.eclipse.typescript.editor.TypeScriptEditor;
import com.axmor.eclipse.typescript.editor.TypeScriptEditorUtils;
import com.axmor.eclipse.typescript.editor.TypeScriptUIImages;

/**
 * @author kudrin
 *
 */
public class TreeRoot {
    
    private TreeRoot[] childItems = null;
    private String name = null;
    private String kind = null;
    private Image image;
    private int offset;
    private int callLine;
    private TypeScriptAPI api;
    private TypeScriptEditor editor;
    private IFile file;
    private int callOffset;
    private int callLength;
    private String identifier;
    private TreeRoot parent;
    private boolean isRecursive;
    
    public TreeRoot(TypeScriptEditor editor, TypeScriptAPI api, JSONObject obj, int offset, int callLine, int callOffset, 
            int callLength, IFile currentFile, TreeRoot parent) {
        this.name = getText(obj);
        this.kind = getKind(obj);        
        this.file = currentFile;
        this.api = api;
        this.editor = editor;
        this.offset = offset;
        this.callLine  = callLine;
        this.callOffset = callOffset;
        this.callLength = callLength;
        this.identifier = this.file.getName() + "_" + this.kind + "_" + this.name + "_" + this.offset;
        this.parent = parent;
        this.isRecursive = isRecursive();
        this.image = createImage(obj, this.isRecursive);
        this.childItems = this.isRecursive ? new TreeRoot[0] : createChildren();        
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
    
    public int getLine() {
        return this.callLine;
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
    
    public String getIdentifier() {
        return this.identifier;
    }
    
    public TreeRoot getParent() {
        return this.parent;
    }
    
    public boolean getRecursive() {
        return this.isRecursive;
    }
    
    public boolean hasChildren() {
        return childItems != null && childItems.length > 0;
    }
    
    private boolean isRecursive() {
        TreeRoot current = getParent();
        while (current != null) {
            if (current.getIdentifier().equals(identifier)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
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
    
    private String getKind(JSONObject obj) {
        try {
            return obj.getString("kind");
        } catch (JSONException e) {
            Activator.error(e);
            return "";
        }        
    }
    
    private Image createImage(JSONObject element, boolean isRecursive) {
        JSONObject obj = (JSONObject) element;
        TypeScriptUIImages imagesFactory = new TypeScriptUIImages();
        return imagesFactory.getImageForModelObject(obj, isRecursive);
    }
    
    private TreeRoot[] createChildren() {
        JSONArray references = api.getReferencesAtPosition(file, offset);
        IFile currentFile = null;
        List<TreeRoot> roots = new ArrayList<TreeRoot>();                
        try {            
            for (int i = 0; i < references.length(); i++) {
                if (references.get(i) instanceof JSONObject) {
                    JSONObject obj = (JSONObject) references.get(i);
                    String fileName = obj.getString("fileName");
                    currentFile = file.getProject().getFile(fileName);
                    Position position = TypeScriptEditorUtils.getPosition(obj);
                    JSONArray model = api.getScriptModel(currentFile);
                    for (int j = 0; j < model.length(); j++) {
                        if (model.get(j) instanceof JSONObject) {
                            JSONObject item = (JSONObject) model.get(j);
                            roots.addAll(fetchChildren(item.getJSONArray("childItems"), position.offset,
                                    position.length, currentFile));                         
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Activator.error(e);
        }
                 
        return roots.toArray(new TreeRoot[roots.size()]);
    }
    
    private List<TreeRoot> fetchChildren(JSONArray childs, int offset, int length, IFile currentFile) {
        List<TreeRoot> newRoots = new ArrayList<TreeRoot>();
        for (int i = 0; i < childs.length(); i++) {
            try {
                JSONObject obj = (JSONObject) childs.get(i);
                Position itemPosition = TypeScriptEditorUtils.getPosition(obj);
                if (offset > itemPosition.offset && offset < itemPosition.offset + itemPosition.length && 
                        !(obj.getString("text").equals(name) && obj.getString("kind").equals(kind))) {
                    if (obj.getJSONArray("childItems").length() > 0) {
                        fetchChildren(obj.getJSONArray("childItems"), offset, length, currentFile);
                    }
                    else {
                        TypeScriptDocumentProvider dp = (TypeScriptDocumentProvider) editor.getDocumentProvider();
                        if (dp == null) {
                            return newRoots;
                        }                        
                        IDocument document = dp.addDocument(new FileEditorInput(currentFile));                        
                        FindReplaceDocumentAdapter findReplaceDocumentAdapter = 
                                new FindReplaceDocumentAdapter(document);
                        IRegion region;
                        try {
                            int line = document.getLineOfOffset(offset) + 1;
                            region = findReplaceDocumentAdapter.find(itemPosition.offset, obj.getString("text"), true, 
                                    true, true, false);                            
                            newRoots.add(new TreeRoot(editor, api, obj, region.getOffset(), line, offset, length, 
                                    currentFile, this));                            
                        } catch (BadLocationException e) {
                            Activator.error(e);
                        }                        
                    }                    
                }
            } catch (JSONException | CoreException e) {
                Activator.error(e);
            }
        }
        return newRoots;
    }  
}

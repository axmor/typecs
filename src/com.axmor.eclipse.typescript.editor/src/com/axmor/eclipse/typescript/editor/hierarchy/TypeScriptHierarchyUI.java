/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor.hierarchy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.Activator;
import com.axmor.eclipse.typescript.core.TypeScriptAPI;
import com.axmor.eclipse.typescript.editor.TypeScriptDocumentProvider;
import com.axmor.eclipse.typescript.editor.TypeScriptEditor;
import com.axmor.eclipse.typescript.editor.TypeScriptEditorUtils;

/**
 * @author kudrin
 *
 */
public class TypeScriptHierarchyUI {
    
    private static TypeScriptHierarchyUI fgInstance;

    private int fViewCount = 0;    
    
    private List<CallHierarchyViewPart> fCallHierarchyViews = new ArrayList<CallHierarchyViewPart>();
    
    private TypeScriptHierarchyUI() {
        // Do nothing
    }

    public static TypeScriptHierarchyUI getDefault() {
        if (fgInstance == null) {
            fgInstance = new TypeScriptHierarchyUI();
        }

        return fgInstance;
    }
    
    public static CallHierarchyViewPart openView(TypeScriptEditor editor, IFile file, 
            JSONArray references) {
        IWorkbenchWindow window = editor.getSite().getWorkbenchWindow();
        TypeScriptAPI api = editor.getApi();
        IWorkbenchPage page= window.getActivePage();
        try {
            CallHierarchyViewPart viewPart= getDefault().findCallHierarchyViewPart(page);
            String secondaryId= null;
            if (viewPart == null) {
                if (page.findViewReference(CallHierarchyViewPart.ID_CALL_HIERARCHY) != null) {
                    secondaryId = String.valueOf(++getDefault().fViewCount);
                }                
            } else {
                secondaryId = viewPart.getViewSite().getSecondaryId();
            }                
            viewPart = (CallHierarchyViewPart)page.showView(CallHierarchyViewPart.ID_CALL_HIERARCHY, secondaryId, 
                    IWorkbenchPage.VIEW_ACTIVATE);
            viewPart.setInputElements(createRoot(editor, api, file, references));
            return viewPart;
        } catch (CoreException e) {
            Activator.error(e);
        }        
        return null;
    }
    
    private CallHierarchyViewPart findCallHierarchyViewPart(IWorkbenchPage page) {
        for (Iterator<CallHierarchyViewPart> iter= fCallHierarchyViews.iterator(); iter.hasNext();) {
            CallHierarchyViewPart view= iter.next();
            if (page.equals(view.getSite().getPage())) {
                return view;
            }
        }
        IViewReference[] viewReferences= page.getViewReferences();
        for (int i= 0; i < viewReferences.length; i++) {
            IViewReference curr= viewReferences[i];
            if (CallHierarchyViewPart.ID_CALL_HIERARCHY.equals(curr.getId()) && page.equals(curr.getPage())) {
                CallHierarchyViewPart view= (CallHierarchyViewPart)curr.getView(true);
                if (view != null) {
                    return view;
                }
            }
        }
        return null;
    }
    
    private static TreeRoot[] createRoot(TypeScriptEditor editor, TypeScriptAPI api, IFile file, 
            JSONArray references) {
        IFile currentFile = null;
        for (int i = 0; i < references.length(); i++) {
            try {
                if (references.get(i) instanceof JSONObject) {
                    JSONObject obj = (JSONObject) references.get(i);
                    if (!obj.getBoolean("isWriteAccess")) {
                        continue;
                    }
                    String fileName = obj.getString("fileName");
                    currentFile = file.getProject().getFile(fileName);                   
                    Position position = TypeScriptEditorUtils.getPosition(obj);
                    JSONArray def = api.getTypeDefinition(currentFile, position.offset);                    
                    if (def.length() > 0) {
                        JSONObject defObj = (JSONObject) def.get(0);
                        Position defPosition = TypeScriptEditorUtils.getPosition(defObj);
                        JSONArray model = api.getScriptModel(currentFile);
                        for (int j = 0; j < model.length(); j++) {
                            if (model.get(j) instanceof JSONObject) {
                                JSONObject item = (JSONObject) model.get(j);
                                JSONObject root = findInitCall(item, defPosition.offset);
                                IDocumentProvider dp = editor.getDocumentProvider();
                                if (root != null && dp instanceof TypeScriptDocumentProvider) {
                                    TypeScriptDocumentProvider provider = (TypeScriptDocumentProvider) dp;                                    
                                    IDocument document = provider.addDocument(new FileEditorInput(currentFile));
                                    int line = document.getLineOfOffset(position.offset) + 1;
                                    return new TreeRoot[] {new TreeRoot(editor, api, root, position.offset, line, 
                                            position.offset, position.length, currentFile, null)};
                                }
                            }
                        }                        
                    }
                    else {
                        break;
                    }                    
                }
            } catch (JSONException | BadLocationException | CoreException e) {                
                Activator.error(e);
            }
        }
        return new TreeRoot[0];
    }
    
    private static JSONObject findInitCall(JSONObject item, int offset) {
        Position itemPosition;
        JSONObject result = null;
        try {
            itemPosition = TypeScriptEditorUtils.getPosition(item);
            if (itemPosition.offset == offset) {
                result = item;                
                return result;
            }
            else {
                JSONArray children = item.getJSONArray("childItems");
                for (int i = 0; i < children.length(); i++) {
                    JSONObject child = (JSONObject) children.get(i);
                    result = findInitCall(child, offset);
                    if (result != null) {
                        break;
                    }
                }
            }            
        } catch (JSONException e) {
            Activator.error(e);
        }
        return result;
    }

    /**
     * @param callHierarchyViewPart
     */
    public void callHierarchyViewActivated(CallHierarchyViewPart view) {
        fCallHierarchyViews.remove(view);
        fCallHierarchyViews.add(0, view);        
    }

    /**
     * @param callHierarchyViewPart
     */
    public void callHierarchyViewClosed(CallHierarchyViewPart view) {
        fCallHierarchyViews.remove(view);        
    }

}

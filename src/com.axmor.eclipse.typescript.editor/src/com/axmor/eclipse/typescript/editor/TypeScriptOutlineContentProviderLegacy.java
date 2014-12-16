package com.axmor.eclipse.typescript.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.editor.parser.TypeScriptModelKinds;
import com.google.common.base.Throwables;

/**
 * For TypeScript Compiler v0.9 and v1.0
 * A content provider mediates between the viewer's model and the viewer itself.
 * 
 * @author Asya Vorobyova
 *
 */
public class TypeScriptOutlineContentProviderLegacy implements ITreeContentProvider {
    
    /**
     * ts file model
     */
    private JSONArray model;
    
    /**
     * Constructor
     * 
     * @param model file model
     */
    public TypeScriptOutlineContentProviderLegacy(JSONArray model) {
        super();
        this.model = model;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    	if (newInput instanceof JSONArray) {
    		this.model = (JSONArray) newInput;
    	}
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof JSONObject) {
            JSONObject obj = (JSONObject) parentElement;
            try {
                String kind = obj.getString("kind");
                //we generate children only for interfaces, classes and methods
                if (kind.equals(TypeScriptModelKinds.Kinds.CONSTRUCTOR.toString())
                        || kind.equals(TypeScriptModelKinds.Kinds.FUNCTION.toString())
                        || kind.equals(TypeScriptModelKinds.Kinds.METHOD.toString())
                        || kind.equals(TypeScriptModelKinds.Kinds.PROPERTY.toString())
                        || kind.equals(TypeScriptModelKinds.Kinds.VAR.toString())) {
                    return TypeScriptContentOutlinePage.NO_CHILDREN;
                }
                String name = obj.getString("name");
                if (obj.getString("containerKind").equals(TypeScriptModelKinds.Kinds.MODULE.toString())) {
                    name = obj.getString("containerName") + "." + name;
                }
                return getChildren(kind, name);
            } catch (JSONException e) {
                Activator.error(e);
            }
        } else if (parentElement instanceof JSONArray) {
            return getChildren("", "");
        }
        return TypeScriptContentOutlinePage.NO_CHILDREN;
    }

    /**
     * Looks for children in document model for a given model object
     * 
     * @param kind a kind of the object
     * @param name a name of the object
     * @return an array of children
     */
    private Object[] getChildren(String kind, String name) {
        List<Object> children = new ArrayList<Object>();
        for (int i = 0; i < model.length(); i++) {
            if (!model.isNull(i)) {
                try {
                    if (model.get(i) instanceof JSONObject) {
                        JSONObject obj = (JSONObject) model.get(i);
                        String parentKind = obj.getString("containerKind");
                        String parentName = obj.getString("containerName");
                        if (parentKind.equals(kind) && parentName.equals(name)) {
                            children.add(obj);
                        }
                    }
                } catch (JSONException e) {
                    throw Throwables.propagate(e);
                }
            }
        }
        return children.toArray();
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof JSONObject) {
            JSONObject obj = (JSONObject) element;
            try {
                String parentKind = obj.getString("containerKind");
                String parentName = obj.getString("containerName");
                return getElement(parentKind, parentName);
            } catch (JSONException e) {
                throw Throwables.propagate(e);
            }
        }
        return null;
    }

    /**
     * Looks in model for a given element
     * 
     * @param elKind a kind of the element
     * @param elName a name of the element
     * @return desired element
     */
    private Object getElement(String elKind, String elName) {
        for (int i = 0; i < model.length(); i++) {
            if (!model.isNull(i)) {
                try {
                    if (model.get(i) instanceof JSONObject) {
                        JSONObject obj = (JSONObject) model.get(i);
                        String kind = obj.getString("kind");
                        String name = obj.getString("name");
                        if (elKind.equals(kind) && elName.equals(name)) {
                            return obj;
                        }
                    }
                } catch (JSONException e) {
                    throw Throwables.propagate(e);
                }
            }
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

}
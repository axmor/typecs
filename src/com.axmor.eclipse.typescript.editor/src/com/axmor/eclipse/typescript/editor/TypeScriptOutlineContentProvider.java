package com.axmor.eclipse.typescript.editor;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.editor.parser.TypeScriptModelKinds;

/**
 * For TypeScript compiler v1.1 and above
 * A content provider mediates between the viewer's model and the viewer itself.
 * 
 * @author Asya Vorobyova
 */
public class TypeScriptOutlineContentProvider implements ITreeContentProvider {
    
    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public Object[] getElements(Object inputElement) {
    	if (inputElement instanceof JSONArray) {
    		JSONArray jsonArray = (JSONArray) inputElement;
    		Object[] result = new Object[jsonArray.length()];
    		for (int i = 0; i < jsonArray.length(); i++) {
				try {
					result[i] = jsonArray.get(i);
				} catch (JSONException e) {
					Activator.error(e);
				}
			}
    		return result;
    	}
    	return TypeScriptContentOutlinePage.NO_CHILDREN;
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
                return getElements(obj.get("childItems"));
            } catch (JSONException e) {
                Activator.error(e);
            }
        }
        return TypeScriptContentOutlinePage.NO_CHILDREN;
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        try {
			return element instanceof JSONObject && ((JSONObject) element).has("childItems") && ((JSONObject) element).getJSONArray("childItems").length() > 0;
		} catch (JSONException e) {
			return false;
		}
    }

}
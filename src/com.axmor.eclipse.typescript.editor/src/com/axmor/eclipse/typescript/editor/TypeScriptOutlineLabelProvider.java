package com.axmor.eclipse.typescript.editor;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.google.common.base.Throwables;

/**
 * A label provider to get images and texts for elements viewing
 * 
 * @author Asya Vorobyova
 *
 */
class TypeScriptOutlineLabelProvider extends LabelProvider {

    @Override
    public Image getImage(Object element) {
        JSONObject obj = (JSONObject) element;
        TypeScriptUIImages imagesFactory = new TypeScriptUIImages();
        return imagesFactory.getImageForModelObject(obj);
    }

    @Override
    public String getText(Object element) {
        try {
            JSONObject obj = (JSONObject) element;
            return obj.getString("name");
        } catch (JSONException e) {
            throw Throwables.propagate(e);
        }
    }
}
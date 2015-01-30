/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.parser;

import com.axmor.eclipse.typescript.editor.Activator;

/**
 * Class storing the image keys
 * 
 * @author Asya Vorobyova
 */
public final class TypeScriptImageKeys {
    
    /** Plugin id. */
    private static final String PLUGIN_ID = Activator.PLUGIN_ID;

    // Keys for images
    /** Key for image. */
    public static final String IMG_PACKAGE = PLUGIN_ID + "IMG_PACKAGE";
    /** Key for image. */
    public static final String IMG_CLASS = PLUGIN_ID + "IMG_CLASS";
    /** Key for image. */
    public static final String IMG_INTERFACE = PLUGIN_ID + "IMG_INTERFACE";
    /** Key for image. */
    public static final String IMG_ENUM_DEFAULT = PLUGIN_ID + "IMG_ENUM_DEFAULT";
    /** Key for image. */
    public static final String IMG_INNER_CLASS_DEFAULT = PLUGIN_ID + "IMG_INNER_CLASS_DEFAULT";
    /** Key for image. */
    public static final String IMG_INNER_CLASS_PRIVATE = PLUGIN_ID + "IMG_INNER_CLASS_PRIVATE";
    /** Key for image. */
    public static final String IMG_INNER_CLASS_PUBLIC = PLUGIN_ID + "IMG_INNER_CLASS_PUBLIC";
    /** Key for image. */
    public static final String IMG_INTERFACE_DEFAULT = PLUGIN_ID + "IMG_INTERFACE_DEFAULT";
    /** Key for image. */
    public static final String IMG_INNER_INTERFACE_DEFAULT = PLUGIN_ID + "IMG_INNER_INTERFACE_DEFAULT";
    /** Key for image. */
    public static final String IMG_INNER_INTERFACE_PRIVATE = PLUGIN_ID + "IMG_INNER_INTERFACE_PRIVATE";
    /** Key for image. */
    public static final String IMG_INNER_INTERFACE_PUBLIC = PLUGIN_ID + "IMG_INNER_INTERFACE_PUBLIC";
    /** Key for image. */
    public static final String IMG_ENUM_PRIVATE = PLUGIN_ID + "IMG_ENUM_PRIVATE";
    /** Key for image. */
    public static final String IMG_FIELD_DEFAULT = PLUGIN_ID + "IMG_FIELD_DEFAULT";
    /** Key for image. */
    public static final String IMG_FIELD_PRIVATE = PLUGIN_ID + "IMG_FIELD_PRIVATE";
    /** Key for image. */
    public static final String IMG_FIELD_PUBLIC = PLUGIN_ID + "IMG_FIELD_PUBLIC";
    /** Key for image. */
    public static final String IMG_METHOD_DEFAULT = PLUGIN_ID + "IMG_METHOD_DEFAULT";
    /** Key for image. */
    public static final String IMG_METHOD_PRIVATE = PLUGIN_ID + "IMG_METHOD_PRIVATE";
    /** Key for image. */
    public static final String IMG_METHOD_PUBLIC = PLUGIN_ID + "IMG_METHOD_PUBLIC";
    /** Key for image. */
    public static final String IMG_CONSTRUCTOR = PLUGIN_ID + "IMG_CONSTRUCTOR";
    /** Key for image. */
    public static final String IMG_STATIC = PLUGIN_ID + "IMG_STATIC";
    /** Key for image. */
    public static final String IMG_RECURSIVE = PLUGIN_ID + "IMG_RECURSIVE";

    /** "Search reference on object" image **/
    public static final String IMG_SEARCH_REF_OBJ = PLUGIN_ID + "IMG_SEARCH_REF_OBJ";

    /** "Type Definition" file image **/
    public static final String IMG_DTS_FILE = PLUGIN_ID + "IMG_DTS_FILE";

    /** Key for image. */
    public static final String IMG_TOGGLE_OCCURRENCE = PLUGIN_ID + "IMG_TOGGLE_OCCURRENCE";

	/** Key for template proposal image. */
	public static final String IMG_TEMPLATE_PROPOSAL = PLUGIN_ID + "IMG_TEMPLATE_PROPOSAL";

    /**
     * Protect from initialization.
     */
    private TypeScriptImageKeys() {
    }
}

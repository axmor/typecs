/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.editor.parser.TypeScriptImageKeys;
import com.axmor.eclipse.typescript.editor.parser.TypeScriptModelKinds;
import com.google.common.base.Throwables;

/**
 * Internal images for ui
 * 
 * @author Asya Vorobyova
 * 
 */
public class TypeScriptUIImages {

    /**
     * The image registry containing <code>Image</code>s.
     */
    private static ImageRegistry imageRegistry;

    /**
     * The registry for composite images
     */
    private static ImageDescriptorRegistry imageDescriptorRegistry;

    /**
     * A path to the icons location
     */
    private static final String ICONS_PATH = "$nl$/icons/full/"; //$NON-NLS-1$

    /**
     * Declare all images
     */
    private static void declareImages() {
        declareRegistryImage(TypeScriptImageKeys.IMG_CLASS, ICONS_PATH + "class_obj.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_CONSTRUCTOR, ICONS_PATH + "constr_ovr.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_ENUM_DEFAULT, ICONS_PATH + "enum_obj.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_ENUM_PRIVATE, ICONS_PATH + "enum_private_obj.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_FIELD_DEFAULT, ICONS_PATH + "field_default_obj.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_FIELD_PRIVATE, ICONS_PATH + "field_private_obj.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_FIELD_PUBLIC, ICONS_PATH + "field_public_obj.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_INNER_CLASS_DEFAULT, ICONS_PATH + "innerclass_default_obj.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_INNER_CLASS_PRIVATE, ICONS_PATH + "innerclass_private_obj.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_INNER_CLASS_PUBLIC, ICONS_PATH + "innerclass_public_obj.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_INNER_INTERFACE_DEFAULT, ICONS_PATH
                + "innerinterface_default_obj.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_INNER_INTERFACE_PRIVATE, ICONS_PATH
                + "innerinterface_private_obj.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_INNER_INTERFACE_PUBLIC, ICONS_PATH
                + "innerinterface_public_obj.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_INTERFACE, ICONS_PATH + "int_obj.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_INTERFACE_DEFAULT, ICONS_PATH + "int_default_obj.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_METHOD_DEFAULT, ICONS_PATH + "methdef_obj.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_METHOD_PRIVATE, ICONS_PATH + "methpri_obj.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_METHOD_PUBLIC, ICONS_PATH + "methpub_obj.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_PACKAGE, ICONS_PATH + "package_obj.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_STATIC, ICONS_PATH + "static_co.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_RECURSIVE, ICONS_PATH + "recursive_co.gif");
        declareRegistryImage(TypeScriptImageKeys.IMG_SEARCH_REF_OBJ, ICONS_PATH + "search_ref_obj.gif");

        declareRegistryImage(TypeScriptImageKeys.IMG_DTS_FILE, "$nl$/icons/typescript_def_file.png");

        declareRegistryImage(TypeScriptImageKeys.IMG_TOGGLE_OCCURRENCE, "$nl$/icons/full/etool16/mark_occurrences.png");
		declareRegistryImage(TypeScriptImageKeys.IMG_TEMPLATE_PROPOSAL, ICONS_PATH + "template_obj.png");
	}

    /**
     * Declare an Image in the registry table.
     * 
     * @param key
     *            The key to use when registering the image
     * @param path
     *            The path where the image can be found. This path is relative to where this plugin
     *            class is found (i.e. typically the packages directory)
     */
    private static void declareRegistryImage(String key, String path) {
        ImageDescriptor desc = ImageDescriptor.getMissingImageDescriptor();
        Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
        URL url = null;
        if (bundle != null) {
            url = FileLocator.find(bundle, new Path(path), null);
            desc = ImageDescriptor.createFromURL(url);
        }
        imageRegistry.put(key, desc);
    }

    /**
     * Returns the ImageRegistry.
     * 
     * @return the image registry
     */
    public static ImageRegistry getImageRegistry() {
        if (imageRegistry == null) {
            initializeImageRegistry();
        }
        return imageRegistry;
    }

    /**
     * Initialize the image registry by declaring all of the required graphics. This involves
     * creating JFace image descriptors describing how to create/find the image should it be needed.
     * The image is not actually allocated until requested.
     * 
     * Where are the images? The images (typically gifs) are found in the same location as this
     * plugin class. This may mean the same package directory as the package holding this class. The
     * images are declared using this.getClass() to ensure they are looked up via this plugin class.
     * 
     * @see org.eclipse.jface.resource.ImageRegistry
     * @return the image registry
     */
    public static ImageRegistry initializeImageRegistry() {
        imageRegistry = new ImageRegistry(Activator.getStandardDisplay());
        declareImages();
        return imageRegistry;
    }

    /**
     * Returns the Image identified by the given key,
     * or <code>null</code> if it does not exist.
     * 
     * @param key the image id
     * @return the image
     */
    public static Image getImage(String key) {
        return getImageRegistry().get(key);
    }

    /**
     * Returns the ImageDescriptor identified by the given key,
     * or <code>null</code> if it does not exist.
     * 
     * @param key the descriptor id
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String key) {
        return getImageRegistry().getDescriptor(key);
    }    
    
    public Image getImageForModelObject(JSONObject obj) {
        return getImageForModelObject(obj, false);
    }

    /**
     * Gets image for an object from script model
     * 
     * @param obj the model object
     * @return the corresponding image
     */
    public Image getImageForModelObject(JSONObject obj, boolean isRecursive) {
        try {
            int flags = 0;
            Entity entity = new Entity("", 0);

            String kind = obj.getString("kind");
            String kindModifiers = obj.getString("kindModifiers");
            String[] parts = kindModifiers.split(",");
            boolean isInner = false;
            if (obj.has("containerKind")) {
                isInner = !obj.getString("containerKind").isEmpty();
            }

            if (kind.equals(TypeScriptModelKinds.Kinds.KEYWORD.toString())) {
                return null;
            } else if (kind.equals(TypeScriptModelKinds.Kinds.MODULE.toString())) {
                entity.key = TypeScriptImageKeys.IMG_PACKAGE;
            } else if (kind.equals(TypeScriptModelKinds.Kinds.CLASS.toString())) {
                if (!isInner) {
                    entity.key = TypeScriptImageKeys.IMG_CLASS;
                } else {
                    entity.useKindModifiers(parts, flags, TypeScriptImageKeys.IMG_INNER_CLASS_DEFAULT,
                            TypeScriptImageKeys.IMG_INNER_CLASS_PRIVATE, TypeScriptImageKeys.IMG_INNER_CLASS_PUBLIC);
                }
            } else if (kind.equals(TypeScriptModelKinds.Kinds.INTERFACE.toString())) {
                if (!isInner) {
                    entity.key = TypeScriptImageKeys.IMG_INTERFACE;
                } else {
                    entity.useKindModifiers(parts, flags, TypeScriptImageKeys.IMG_INNER_INTERFACE_DEFAULT,
                            TypeScriptImageKeys.IMG_INNER_INTERFACE_PRIVATE,
                            TypeScriptImageKeys.IMG_INNER_INTERFACE_PUBLIC);
                }
            } else if (kind.equals(TypeScriptModelKinds.Kinds.ENUM.toString())) {
                entity.key = TypeScriptImageKeys.IMG_ENUM_DEFAULT;
                if (isInner) {
                    for (int i = 0; i < parts.length; i++) {
                        if (parts[i].equals(TypeScriptModelKinds.Kinds.PRIVATE.toString())) {
                            entity.key = TypeScriptImageKeys.IMG_ENUM_PRIVATE;
                            break;
                        }
                    }
                }
            } else if (kind.equals(TypeScriptModelKinds.Kinds.CONSTRUCTOR.toString())) {
                entity.key = TypeScriptImageKeys.IMG_FIELD_DEFAULT;
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].equals(TypeScriptModelKinds.Kinds.PRIVATE.toString())) {
                        entity.key = TypeScriptImageKeys.IMG_FIELD_PRIVATE;
                    } else if (parts[i].equals(TypeScriptModelKinds.Kinds.PUBLIC.toString())) {
                        entity.key = TypeScriptImageKeys.IMG_FIELD_PUBLIC;
                    }
                }
                flags = flags | TypeScriptImageDescriptor.CONSTRUCTOR;
                entity.flags = flags;
            } else if (kind.equals(TypeScriptModelKinds.Kinds.METHOD.toString())) {
                entity.useKindModifiers(parts, flags, TypeScriptImageKeys.IMG_METHOD_DEFAULT,
                        TypeScriptImageKeys.IMG_METHOD_PRIVATE, TypeScriptImageKeys.IMG_METHOD_PUBLIC);
            } else if (kind.equals(TypeScriptModelKinds.Kinds.PROPERTY.toString())) {
                entity.useKindModifiers(parts, flags, TypeScriptImageKeys.IMG_FIELD_DEFAULT,
                        TypeScriptImageKeys.IMG_FIELD_PRIVATE, TypeScriptImageKeys.IMG_FIELD_PUBLIC);
            } else if (kind.equals(TypeScriptModelKinds.Kinds.VAR.toString())) {
                entity.useKindModifiers(parts, flags, TypeScriptImageKeys.IMG_FIELD_DEFAULT,
                        TypeScriptImageKeys.IMG_FIELD_PRIVATE, TypeScriptImageKeys.IMG_FIELD_PUBLIC);
            } else if (kind.equals(TypeScriptModelKinds.Kinds.FUNCTION.toString())) {
                entity.useKindModifiers(parts, flags, TypeScriptImageKeys.IMG_METHOD_DEFAULT,
                        TypeScriptImageKeys.IMG_METHOD_PRIVATE, TypeScriptImageKeys.IMG_METHOD_PUBLIC);
            }
            if (isRecursive) {
                flags = flags | TypeScriptImageDescriptor.RECURSIVE;
                entity.flags = flags;
            }
            if (entity.flags == 0) {
                return TypeScriptUIImages.getImage(entity.key);
            } else {
                return TypeScriptUIImages.getImage(new TypeScriptImageDescriptor(TypeScriptUIImages
                        .getImageDescriptor(entity.key), entity.flags));
            }
        } catch (JSONException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Supporting class for keeping image data
     *
     */
    private class Entity {
        /**
         * 
         */
        private String key;
        /**
         * 
         */
        private int flags;

        /**
         * @param key an image key
         * @param flags id to determine kind modifiers
         */
        public Entity(String key, int flags) {
            super();
            this.key = key;
            this.flags = flags;
        }

        /**
         * Method to change the instance properties
         * 
         * @param parts array of modifiers
         * @param flags current flags
         * @param defaultKey default key value
         * @param privateKey default private key value
         * @param publicKey default public key value
         */
        private void useKindModifiers(String[] parts, int flags, String defaultKey, String privateKey,
                String publicKey) {
            String newKey = defaultKey;
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals(TypeScriptModelKinds.Kinds.PRIVATE.toString())) {
                    newKey = privateKey;
                } else if (parts[i].equals(TypeScriptModelKinds.Kinds.PUBLIC.toString())) {
                    newKey = publicKey;
                } else if (parts[i].equals(TypeScriptModelKinds.Kinds.STATIC.toString())) {
                    flags = flags | TypeScriptImageDescriptor.STATIC;
                }
            }
            this.key = newKey;
            this.flags = flags;
        }

    }

    /**
     * Returns the image for the given composite descriptor.
     *
     * @param imageDescriptor image descriptor
     * @return the image
     */
    public static Image getImage(CompositeImageDescriptor imageDescriptor) {
        if (imageDescriptorRegistry == null) {
            imageDescriptorRegistry = new ImageDescriptorRegistry();
        }
        return imageDescriptorRegistry.get(imageDescriptor);
    }

    /**
     * Disposes the ImageDescriptorRegistry
     */
    public static void disposeImageDescriptorRegistry() {
        if (imageDescriptorRegistry != null) {
            imageDescriptorRegistry.dispose();
        }
    }

    /**
     * Returns whether the images have been initialized.
     * 
     * @return whether the images have been initialized
     */
    public static synchronized boolean isInitialized() {
        return imageDescriptorRegistry != null;
    }

}

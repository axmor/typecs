/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.axmor.eclipse.typescript.core;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.axmor.eclipse.typescript.core.index.IndexInfo;
import com.axmor.eclipse.typescript.core.index.TypeScriptIndexManager;
import com.axmor.eclipse.typescript.core.index.TypeScriptIndexer.DocumentKind;
import com.axmor.eclipse.typescript.core.index.TypeScriptIndexer.TypeVisibility;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends AbstractUIPlugin {

    /** The plug-in ID. */
    public static final String PLUGIN_ID = "com.axmor.eclipse.typescript.bridge"; //$NON-NLS-1$

    /** The shared instance. */
    private static Activator plugin;

    /** Index manager. */
    private TypeScriptIndexManager indexManager;

    /**
     * The constructor.
     */
    public Activator() {
    }

    @Override
    public final void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        indexManager = new TypeScriptIndexManager();
        indexManager.startIndex();
    }

    @Override
    public final void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
        indexManager.stopIndex();
        TypeScriptAPIFactory.stopTypeScriptAPIs();
    }

    /**
     * Returns the shared instance.
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative path.
     * 
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /**
     * Gets results for Open Type action
     * 
     * @param pattern
     *            text to search
     * @return results
     */
	public Iterable<TypeDocument> getSearchResults(String pattern) {
		if (pattern.endsWith("*")) {
			pattern = pattern.substring(0, pattern.length() - 1);
		}
		return Iterables.transform(indexManager.searchByName(pattern), new Function<IndexInfo, TypeDocument>() {
			@Override
			public TypeDocument apply(IndexInfo input) {
				String type = "";
                for (int k = 0; k < DocumentKind.values().length; k++) {
					if (input.getType() == DocumentKind.values()[k].getIntValue()) {
                        type = DocumentKind.values()[k].getStringValue();
                        break;
                    }
                }
                String visibility = "";
                for (int k = 0; k < TypeVisibility.values().length; k++) {
					if (input.getVisibility() == TypeVisibility.values()[k].getIntValue()) {
                        visibility = TypeVisibility.values()[k].getStringValue();
                        break;
                    }
                }
				return new TypeDocument(input.getName(), input.getFile(), input.getProject(), String.valueOf(input
						.getOffset()),
                        type, visibility);
			}
		});
    }

    /**
     * Class for storing document fields
     * 
     * @author Asya Vorobyova
     */
    public class TypeDocument {
        /** name of type */
        private String name;
        /** type file */
        private String file;
        /** type project */
        private String project;
        /** type offset in file */
        private String offset;
        /** document type */
        private String type;
        /** type visibility */
        private String visibility;

        /**
         * @param name name
         * @param file file
         * @param project project
         * @param offset offset
         * @param type type
         * @param visibility visibility 
         */
        public TypeDocument(String name, String file, String project, String offset, String type, String visibility) {
            super();
            this.name = name;
            this.file = file;
            this.project = project;
            this.offset = offset;
            this.type = type;
            this.visibility = visibility;
        }

        /**
         * Gets String value of key
         * 
         * @param key key
         * @return value
         */
        public String getString(String key) {
            if (key.equals("name")) {
                return name;
            }
            if (key.equals("file")) {
                return file;
            }
            if (key.equals("project")) {
                return project;
            }
            if (key.equals("offset")) {
                return offset;
            }
            if (key.equals("type")) {
                return type;
            }
            if (key.equals("visibility")) {
                return visibility;
            }
            return null;
        }
    }

    /**
     * Print error message to Error log.
     * 
     * @param e
     *            exception
     */
    public static void error(final Exception e) {
        plugin.getLog().log(new Status(Status.ERROR, PLUGIN_ID, e.getMessage(), e));
    }
}

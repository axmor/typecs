/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.axmor.eclipse.typescript.core;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.util.Version;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.axmor.eclipse.typescript.core.index.TypeScriptIndexManager;
import com.axmor.eclipse.typescript.core.index.TypeScriptIndexer.DocumentKind;
import com.axmor.eclipse.typescript.core.index.TypeScriptIndexer.TypeVisibility;
import com.google.common.base.Throwables;

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
    public TypeDocument[] getSearchResults(String pattern) {
        IndexReader ireader;
        ScoreDoc[] hits = null;
        TypeDocument[] docs = null;
        try {
            ireader = IndexReader.open(indexManager.getIdxDir());
            IndexSearcher isearcher = new IndexSearcher(ireader);
            // Parse a simple query that searches for "text":

            Sort sort = new Sort(new SortField[] { SortField.FIELD_SCORE, new SortField("score", SortField.INT) });
            TopFieldCollector topField = TopFieldCollector.create(sort, 100, true, true, true, false);

            QueryParser parser = new QueryParser(Version.LUCENE_35, "name", new StandardAnalyzer(Version.LUCENE_35));
            parser.setAllowLeadingWildcard(true);
            parser.setAutoGeneratePhraseQueries(false);
            Query query = parser.parse("name:(*" + pattern.toLowerCase() + "*) or terms:(*" + pattern.toLowerCase()
                    + "*)");
            isearcher.search(query, topField);
            hits = topField.topDocs().scoreDocs;
            // Iterate through the results:
            docs = new TypeDocument[hits.length];
            String innerName;
            for (int i = 0; i < hits.length; i++) {
                Document hitDoc = isearcher.doc(hits[i].doc);
                innerName = hitDoc.get("name");
                String[] terms = hitDoc.get("name").split(".");
                if (terms.length > 0) {
                    for (int j = 0; j < terms.length; j++) {
                        if (terms[j].toLowerCase().contains(pattern.toLowerCase())) {
                            innerName = terms[j];
                            break;
                        }
                    }
                }
                int typeInt = Integer.parseInt(hitDoc.get("type"));
                String type = "";
                for (int k = 0; k < DocumentKind.values().length; k++) {
                    if (typeInt == DocumentKind.values()[k].getIntValue()) {
                        type = DocumentKind.values()[k].getStringValue();
                        break;
                    }
                }
                int visibilityInt = Integer.parseInt(hitDoc.get("visibility"));
                String visibility = "";
                for (int k = 0; k < TypeVisibility.values().length; k++) {
                    if (visibilityInt == TypeVisibility.values()[k].getIntValue()) {
                        visibility = TypeVisibility.values()[k].getStringValue();
                        break;
                    }
                }
                docs[i] = new TypeDocument(innerName, hitDoc.get("file"), hitDoc.get("project"), hitDoc.get("offset"),
                        type, visibility);
            }
            isearcher.close();
            ireader.close();
        } catch (IndexNotFoundException e) {
            indexManager.flush();
            docs = new TypeDocument[0];
        } catch (IOException | ParseException e) {
            throw Throwables.propagate(e);
        }
        return docs;
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

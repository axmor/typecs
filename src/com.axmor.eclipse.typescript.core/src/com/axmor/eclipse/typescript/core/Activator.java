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
     * @param pattern text to search
     * @return results
     */
    public String[] getSearchResults(String pattern) {
        IndexReader ireader;
        ScoreDoc[] hits = null;
        String[] docs = null;
        try {
            ireader = IndexReader.open(indexManager.getIdxDir());
            IndexSearcher isearcher = new IndexSearcher(ireader);
         // Parse a simple query that searches for "text":

            Sort sort = new Sort(new SortField[] {
                    SortField.FIELD_SCORE,
                    new SortField("score", SortField.INT)});
            TopFieldCollector topField = TopFieldCollector.create(sort, 100, true, true, true, false);
            
            QueryParser parser = new QueryParser(Version.LUCENE_35, "name", new StandardAnalyzer(Version.LUCENE_35));
            parser.setAllowLeadingWildcard(true);
            parser.setAutoGeneratePhraseQueries(false);
            Query query = parser.parse("name:(*" + pattern.toLowerCase() + "*) or terms:(" + pattern.toLowerCase() + "*)");
            isearcher.search(query, topField);
            hits = topField.topDocs().scoreDocs;
            // Iterate through the results:
            docs = new String[hits.length];
            for (int i = 0; i < hits.length; i++) {
                Document hitDoc = isearcher.doc(hits[i].doc);
                System.out.println(hitDoc.get("name") + ":" + hits[i].score + ":" + hitDoc.get("file"));
                docs[i] = hitDoc.toString();
            }            
            isearcher.close();
            ireader.close();
        } catch (IOException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return docs;
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

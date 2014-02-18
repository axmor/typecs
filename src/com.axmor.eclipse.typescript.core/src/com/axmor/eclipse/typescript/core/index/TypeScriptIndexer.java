/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.core.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NRTCachingDirectory;
import org.apache.lucene.util.Version;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.Activator;
import com.axmor.eclipse.typescript.core.TypeScriptAPIFactory;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;

/**
 * Indexer that uses Lucene engine and TS api.
 * 
 * @author Konstantin Zaitcev
 */
public class TypeScriptIndexer {

    /** Indexer constant. */
    private static final double IDX_MAX_CACHED_SIZE = 10.0;
    /** Indexer constant. */
    private static final double IDX_MAX_MERGED_SIZE = 1.0;

    /** Path to index file. */
    private File indexPath;
    /** Index writer. */
    private IndexWriter iwriter;
    /** Caching indexed directory. */
    private NRTCachingDirectory idxDir;

    /**
     * @return the idxDir
     */
    public NRTCachingDirectory getIdxDir() {
        return idxDir;
    }

    /**
     * Performs index setup and initial cleanup.
     */
    public TypeScriptIndexer() {
        this.indexPath = Activator.getDefault().getStateLocation().append("index").toFile();
        if (!this.indexPath.exists()) {
            this.indexPath.mkdirs();
        }

        try {
            idxDir = new NRTCachingDirectory(FSDirectory.open(indexPath), IDX_MAX_MERGED_SIZE, IDX_MAX_CACHED_SIZE);
            try {
                IndexReader ireader = IndexReader.open(idxDir, false);
                try {
                    for (int i = 0; i < ireader.numDocs(); i++) {
                        Document doc = ireader.document(i);
                        if (!ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(doc.get("file"))).exists()) {
                            ireader.deleteDocument(i);
                        }
                    }
                } finally {
                    ireader.close();
                }
            } catch (IndexNotFoundException e) {
                // ignore this exception
            }
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, analyzer);
            config.setMergeScheduler(idxDir.getMergeScheduler());
            iwriter = new IndexWriter(idxDir, config);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }

    }
    
    /**
     * Removes all instances from index related to given file.
     * 
     * @param path
     *            file path
     */
    public synchronized void removeFromIndex(String path) {
        try {
            iwriter.deleteDocuments(new Term("file", path));
        } catch (IOException e) {
            Activator.error(e);
        }
    }

    /**
     * Indexes TS file by path.
     * 
     * @param path
     *            file path
     */
    public void indexFile(String path) {
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
        String project = file.getProject().getName();

        removeFromIndex(path);
        try {
            JSONArray model = TypeScriptAPIFactory.getTypeScriptAPI(file.getProject()).getScriptModel(file);
            for (int i = 0; i < model.length(); i++) {
                JSONObject obj = model.getJSONObject(i);
                String name = obj.getString("name");
                String kind = obj.getString("kind");
                String modifier = obj.getString("kindModifiers");
                int offset = obj.getInt("minChar");

                if ("module".equals(obj.getString("containerKind"))) {
                    name = obj.getString("containerName") + "." + name;
                }
                switch (kind) {
                case "interface":
                    addDocumentToIndex(name, project, path, 1, 0, offset, file.getModificationStamp());
                    break;
                case "enum":
                    addDocumentToIndex(name, project, path, 2, 0, offset, file.getModificationStamp());
                    break;
                case "class":
                    addDocumentToIndex(name, project, path, 3, "private".equals(modifier) ? 1 : 0, offset,
                            file.getModificationStamp());
                    break;
                default:
                    break;
                }
            }
        } catch (Exception e) {
            Activator.error(e);
        }
    }

    /**
     * Adds document to lucene index.
     * 
     * @param name
     *            name
     * @param project
     *            project
     * @param file
     *            file
     * @param type
     *            type
     * @param visibility
     *            visibility
     * @param offset
     *            offset
     * @param modificationStamp
     *            modification stamp
     */
    private void addDocumentToIndex(String name, String project, String file, int type, int visibility, int offset,
            long modificationStamp) throws IOException {
        Document doc = new Document();
        doc.add(new Field("name", name, Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("terms", getTerms(name), Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("file", file, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("project", project, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new NumericField("score", Field.Store.YES, true).setIntValue(getScore(type, visibility)));
        doc.add(new NumericField("offset", Field.Store.YES, false).setIntValue(offset));
        doc.add(new NumericField("type", Field.Store.YES, false).setIntValue(type));
        doc.add(new NumericField("visibility", Field.Store.YES, false).setIntValue(visibility));
        doc.add(new NumericField("file_stamp", Field.Store.YES, false).setLongValue(modificationStamp));
        iwriter.addDocument(doc);
    }

    /**
     * @param type
     *            entry type (0 - interface, 1 - enum, 2 - class)
     * @param visibility
     *            visibility (0 - public, 1 - private)
     * @return score for search by type and visibility
     */
    private int getScore(int type, int visibility) {
        return type * 10 + visibility;
    }

    /**
     * @param name
     *            name
     * @return transformed name to string separated by whitespace for index terms
     */
    private String getTerms(String name) {
        ArrayList<String> res = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isSpaceChar(ch) || ch == '.' || ch == '_') {
                if (sb.length() > 0) {
                    res.add(sb.toString());
                    sb = new StringBuilder();
                }
            } else if (Character.isUpperCase(ch)) {
                if (sb.length() > 0) {
                    res.add(sb.toString());
                    sb = new StringBuilder();
                }
                sb.append(ch);
            } else {
                sb.append(ch);
            }
        }
        if (sb.length() > 0) {
            res.add(sb.toString());
        }
        return Joiner.on(' ').join(res);
    }

    /**
     * @param path
     *            path to file
     * @param modificationStamp
     *            modification stamp to check
     * @return <code>true</code> if file need reindex
     */
    public boolean checkFile(String path, long modificationStamp) {
        try {
            IndexReader ireader = IndexReader.open(idxDir, true);
            try {
                TermDocs docs = ireader.termDocs(new Term("file", path));
                try {
                    if (docs.next()) {
                        Document doc = ireader.document(docs.doc());
                        String stamp = doc.get("file_stamp");

                        if (modificationStamp == Long.parseLong(stamp)) {
                            return false;
                        }
                    }
                } finally {
                    docs.close();
                }
            } finally {
                ireader.close();
            }
        } catch (IndexNotFoundException e) {
            // ignore exception
        } catch (IOException e) {
            Activator.error(e);
        }
        return true;
    }

    /**
     * Closes lucene indexes.
     */
    public void close() {
        try {
            iwriter.close(true);
            idxDir.close();
        } catch (IOException e) {
            Activator.error(e);
        }
    }
}

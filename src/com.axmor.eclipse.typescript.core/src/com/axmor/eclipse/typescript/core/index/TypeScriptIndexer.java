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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple4;
import org.mapdb.HTreeMap;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.Activator;
import com.axmor.eclipse.typescript.core.TypeScriptAPIFactory;
import com.google.common.collect.ImmutableSet;

/**
 * Indexer that uses MapDB engine and TS api.
 * 
 * @author Konstantin Zaitcev
 */
public class TypeScriptIndexer {

	/** Index version. */
	private static final int IDX_VERSION = 2;
	private static final Set<String> EMPTY_BASE_TYPES = Collections.emptySet();

	/** Index database. */
	private DB idxDB;

	/** File, Name, Parent, Index */
	public NavigableSet<Fun.Tuple4<String, String, String, IndexInfo>> idxTypes;
	/** FileName, Filename (Full workspace related filename) where B imports A */
	public NavigableSet<Fun.Tuple2<String, String>> idxReferences;
	/** FileName, ModificationStamp (Full workspace related filename) */
	public HTreeMap<String, Long> idxFiles;

	/**
	 * Enum for hardcoding of document kind case
	 * 
	 * @author Asya Vorobyova
	 */
	public enum DocumentKind {
		/** interface case */
		INTERFACE,
		/** enum case */
		ENUM,
		/** class case */
		CLASS;

		/**
		 * Gets corresponding int identifier
		 * 
		 * @return int value
		 */
		public int getIntValue() {
			if (name().equals("INTERFACE")) {
				return 1;
			}
			if (name().equals("ENUM")) {
				return 2;
			}
			if (name().equals("CLASS")) {
				return 3;
			}
			return 0;
		}

		/**
		 * @return string value
		 */
		public String getStringValue() {
			return name().toLowerCase();
		}
	}

	/**
	 * Enum for hardcoding of type modifier case
	 * 
	 * @author Asya Vorobyova
	 */
	public enum TypeVisibility {
		/** public */
		PUBLIC,
		/** private */
		PRIVATE;

		/**
		 * Gets corresponding int identifier
		 * 
		 * @return int value
		 */
		public int getIntValue() {
			if (name().equals("PUBLIC")) {
				return 0;
			}
			if (name().equals("PRIVATE")) {
				return 1;
			}
			return -1;
		}

		/**
		 * @return string value
		 */
		public String getStringValue() {
			return name().toLowerCase();
		}
	}

	/**
	 * Performs index setup and initial cleanup.
	 */
	public TypeScriptIndexer() {
		File indexPath = Activator.getDefault().getStateLocation().append("idx_" + IDX_VERSION).toFile();
		if (!indexPath.exists()) {
			indexPath.getParentFile().mkdirs();
		}

		if (this.idxDB != null) {
			this.idxDB.close();
		}
		this.idxDB = DBMaker.newFileDB(indexPath).closeOnJvmShutdown().make();
		this.idxTypes = idxDB.getTreeSet("types");
		this.idxReferences = idxDB.getTreeSet("refs");
		this.idxFiles = idxDB.getHashMap("files");

		final HashSet<String> typesFiles = new HashSet<>();

		for (Tuple4<String, String, String, IndexInfo> t : this.idxTypes) {
			typesFiles.add(t.a);
		}

		for (String file : typesFiles) {
			if (!ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(file)).exists()) {
				removeFromTypesIndex(file);
			}
		}

		final HashSet<String> refsFiles = new HashSet<>();

		for (Tuple2<String, String> t : this.idxReferences) {
			refsFiles.add(t.a);
		}

		for (String file : refsFiles) {
			if (!ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(file)).exists()) {
				removeFromRefsIndex(file);
			}
		}
		for (String file : ImmutableSet.<String> copyOf(idxFiles.keySet())) {
			if (!ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(file)).exists()) {
				idxFiles.remove(file);
			}
		}
		idxDB.commit();
	}

	/**
	 * Removes all instances from index related to given file.
	 * 
	 * @param path
	 *            file path
	 */
	public synchronized void removeFromTypesIndex(final String path) {
		Iterator<Tuple4<String, String, String, IndexInfo>> iterator = idxTypes.iterator();
		while (iterator.hasNext()) {
			Fun.Tuple4<String, String, String, IndexInfo> t = iterator.next();
			if (path.equals(t.a)) {
				iterator.remove();
			}
		}
		idxDB.commit();
	}

	/**
	 * Removes all instances from index related to given file.
	 * 
	 * @param path
	 *            file path
	 */
	public synchronized void removeFromRefsIndex(final String path) {
		Iterator<Tuple2<String, String>> iterator = idxReferences.iterator();
		while (iterator.hasNext()) {
			Fun.Tuple2<String, String> t = iterator.next();
			if (path.equals(t.a)) {
				iterator.remove();
			}
		}
		idxDB.commit();
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
		idxFiles.put(file.getFullPath().toString(), file.getLocation().toFile().lastModified());

		removeFromTypesIndex(path);
		try {
			JSONArray model = TypeScriptAPIFactory.getTypeScriptAPI(file.getProject()).getScriptModel(file);
			// JSONObject syntaxTree =
			// TypeScriptAPIFactory.getTypeScriptAPI(file.getProject()).getSyntaxTree(file);
			// if (syntaxTree != null) {
			// HashMap<String, Set<String>> baseTypes = new HashMap<>();
			// indexModelTree(syntaxTree.getJSONArray("statements"), "", baseTypes);
			//
			// indexModelFromSyntax(project, file, path, "", baseTypes, model);
			// } else {
			indexModel(project, file, path, model);
			indexReferences(file);
			// }
			idxDB.commit();
		} catch (Exception e) {
			Activator.error(e);
		}
	}

	/**
	 * @param file
	 */
	private void indexReferences(IFile file) {
		JSONArray references = TypeScriptAPIFactory.getTypeScriptAPI(file.getProject()).getReferences(file);
		if (references != null) {
			for (int i = 0; i < references.length(); i++) {
				try {
					JSONObject ref = references.getJSONObject(i);
					IFile refFile = file.getParent().getFile(new Path(ref.getString("fileName")));
					if (refFile.exists()) {
						idxReferences.add(new Fun.Tuple2<String, String>(refFile.getFullPath().toString(), file
								.getFullPath().toString()));
					}
				} catch (JSONException e) {
					Activator.error(e);
				}
			}
		}
	}

	/**
	 * Adds document to index.
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
	 */
	private void addDocumentToIndex(String qname, String name, String project, String file, int type, int visibility,
			int offset, Set<String> baseTypes) throws IOException {
		IndexInfo info = new IndexInfo();
		info.setName(name);
		info.setFile(file);
		info.setProject(project);
		info.setOffset(offset);
		info.setType(type);
		info.setVisibility(visibility);
		info.getParents().addAll(baseTypes);
		idxTypes.add(Fun.t4(file, qname, "", info));
	}

	/**
	 * @param path
	 *            path to file
	 * @param modificationStamp
	 *            modification stamp to check
	 * @return <code>true</code> if file need reindex
	 */
	public boolean checkFile(final String path, final long modificationStamp) {
		return !idxFiles.containsKey(path) || idxFiles.get(path) != modificationStamp;
	}

	/**
	 * Closes indexes.
	 */
	public void close() {
		idxDB.commit();
		idxDB.compact();
		idxDB.close();
	}

	/**
	 * Flushes changes on disk.
	 */
	public void flush() {
		idxDB.commit();
	}

	private void indexModel(String project, IFile file, String path, JSONArray model) throws JSONException, IOException {
		for (int i = 0; i < model.length(); i++) {
			JSONObject obj = model.getJSONObject(i);
			String name = obj.getString("text");
			String kind = obj.getString("kind");
			String modifier = obj.getString("kindModifiers");
			int offset = obj.getJSONArray("spans").getJSONObject(0).getInt("start");

			switch (kind) {
			case "interface":
				addDocumentToIndex(name, name, project, path, DocumentKind.INTERFACE.getIntValue(), 0, offset,
						EMPTY_BASE_TYPES);
				break;
			case "enum":
				addDocumentToIndex(name, name, project, path, DocumentKind.ENUM.getIntValue(), 0, offset,
						EMPTY_BASE_TYPES);
				break;
			case "class":
				addDocumentToIndex(
						name,
						name,
						project,
						path,
						DocumentKind.CLASS.getIntValue(),
						"private".equals(modifier) ? TypeVisibility.PRIVATE.getIntValue() : TypeVisibility.PUBLIC
								.getIntValue(), offset, EMPTY_BASE_TYPES);
				break;
			default:
				break;
			}

			if (obj.has("childItems") && !obj.isNull("childItems")) {
				indexModel(project, file, path, obj.getJSONArray("childItems"));
			}
		}
	}
}

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
import java.util.HashMap;
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
import org.mapdb.Fun.Tuple4;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.Activator;
import com.axmor.eclipse.typescript.core.TypeScriptAPIFactory;
import com.axmor.eclipse.typescript.core.TypeScriptUtils;
import com.google.common.collect.Iterables;

/**
 * Indexer that uses MapDB engine and TS api.
 * 
 * @author Konstantin Zaitcev
 */
public class TypeScriptIndexer {

	/** Index version. */
	private static final int IDX_VERSION = 1;
	private static final Set<String> EMPTY_BASE_TYPES = Collections.emptySet();

	/** Index database. */
	private DB idxDB;

	/** File, Name, Parent, Index */
	public NavigableSet<Fun.Tuple4<String, String, String, IndexInfo>> idxTypes;

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
		final HashSet<String> files = new HashSet<>();

		for (Tuple4<String, String, String, IndexInfo> t : this.idxTypes) {
			files.add(t.a);
		}

		for (String file : files) {
			if (!ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(file)).exists()) {
				removeFromIndex(file);
			}
		}
	}

	/**
	 * Removes all instances from index related to given file.
	 * 
	 * @param path
	 *            file path
	 */
	public synchronized void removeFromIndex(final String path) {
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
			if (TypeScriptUtils.isTypeScriptLegacyVersion()) {
				indexModelLegacy(project, file, path, model);
			} else {
				// JSONObject syntaxTree =
				// TypeScriptAPIFactory.getTypeScriptAPI(file.getProject()).getSyntaxTree(file);
				// if (syntaxTree != null) {
				// HashMap<String, Set<String>> baseTypes = new HashMap<>();
				// indexModelTree(syntaxTree.getJSONArray("statements"), "", baseTypes);
				//
				// indexModelFromSyntax(project, file, path, "", baseTypes, model);
				// } else {
					indexModel(project, file, path, model);
				// }
			}
			idxDB.commit();
		} catch (Exception e) {
			Activator.error(e);
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
	 * @param modificationStamp
	 *            modification stamp
	 */
	private void addDocumentToIndex(String qname, String name, String project, String file, int type, int visibility,
			int offset, Set<String> baseTypes, long modificationStamp) throws IOException {
		IndexInfo info = new IndexInfo();
		info.setName(name);
		info.setFile(file);
		info.setProject(project);
		info.setOffset(offset);
		info.setType(type);
		info.setVisibility(visibility);
		info.getParents().addAll(baseTypes);
		info.setModificationStamp(modificationStamp);
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
		return !Iterables.tryFind(idxTypes,
				new com.google.common.base.Predicate<Fun.Tuple4<String, String, String, IndexInfo>>() {
					@Override
					public boolean apply(Tuple4<String, String, String, IndexInfo> t) {
						return path.equals(t.a) && t.d.getModificationStamp() == modificationStamp;
					}
				}).isPresent();
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

	private void indexModelLegacy(String project, IFile file, String path, JSONArray model) throws JSONException,
			IOException {
		String qname = "";
		for (int i = 0; i < model.length(); i++) {
			JSONObject obj = model.getJSONObject(i);
			String name = obj.getString("name");
			String kind = obj.getString("kind");
			String modifier = obj.getString("kindModifiers");
			int offset = obj.getInt("minChar");
			qname = name;
			if ("module".equals(obj.getString("containerKind"))) {
				qname = obj.getString("containerName") + "." + name;
			}
			switch (kind) {
			case "interface":
				addDocumentToIndex(qname, name, project, path, DocumentKind.INTERFACE.getIntValue(), 0, offset,
						EMPTY_BASE_TYPES, file.getModificationStamp());
				break;
			case "enum":
				addDocumentToIndex(qname, name, project, path, DocumentKind.ENUM.getIntValue(), 0, offset,
						EMPTY_BASE_TYPES, file.getModificationStamp());
				break;
			case "class":
				addDocumentToIndex(
						qname,
						name,
						project,
						path,
						DocumentKind.CLASS.getIntValue(),
						"private".equals(modifier) ? TypeVisibility.PRIVATE.getIntValue() : TypeVisibility.PUBLIC
								.getIntValue(), offset, EMPTY_BASE_TYPES, file.getModificationStamp());
				break;
			default:
				break;
			}
		}
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
						EMPTY_BASE_TYPES, file.getModificationStamp());
				break;
			case "enum":
				addDocumentToIndex(name, name, project, path, DocumentKind.ENUM.getIntValue(), 0, offset,
						EMPTY_BASE_TYPES, file.getModificationStamp());
				break;
			case "class":
				addDocumentToIndex(
						name,
						name,
						project,
						path,
						DocumentKind.CLASS.getIntValue(),
						"private".equals(modifier) ? TypeVisibility.PRIVATE.getIntValue() : TypeVisibility.PUBLIC
								.getIntValue(), offset, EMPTY_BASE_TYPES, file.getModificationStamp());
				break;
			default:
				break;
			}

			if (obj.has("childItems") && !obj.isNull("childItems")) {
				indexModel(project, file, path, obj.getJSONArray("childItems"));
			}
		}
	}

	private void indexModelFromSyntax(String project, IFile file, String path, String qname,
			HashMap<String, Set<String>> baseTypes, JSONArray model) throws JSONException, IOException {
		// JSONArray imports = tree.getJSONArray("imports");
		for (int i = 0; i < model.length(); i++) {
			JSONObject obj = model.getJSONObject(i);
			String name = obj.getString("text");
			qname = name;
			String kind = obj.getString("kind");
			String modifier = obj.getString("kindModifiers");
			int offset = obj.getJSONArray("spans").getJSONObject(0).getInt("start");
			switch (kind) {
			case "interface":
				addDocumentToIndex(qname, name, project, path, DocumentKind.INTERFACE.getIntValue(), 0, offset,
						baseTypes.get(qname) != null ? baseTypes.get(qname) : EMPTY_BASE_TYPES,
						file.getModificationStamp());
				break;
			case "enum":
				addDocumentToIndex(qname, name, project, path, DocumentKind.ENUM.getIntValue(), 0, offset,
						baseTypes.get(qname) != null ? baseTypes.get(qname) : EMPTY_BASE_TYPES,
						file.getModificationStamp());
				break;
			case "class":
				addDocumentToIndex(
						qname,
						name,
						project,
						path,
						DocumentKind.CLASS.getIntValue(),
						"private".equals(modifier) ? TypeVisibility.PRIVATE.getIntValue() : TypeVisibility.PUBLIC
								.getIntValue(), offset, baseTypes.get(qname) != null ? baseTypes.get(qname)
								: EMPTY_BASE_TYPES,
						file.getModificationStamp());
				break;
			default:
				break;
			}

			if (obj.has("childItems") && !obj.isNull("childItems")) {
				indexModelFromSyntax(project, file, path, qname, baseTypes, obj.getJSONArray("childItems"));
			}
		}
	}

	private void indexModelTree(JSONArray statements, String name, HashMap<String, Set<String>> baseTypes)
			throws JSONException {
		for (int i = 0; i < statements.length(); i++) {
			JSONObject obj = statements.getJSONObject(i);
			if (obj.has("name")) {
				String n = obj.getJSONObject("name").getString("text");
				name = name.isEmpty() ? (name + n) : (name + "." + n);
				// System.out.println(name);
			}
			if (obj.has("baseTypes")) {
				baseTypes.put(name, getBaseTypes(obj.getJSONArray("baseTypes")));
			}
			if (obj.has("body")) {
				JSONObject body = obj.getJSONObject("body");
				if (body.has("statements")) {
					indexModelTree(body.getJSONArray("statements"), name, baseTypes);
				}
			}
		}
	}

	/**
	 * @param jsonArray
	 * @return
	 * @throws JSONException
	 */
	private Set<String> getBaseTypes(JSONArray types) throws JSONException {
		Set<String> baseTypes = new HashSet<>();
		for (int i = 0; i < types.length(); i++) {
			JSONObject obj = types.getJSONObject(i);
			if (obj.has("typeName")) {
				JSONObject tname = obj.getJSONObject("typeName");
				if (tname.has("left") && tname.has("right")) {
					baseTypes.add(tname.getJSONObject("left").getString("text") + "."
							+ tname.getJSONObject("right").getString("text"));
				} else if (tname.has("text")) {
					baseTypes.add(tname.getString("text"));
				}
			}
		}
		return baseTypes;
	}
}

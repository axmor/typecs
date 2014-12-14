/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.core.index;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Konstantin Zaitcev
 */
public class IndexInfo implements Serializable, Comparable<IndexInfo> {

	/** Serial version UID. */
	private static final long serialVersionUID = 7634961346855551158L;

	private String qname;
	private String name;
	private String file;
	private String project;
	private Set<String> parents = new HashSet<>();
	private int offset;
	private int visibility;
	private int type;
	private long modificationStamp;


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the file
	 */
	public String getFile() {
		return file;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * @return the project
	 */
	public String getProject() {
		return project;
	}

	/**
	 * @param project
	 *            the project to set
	 */
	public void setProject(String project) {
		this.project = project;
	}

	/**
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * @param offset
	 *            the offset to set
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * @return the visibility
	 */
	public int getVisibility() {
		return visibility;
	}

	/**
	 * @param visibility
	 *            the visibility to set
	 */
	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the modificationStamp
	 */
	public long getModificationStamp() {
		return modificationStamp;
	}

	/**
	 * @param modificationStamp
	 *            the modificationStamp to set
	 */
	public void setModificationStamp(long modificationStamp) {
		this.modificationStamp = modificationStamp;
	}

	/**
	 * type - entry type (0 - interface, 1 - enum, 2 - class) visibility - visibility (0 - public, 1
	 * - private)
	 * 
	 * @return score for search by type and visibility
	 */
	public int getScore() {
		return type * 10 + visibility;
	}


	@Override
	public int compareTo(IndexInfo o) {
		return o.toString().compareTo(this.toString());
	}

	/**
	 * @return the parents
	 */
	public Set<String> getParents() {
		return parents;
	}

	/**
	 * @return the qname
	 */
	public String getQname() {
		return qname;
	}

	/**
	 * @param qname the qname to set
	 */
	public void setQname(String qname) {
		this.qname = qname;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IndexInfo [qname=" + qname + ", name=" + name + ", file=" + file + ", project=" + project
				+ ", parents=" + parents + ", offset=" + offset + ", visibility=" + visibility + ", type=" + type
				+ ", modificationStamp=" + modificationStamp + "]";
	}

}

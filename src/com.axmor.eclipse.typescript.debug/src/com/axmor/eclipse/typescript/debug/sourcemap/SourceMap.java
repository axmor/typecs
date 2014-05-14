/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.sourcemap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Konstantin Zaitcev
 */
public class SourceMap {
    /** Full FS path of target JavaScript file. */
    private String file;
    /** Source map version. */
    private int version;
    /** Set of names. */
    private Set<String> names = new HashSet<>();
    /** List of */
    private List<SourceMapItem> maps = new ArrayList<>();

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
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * @return the names
     */
    public Set<String> getNames() {
        return names;
    }

    /**
     * @param names
     *            the names to set
     */
    public void setNames(Set<String> names) {
        this.names = names;
    }

    /**
     * @return the maps
     */
    public List<SourceMapItem> getMaps() {
        return maps;
    }

    /**
     * @param maps
     *            the maps to set
     */
    public void setMaps(List<SourceMapItem> maps) {
        this.maps = maps;
    }

    /**
     * @param lineNumber
     *            line number of TS file
     * @return matched {@link SourceMapItem} or <code>null</code> if not found
     */
    public SourceMapItem getItemByTSLine(long lineNumber) {
        for (SourceMapItem item : maps) {
            if (item.getTsLine() == lineNumber) {
                return item;
            }
        }
        return null;
    }

    /**
     * @param lineNumber
     *            line number of TS file
     * @return matched {@link SourceMapItem} or <code>null</code> if not found
     */
    public SourceMapItem getItemByJSLine(long lineNumber) {
        for (SourceMapItem item : maps) {
            if (item.getJsLine() == lineNumber) {
                return item;
            }
        }
        return null;
    }
}

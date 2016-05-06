/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.sourcemap;

import org.chromium.sdk.Breakpoint.Target.ScriptName;

/**
 * Source map item, contains mapping between TypeScript source and JavaScript target file.
 * 
 * @author Konstantin Zaitcev
 */
public class SourceMapItem {
    /** TypeScript source map relative file path. */
    private String tsFile;
    /** TypeScript source line. */
    private int tsLine;
    /** TypeScript column position in source line. */
    private int tsColumn;

    /** JavaScript source map relative file path. */
    private String jsFile;
    /** JavaScript source line. */
    private int jsLine;
    /** JavaScript column position in source line. */
    private int jsColumn;
    private ScriptName scriptName;

    /**
     * @return the tsFile
     */
    public String getTsFile() {
        return tsFile;
    }

    /**
     * @param tsFile
     *            the tsFile to set
     */
    public void setTsFile(String tsFile) {
        this.tsFile = tsFile;
    }

    /**
     * @return the tsLine
     */
    public int getTsLine() {
        return tsLine;
    }

    /**
     * @param tsLine
     *            the tsLine to set
     */
    public void setTsLine(int tsLine) {
        this.tsLine = tsLine;
    }

    /**
     * @return the tsColumn
     */
    public int getTsColumn() {
        return tsColumn;
    }

    /**
     * @param tsColumn
     *            the tsColumn to set
     */
    public void setTsColumn(int tsColumn) {
        this.tsColumn = tsColumn;
    }

    /**
     * @return the jsFile
     */
    public String getJsFile() {
        return jsFile;
    }

    /**
     * @param jsFile
     *            the jsFile to set
     */
    public void setJsFile(String jsFile) {
        this.jsFile = jsFile;
    }

    /**
     * @return the jsLine
     */
    public int getJsLine() {
        return jsLine;
    }

    /**
     * @param jsLine
     *            the jsLine to set
     */
    public void setJsLine(int jsLine) {
        this.jsLine = jsLine;
    }

    /**
     * @return the jsColumn
     */
    public int getJsColumn() {
        return jsColumn;
    }

    /**
     * @param jsColumn
     *            the jsColumn to set
     */
    public void setJsColumn(int jsColumn) {
        this.jsColumn = jsColumn;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(jsFile);
        builder.append("[");
        builder.append(jsLine);
        builder.append(",");
        builder.append(jsColumn);
        builder.append("] => ");
        builder.append(tsFile);
        builder.append("[");
        builder.append(tsLine);
        builder.append(",");
        builder.append(tsColumn);
        builder.append("]");
        return builder.toString();
    }

	public ScriptName getScriptName() {
		return scriptName;
	}

	public void setScriptName(ScriptName scriptName) {
		this.scriptName = scriptName;
	}
}

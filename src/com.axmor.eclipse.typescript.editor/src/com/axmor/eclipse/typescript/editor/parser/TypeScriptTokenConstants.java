/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor.parser;

/**
 * @author Konstantin Zaitcev
 */
public final class TypeScriptTokenConstants {
    /** Token constant. */
	public static final String TS_DEFAULT = "ts_default";
    /** Token constant. */
	public static final String TS_REFERENCE = "ts_reference";
    /** Token constant. */
	public static final String TS_KEYWORD = "ts_keyword";
    /** Token constant. */
	public static final String TS_STRING = "ts_string";
    /** Token constant. */
	public static final String TS_COMMENT = "ts_comment";
    /** Token constant. */
    public static final String TS_NUMBER = "ts_number";
    /** Token constant. */
	public static final String TS_JAVA_DOC = "ts_java_doc";
    /** Token constant. */
	public static final String TS_BOLD = "ts_bold";
	
	/**
     * Protect from initialization.
     */
    private TypeScriptTokenConstants() {
    }
}

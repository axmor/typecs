/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.preferences;

import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_BOLD_SUFFIX;
import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_BRACKETS;
import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_COMMENT;
import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_DEFAULT;
import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_JAVA_DOC;
import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_KEYWORD;
import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_NUMBER;
import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_REFERENCE;
import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_STRING;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

import com.axmor.eclipse.typescript.editor.Activator;

/**
 * @author Kudrin Pavel
 */
public class TypescriptPreferenceInitializer extends AbstractPreferenceInitializer {

	public TypescriptPreferenceInitializer() {
		super();
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		PreferenceConverter.setDefault(prefs, TS_DEFAULT, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(prefs, TS_REFERENCE, new RGB(90, 90, 90));
		PreferenceConverter.setDefault(prefs, TS_KEYWORD, new RGB(127, 0, 85));
		PreferenceConverter.setDefault(prefs, TS_JAVA_DOC, new RGB(63, 95, 121));
		PreferenceConverter.setDefault(prefs, TS_NUMBER, new RGB(51, 0, 102));
		PreferenceConverter.setDefault(prefs, TS_COMMENT, new RGB(63, 127, 25));
		PreferenceConverter.setDefault(prefs, TS_STRING, new RGB(42, 0, 255));
		PreferenceConverter.setDefault(prefs, TS_BRACKETS, new RGB(42, 0, 255));
		prefs.setDefault(TS_BRACKETS + TS_BOLD_SUFFIX, true);		
	}
}

/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.preferences;

import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.*;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;

import com.axmor.eclipse.typescript.editor.Activator;

/**
 * @author Kudrin Pavel
 */
public class TypescriptPreferenceInitializer extends
		AbstractPreferenceInitializer {
	
	public TypescriptPreferenceInitializer() {
		super();
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		PreferenceConverter.setDefault(prefs, TS_REFERENCE, new RGB(90, 90, 90));
		PreferenceConverter.setDefault(prefs, TS_KEYWORD, new RGB(127, 0, 85));
		PreferenceConverter.setDefault(prefs, TS_JAVA_DOC, new RGB(63, 95, 121));
		PreferenceConverter.setDefault(prefs, TS_NUMBER, new RGB(51, 0, 102));
		PreferenceConverter.setDefault(prefs, TS_COMMENT, new RGB(63, 127, 25));
		PreferenceConverter.setDefault(prefs, TS_STRING, new RGB(42, 0, 255));
		PreferenceConverter.setDefault(prefs, TS_BRACKETS, new RGB(42, 0, 255));
		prefs.setDefault(TS_BRACKETS + TS_BOLD_SUFFIX, true);
	}
	
	public static void setThemeBasedPreferences(IPreferenceStore store, boolean fireEvent) {
		ColorRegistry registry= null;
		if (PlatformUI.isWorkbenchRunning())
			registry= PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();

		setDefault(
				store,
				TS_STRING,
				findRGB(registry, "java_string", new RGB(42, 0, 255)), fireEvent);

	}
	
	private static void setDefault(IPreferenceStore store, String key, RGB newValue, boolean fireEvent) {
		if (!fireEvent) {
			PreferenceConverter.setDefault(store, key, newValue);
			return;
		}

		RGB oldValue= null;
		if (store.isDefault(key))
			oldValue= PreferenceConverter.getDefaultColor(store, key);

		PreferenceConverter.setDefault(store, key, newValue);

		if (oldValue != null && !oldValue.equals(newValue))
			store.firePropertyChangeEvent(key, oldValue, newValue);
	}
	
	private static RGB findRGB(ColorRegistry registry, String key, RGB defaultRGB) {
		if (registry == null)
			return defaultRGB;
		RGB rgb= registry.getRGB(key);
		if (rgb != null)
			return rgb;
		return defaultRGB;
	}
}

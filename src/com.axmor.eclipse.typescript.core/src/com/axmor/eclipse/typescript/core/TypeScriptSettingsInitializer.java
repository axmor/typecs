/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.core;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author kudrin
 *
 */
public class TypeScriptSettingsInitializer extends AbstractPreferenceInitializer {

    public TypeScriptSettingsInitializer() {
        super();
    }

    @Override
    public void initializeDefaultPreferences() {        
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault("indentSize", 4);
        store.setDefault("tabSize", 4);
        store.setDefault("newLineChar", "\r\n");
        store.setDefault("convertTabs", true);
        store.setDefault("insertSpaceComma", true);
        store.setDefault("insertSpaceSemicolon", true);
        store.setDefault("insertSpaceBinary", true);
        store.setDefault("insertSpaceKeywords", true);
        store.setDefault("insertSpaceFunction", false);
        store.setDefault("insertSpaceParenthesis", false);
        store.setDefault("placeBraceFunctions", false);
        store.setDefault("placeBraceBlocks", false);
        store.setDefault("insertCloseBrackets", true);
    }

}

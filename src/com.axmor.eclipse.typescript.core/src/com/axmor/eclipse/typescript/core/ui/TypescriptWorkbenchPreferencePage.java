/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.axmor.eclipse.typescript.core.ui;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.axmor.eclipse.typescript.core.Activator;
import com.axmor.eclipse.typescript.core.i18n.Messages;

/**
 * @author Konstantin Zaitcev
 */
public class TypescriptWorkbenchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    protected void createFieldEditors() {
        addField(new ComboFieldEditor("compiler_version", Messages.TypescriptWorkbenchPreferencePage_compiler_version,
                new String[][] { { "0.9.5", "0.9.5" } }, getFieldEditorParent()));
    }
}

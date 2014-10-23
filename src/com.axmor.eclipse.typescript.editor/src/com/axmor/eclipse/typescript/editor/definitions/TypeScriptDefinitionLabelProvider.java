/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor.definitions;

import static com.google.common.base.Strings.nullToEmpty;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.axmor.eclipse.typescript.editor.TypeScriptUIImages;
import com.axmor.eclipse.typescript.editor.parser.TypeScriptImageKeys;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptDefinitionLabelProvider extends LabelProvider {

    @Override
    public Image getImage(Object element) {
        return TypeScriptUIImages.getImage(TypeScriptImageKeys.IMG_DTS_FILE);
    }

    @Override
    public String getText(Object element) {
        TypeScriptDefinition module = (TypeScriptDefinition) element;
        if (module != null) {
            return nullToEmpty(module.getName()) + " (" + nullToEmpty(module.getVersion()) + ")";
        }
        return "";
    }
}

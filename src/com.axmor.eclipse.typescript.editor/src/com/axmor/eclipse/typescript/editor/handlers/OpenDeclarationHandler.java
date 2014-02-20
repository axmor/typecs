/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.axmor.eclipse.typescript.editor.TypeScriptEditor;
import com.axmor.eclipse.typescript.editor.TypeScriptEditorUtils;

/**
 * Handler to perform declaration opening
 * 
 * @author Asya Vorobyova
 *
 */
public class OpenDeclarationHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        //ISelection selection = HandlerUtil.getCurrentSelection(event);
        TypeScriptEditor editor = (TypeScriptEditor) HandlerUtil.getActiveEditor(event);
        if (editor == null) {
            return null;
        }
        TypeScriptEditorUtils.openDeclaration(editor);
        return null;
    }

}

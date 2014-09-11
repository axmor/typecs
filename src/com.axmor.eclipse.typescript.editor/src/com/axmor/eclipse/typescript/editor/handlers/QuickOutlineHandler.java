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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.axmor.eclipse.typescript.editor.TypeScriptEditor;
import com.axmor.eclipse.typescript.editor.TypeScriptProjectionViewer;

/**
 * Handler to show quick outline popup dialog
 * 
 * @author Asya Vorobyova
 */
public class QuickOutlineHandler extends AbstractHandler {

    /** The text operation target */
    private ITextOperationTarget operationTarget;
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        TypeScriptEditor editor = (TypeScriptEditor) HandlerUtil.getActiveEditor(event);
        if (editor == null) {
            return null;
        }
        operationTarget = (TypeScriptProjectionViewer) editor.getViewer();
        final int operationCode = TypeScriptProjectionViewer.QUICK_OUTLINE;
        
        Shell shell = HandlerUtil.getActiveShell(event);
        if (!operationTarget.canDoOperation(operationCode)) {
            if (shell != null) {
                MessageDialog.openError(shell, "Quick Outline", "Cannot show a quick outline dialog");
            }    
            return null;
        }

        Display display = null;
        if (shell != null && !shell.isDisposed()) {
            display = shell.getDisplay();
        }    

        BusyIndicator.showWhile(display, new Runnable() {
            public void run() {
                operationTarget.doOperation(operationCode);
            }
        });
        
        return null;
    }

}

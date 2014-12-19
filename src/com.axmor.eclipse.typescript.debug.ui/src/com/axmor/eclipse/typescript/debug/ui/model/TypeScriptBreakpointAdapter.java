/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.ui.model;

import static com.axmor.eclipse.typescript.debug.launching.TypeScriptDebugConstants.TS_DEBUG_MODEL;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

import com.axmor.eclipse.typescript.debug.model.TypeScriptLineBreakpoint;

/**
 * Adapter to create TypeScript line breakpoint and register at breakpoint manager.
 * 
 * @author Konstantin Zaitcev
 */
public class TypeScriptBreakpointAdapter implements IToggleBreakpointsTarget {

    @Override
    public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
        if (part != null && part instanceof ITextEditor) {
            ITextEditor textEditor = (ITextEditor) part;
            IResource resource = (IResource) textEditor.getEditorInput().getAdapter(IResource.class);
            ITextSelection textSelection = (ITextSelection) selection;
            int lineNumber = textSelection.getStartLine();
            IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(TS_DEBUG_MODEL);
            for (int i = 0; i < breakpoints.length; i++) {
                IBreakpoint breakpoint = breakpoints[i];
                if (resource.equals(breakpoint.getMarker().getResource())) {
                    if (((ILineBreakpoint) breakpoint).getLineNumber() == (lineNumber + 1)) {
                        breakpoint.delete();
                        return;
                    }
                }
            }
            TypeScriptLineBreakpoint lineBreakpoint = new TypeScriptLineBreakpoint(resource, lineNumber + 1);
            DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(lineBreakpoint);
        }
    }

    @Override
    public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
        return true;
    }

    @Override
    public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
        // not supported
    }

    @Override
    public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
        // not supported
        return false;
    }

    @Override
    public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
        // not supported
    }

    @Override
    public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
        // not supported
        return false;
    }
}

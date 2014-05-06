/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.model;

import static com.axmor.eclipse.typescript.debug.launching.TypeScriptDebugConstants.TS_DEBUG_MODEL;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LineBreakpoint;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptLineBreakpoint extends LineBreakpoint {

    /**
     * Default constructor need for breakpoint manager for recreate. 
     */
    public TypeScriptLineBreakpoint() {
        // empty block
    }
    
    public TypeScriptLineBreakpoint(final IResource resource, final int lineNumber) throws CoreException {
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                IMarker marker = resource.createMarker("com.axmor.eclipse.typescript.debug.typeScriptBreakpoint");
                setMarker(marker);
                setEnabled(true);
                marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
                marker.setAttribute(IBreakpoint.ID, getModelIdentifier());
                marker.setAttribute(IMarker.MESSAGE, "Line Breakpoint: " + resource.getName() + " [line: " + lineNumber
                        + "]");
            }
        };
        run(getMarkerRule(resource), runnable);
    }

    @Override
    public String getModelIdentifier() {
        return TS_DEBUG_MODEL;
    }
}

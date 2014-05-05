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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LineBreakpoint;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptLineBreakpoint extends LineBreakpoint {

    public TypeScriptLineBreakpoint(IResource resource, int lineNumber) throws CoreException {
        IMarker marker = resource.createMarker("com.axmor.eclipse.typescript.debug.typeScriptBreakpoint");
        setMarker(marker);
        setEnabled(true);
        ensureMarker().setAttribute(IMarker.LINE_NUMBER, lineNumber);
        ensureMarker().setAttribute(IBreakpoint.ID, TS_DEBUG_MODEL);
    }

    @Override
    public String getModelIdentifier() {
        return TS_DEBUG_MODEL;
    }
}

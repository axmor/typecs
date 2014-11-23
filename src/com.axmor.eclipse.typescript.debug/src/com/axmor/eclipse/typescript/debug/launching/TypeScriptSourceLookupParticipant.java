/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.debug.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;

import com.axmor.eclipse.typescript.debug.model.TypeScriptStackFrame;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptSourceLookupParticipant extends AbstractSourceLookupParticipant {

    @Override
    public String getSourceName(Object object) throws CoreException {
        if (object instanceof TypeScriptStackFrame) {
            TypeScriptStackFrame sf = (TypeScriptStackFrame) object;
            return sf.getSourceName();
        }
        return null;
    }
}

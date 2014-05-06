/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.model;

import static com.axmor.eclipse.typescript.debug.launching.TypeScriptDebugConstants.TS_DEBUG_MODEL;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * Common debug element.
 * 
 * @author Konstantin Zaitcev
 */
public abstract class TypeScriptDebugElement extends PlatformObject implements IDebugElement {

    private IDebugTarget target;

    public TypeScriptDebugElement(IDebugTarget target) {
        this.target = target;
    }

    @Override
    public String getModelIdentifier() {
        return TS_DEBUG_MODEL;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IDebugElement.class) {
            return this;
        }
        return super.getAdapter(adapter);
    }

    @Override
    public IDebugTarget getDebugTarget() {
        return target;
    }

    @Override
    public ILaunch getLaunch() {
        return target.getLaunch();
    }
}

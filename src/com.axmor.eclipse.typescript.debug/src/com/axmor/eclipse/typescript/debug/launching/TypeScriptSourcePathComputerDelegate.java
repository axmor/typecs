/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.launching;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptSourcePathComputerDelegate implements ISourcePathComputerDelegate {

    @Override
    public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration, IProgressMonitor monitor)
            throws CoreException {
        ISourceContainer sourceContainer = null;
        // FIXME: KOS need rewrite
        sourceContainer = new ProjectSourceContainer(ResourcesPlugin.getWorkspace().getRoot().getProject("warship_sample"), false);
        if (sourceContainer == null) {
            sourceContainer = new WorkspaceSourceContainer();
        }
        
        return new ISourceContainer[] { sourceContainer };
    }
}

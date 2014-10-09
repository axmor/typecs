/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.builder.builder;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Konstantin Zaitcev
 */
public class RemoveTypeScriptSupportHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection != null && selection instanceof IStructuredSelection) {
            for (Object obj : ((IStructuredSelection) selection).toList()) {
                if (obj instanceof IAdaptable) {
                    IProject proj = (IProject) ((IAdaptable) obj).getAdapter(IProject.class);
                    if (proj != null) {
                        try {
                            IProjectDescription description = proj.getDescription();
                            String[] natures = description.getNatureIds();

                            for (int i = 0; i < natures.length; ++i) {
                                if (TypescriptNature.NATURE_ID.equals(natures[i])) {
                                    // Remove the nature
                                    String[] newNatures = new String[natures.length - 1];
                                    System.arraycopy(natures, 0, newNatures, 0, i);
                                    System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
                                    description.setNatureIds(newNatures);
                                    proj.setDescription(description, null);
                                    continue;
                                }
                            }
                        } catch (CoreException e) {
                            throw new ExecutionException(e.getMessage(), e);
                        }
                    }
                }

            }
        }
        return null;
    }
}

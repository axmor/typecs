/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

/**
 * @author Konstantin Zaitcev
 */
public class PerspectiveFactory implements IPerspectiveFactory {

    @Override
    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(true);

        IFolderLayout leftFolder = layout.createFolder("left", IPageLayout.LEFT, 0.22f, editorArea);
        IFolderLayout bottomFolder = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.65f, editorArea);
        IFolderLayout rightFolder = layout.createFolder("right", IPageLayout.RIGHT, 0.80f, editorArea);

        leftFolder.addView("org.eclipse.ui.navigator.ProjectExplorer");
        leftFolder.addView("org.eclipse.jdt.ui.PackageExplorer");


        bottomFolder.addView("org.eclipse.ui.views.ProblemView");
        bottomFolder.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);

        rightFolder.addView("org.eclipse.ui.views.ContentOutline");

        layout.addActionSet("org.eclipse.debug.ui.launchActionSet");
    }
    
}

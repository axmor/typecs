/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.rename;

import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * @author Konstantin Zaitcev
 */
public class RenameWizard extends RefactoringWizard {
    
    /** Rename info bean. */
    private final RenameInfo info;

    /**
     * Constructor.
     * 
     * @param processor
     *            refactoring processor
     * @param info
     *            rename info bean
     */
    public RenameWizard(final RenameProcessor processor, final RenameInfo info) {
        super(new ProcessorBasedRefactoring(processor), RefactoringWizard.WIZARD_BASED_USER_INTERFACE);
        this.info = info;
        setDefaultPageTitle("Rename Element");
    }

    @Override
    protected void addUserInputPages() {
        addPage(new RenameInputPage(info));
    }
}

/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.rename;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.google.common.base.Strings;

/**
 * @author Konstantin Zaitcev
 */
public class RenameInputPage extends UserInputWizardPage {

    /** Rename info to configure. */
    private final RenameInfo info;
    /** UI field. */
    private Text newName;

    /**
     * Constructs from info.
     * 
     * @param info
     *            rename info
     */
    public RenameInputPage(final RenameInfo info) {
        super(RenameInputPage.class.getName());
        this.info = info;
        setTitle("Rename Element");
        setMessage("Rename element and all references in TypeScript source files");
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = createRootComposite(parent);
        setControl(composite);
        new Label(composite, SWT.NONE).setText("New name:");
        newName = new Text(composite, SWT.BORDER);
        newName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        newName.setText(Strings.nullToEmpty(info.getOldName()));
        newName.selectAll();
        newName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(final KeyEvent e) {
                info.setNewName(newName.getText());
                validate();
            }
        });

        validate();
    }

    /**
     * Creates a root composite.
     * 
     * @param parent
     *            parent composite
     * @return composite
     */
    private Composite createRootComposite(final Composite parent) {
        Composite result = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 10;
        gridLayout.marginHeight = 10;
        result.setLayout(gridLayout);
        initializeDialogUnits(result);
        Dialog.applyDialogFont(result);
        return result;
    }

    /**
     * Validates name input.
     */
    private void validate() {
        String txt = newName.getText();
        setPageComplete(!Strings.isNullOrEmpty(txt) && isJavaIdentifier(txt) && !txt.equals(info.getOldName()));
    }

    /**
     * @param s
     *            string to check
     * @return <code>true</code> if string contains only valid Java name chars
     */
    private static boolean isJavaIdentifier(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
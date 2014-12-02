/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.axmor.eclipse.typescript.builder.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import com.axmor.eclipse.typescript.builder.Activator;
import com.axmor.eclipse.typescript.builder.i18n.Messages;
import com.axmor.eclipse.typescript.core.TypeScriptCompilerSettings;
import com.axmor.eclipse.typescript.core.ui.SWTFactory;
import com.axmor.eclipse.typescript.core.ui.TypeScriptElementSelectionDialog;

/**
 * TypeScript build properties page.
 * 
 * @author Konstantin Zaitcev
 */
public class TypescriptProjectPropertiesPage extends PropertyPage implements IWorkbenchPropertyPage {
    
    /** UI control for compileSourceResource. */
    private Text compileSourceResource;
    /** UI control for compileTargetResource. */
    private Text compileTargetResource;
    /**
     * relative path from {@link #target} to output file is detected based on relative path from
     * {@link #source} to input file
     */
    private Button targetRelativePathBasedOnSource;

    /** UI control for noResolve. */
    private Button noResolve;
    /** UI control for noImplicitAny. */
    private Button noImplicitAny;
    /** UI control for sourceMap. */
    private Button sourceMap;
    /** UI control for module. */
    private Combo module;
    /** UI control for targetVersion. */
    private Combo targetVersion;
    /** UI control for declaration. */
    private Button declaration;
    /** UI control for removeComments. */
    private Button removeComments;
    /** UI control for mapRoot. */
    private Text mapRoot;
    /** UI control for mapRootLabel. */
    private Label mapRootLabel;
    /** UI control for mapRootButton. */
    private Button mapRootButton;

    /**
     * Constructor.
     */
    public TypescriptProjectPropertiesPage() {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription(Messages.ProjectPrefPage_title);
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, 0, 0, GridData.FILL);

        // compile section
        Group group = SWTFactory.createGroup(composite, Messages.ProjectPrefPage_compileSection, 3, 1,
                GridData.FILL_HORIZONTAL);

        // source directory
        SWTFactory.createLabel(group, Messages.ProjectPrefPage_sourceLabel, 1);
        compileSourceResource = SWTFactory.createSingleText(group, 1);
        createBrowseButton(group, compileSourceResource, Messages.ProjectPrefPage_sourceSelectionTitle,
                Messages.ProjectPrefPage_sourceSelectionDesc);

        // target directory
        SWTFactory.createLabel(group, Messages.ProjectPrefPage_targetLabel, 1);
        compileTargetResource = SWTFactory.createSingleText(group, 1);
        createBrowseButton(group, compileTargetResource, Messages.ProjectPrefPage_targetSelectionTitle,
                Messages.ProjectPrefPage_targetSelectionDesc);

        targetRelativePathBasedOnSource = SWTFactory.createCheckButton(group,
                Messages.ProjectPrefPage_targetRelativePathBasedOnSource, null, true, 3);
        noImplicitAny = SWTFactory.createCheckButton(group, Messages.ProjectPrefPage_noImlicitAny, null, true, 3);
        noResolve = SWTFactory.createCheckButton(group, Messages.ProjectPrefPage_noResolve, null, true, 3);

        // generation section
        Group group2 = SWTFactory.createGroup(composite, Messages.ProjectPrefPage_generationSection, 2, 1,
                GridData.FILL_HORIZONTAL);

        // module
        SWTFactory.createLabel(group2, Messages.ProjectPrefPage_moduleLabel, 1);
        module = SWTFactory.createCombo(group2, SWT.READ_ONLY, 1, new String[] { "default", "commonjs", "amd" });

        // target version
        SWTFactory.createLabel(group2, Messages.ProjectPrefPage_targetVersion, 1);
        targetVersion = SWTFactory.createCombo(group2, SWT.READ_ONLY, 1, new String[] { "ES5", "ES3" });

        declaration = SWTFactory.createCheckButton(group2, Messages.ProjectPrefPage_declaration, null, true, 2);
        removeComments = SWTFactory.createCheckButton(group2, Messages.ProjectPrefPage_removeComments, null, true, 2);

        // source map
        sourceMap = SWTFactory.createCheckButton(group2, Messages.ProjectPrefPage_sourceMap, null, true, 2);
        sourceMap.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshWidgets();
            }

        });
        mapRootLabel = SWTFactory.createLabel(group2, Messages.ProjectPrefPage_mapRootLabel, 2);
        mapRoot = SWTFactory.createSingleText(group2, 1);
        mapRootButton = createBrowseButton(group2, mapRoot, Messages.ProjectPrefPage_rootSelectionTitle,
                Messages.ProjectPrefPage_rootSelectionDesc);

        loadPreferences();
        return composite;
    }

    @Override
    public boolean performOk() {
        TypeScriptCompilerSettings settings = TypeScriptCompilerSettings.load((IProject) ((IAdaptable) getElement())
                .getAdapter(IProject.class));
        settings.setSource(compileSourceResource.getText());
        settings.setTarget(compileTargetResource.getText());
        settings.setTargetRelativePathBasedOnSource(targetRelativePathBasedOnSource.getSelection());
        settings.setNoResolve(noResolve.getSelection());
        settings.setNoImplicitAny(noImplicitAny.getSelection());
        settings.setModule(module.getText());
        settings.setTargetVersion(targetVersion.getText());
        settings.setGenerateDeclaration(declaration.getSelection());
        settings.setRemoveComments(removeComments.getSelection());
        settings.setSourceMap(sourceMap.getSelection());
        settings.setMapRoot(mapRoot.getText());
        settings.save();
        return super.performOk();
    }

    @Override
    protected void performDefaults() {
        loadPreferences();
        super.performDefaults();
    }

    /**
     * Refreshes widget on updates.
     */
    private void refreshWidgets() {
        mapRoot.setEnabled(sourceMap.getSelection());
        mapRootButton.setEnabled(sourceMap.getSelection());
        mapRootLabel.setEnabled(sourceMap.getSelection());
    }

    /**
     * Loads preferences from store to UI.
     */
    private void loadPreferences() {
        TypeScriptCompilerSettings settings = TypeScriptCompilerSettings.load((IProject) ((IAdaptable) getElement())
                .getAdapter(IProject.class));
        compileSourceResource.setText(settings.getSource());
        compileTargetResource.setText(settings.getTarget());
        targetRelativePathBasedOnSource.setSelection(settings.isTargetRelativePathBasedOnSource());
        noResolve.setSelection(settings.isNoResolve());
        noImplicitAny.setSelection(settings.isNoImplicitAny());
        module.setText(settings.getModule());
        targetVersion.setText(settings.getTargetVersion());
        declaration.setSelection(settings.isGenerateDeclaration());
        removeComments.setSelection(settings.isRemoveComments());
        sourceMap.setSelection(settings.isSourceMap());
        mapRoot.setText(settings.getMapRoot());

        refreshWidgets();
    }

    /***
     * @param parent
     *            parent control
     * @param text
     *            button text
     * @param title
     *            dialog title
     * @param message
     *            dialog description
     * @return browse button that opens selection dialog
     */
    private Button createBrowseButton(Composite parent, final Text text, final String title, final String message) {
        Button button = new Button(parent, SWT.NONE);
        button.setText(Messages.ProjectPrefPage_browseButton);
        final IProject project = (IProject) this.getElement().getAdapter(IProject.class);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IResource resource = new TypeScriptElementSelectionDialog(getShell(), title, message, project)
                        .open(text.getText(), false);
                if (resource != null) {
                    text.setText(resource.getProjectRelativePath().toString());
                }
                super.widgetSelected(e);
            }
        });
        return button;
    }
}

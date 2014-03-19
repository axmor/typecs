/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.definitions;

import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

import com.axmor.eclipse.typescript.core.TypeScriptCompilerSettings;
import com.axmor.eclipse.typescript.core.ui.SWTFactory;
import com.axmor.eclipse.typescript.core.ui.TypeScriptElementSelectionDialog;
import com.axmor.eclipse.typescript.editor.Activator;
import com.google.common.base.Strings;

/**
 * @author Konstantin Zaitcev
 */
public class AddTypeScriptDefinitionDialog extends FilteredItemsSelectionDialog {

    /** Section name for dialog settings. */
    private static final String DIALOG_SETTINGS = AddTypeScriptDefinitionDialog.class.getName();
    /** List of loaded modules. */
    private static List<TypeScriptDefinition> definitions;

    /** Project. */
    private IProject project;
    /** Target directory. */
    private Text targetDir;

    /** Info field. */
    private Text defProject;
    /** Info field. */
    private Text defName;
    /** Info field. */
    private Text defVersion;
    /** Info field. */
    private Text defAuthors;
    /** Info field. */
    private Link defLink;
    /** Info field. */
    private Text defDescription;

    /**
     * @param shell
     *            shell
     * @param project
     *            project
     */
    public AddTypeScriptDefinitionDialog(Shell shell, IProject project) {
        super(shell, true);
        this.project = project;
        setTitle("Add TypeScript Definition from Repository");
        setHelpAvailable(false);
        setInitialPattern("**");
        setListLabelProvider(new TypeScriptDefinitionLabelProvider());
        getDialogSettings().put("ShowStatusLine", false);
    }

    /**
     * @return the targetDir
     */
    public IContainer getTargetDir() {
        String folder = getDialogSettings().get("target_dir");
        if (Strings.isNullOrEmpty(folder)) {
            return project;
        }
        return project.getFolder(folder);
    }

    @Override
    protected Control createExtendedContentArea(Composite parent) {
        Composite root = SWTFactory.createComposite(parent, 1, 0, GridData.FILL_HORIZONTAL);
        Composite composite = SWTFactory.createComposite(root, 3, 0, GridData.FILL_HORIZONTAL);

        SWTFactory.createLabel(composite, "Target Directory:", 0);
        targetDir = SWTFactory.createText(composite, SWT.BORDER, 0, GridData.FILL_HORIZONTAL);

        if (getDialogSettings().get("target_dir") == null) {
            getDialogSettings().put("target_dir", TypeScriptCompilerSettings.load(project).getSource());
        }

        targetDir.setText(getDialogSettings().get("target_dir"));

        Button button = SWTFactory.createPushButton(composite, "Browse...", null);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IResource resource = new TypeScriptElementSelectionDialog(getShell(), "", "", project).open(targetDir
                        .getText());
                if (resource != null) {
                    targetDir.setText(resource.getProjectRelativePath().toString());
                    getDialogSettings().put("target_dir", targetDir.getText());
                }

                super.widgetSelected(e);
            }
        });
        Composite info = SWTFactory.createComposite(root, 4, 0, GridData.FILL_HORIZONTAL);
        SWTFactory.createLabel(info, "Project:", 1);
        defProject = SWTFactory.createText(info, SWT.READ_ONLY | SWT.BORDER, 1);
        SWTFactory.createLabel(info, "Version:", 1);
        defVersion = SWTFactory.createText(info, SWT.READ_ONLY | SWT.BORDER, 1);
        SWTFactory.createLabel(info, "Name:", 1);
        defName = SWTFactory.createText(info, SWT.READ_ONLY | SWT.BORDER, 1);
        SWTFactory.createLabel(info, "Authors:", 1);
        defAuthors = SWTFactory.createText(info, SWT.READ_ONLY | SWT.BORDER, 1);
        SWTFactory.createLabel(info, "Description:", 1);
        defDescription = SWTFactory.createText(info, SWT.READ_ONLY | SWT.BORDER, 3);
        SWTFactory.createLabel(info, "Project URL:", 1);
        defLink = new Link(info, SWT.NONE);
        defLink.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    Program.launch(e.text);
                } catch (Exception ex) {
                    Activator.error(ex);
                }
            }
        });
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        defLink.setLayoutData(gd);
        return root;
    }

    @Override
    protected IDialogSettings getDialogSettings() {
        IDialogSettings settings = Activator.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS);

        if (settings == null) {
            settings = Activator.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS);
        }

        return settings;
    }

    @Override
    protected void handleSelected(StructuredSelection selection) {
        super.handleSelected(selection);
        if (selection != null && selection.size() == 1) {
            TypeScriptDefinition def = (TypeScriptDefinition) selection.getFirstElement();
            defName.setText(def.getName());
            defProject.setText(def.getProject());
            defVersion.setText(def.getVersion());
            defAuthors.setText(def.getAuthor());
            defLink.setText(String.format("<a href=\"%1$s\">%1$s</a>", def.getProjectUrl()));
            defDescription.setText(def.getDescription());
        } else {
            defName.setText("");
            defProject.setText("");
            defVersion.setText("");
            defAuthors.setText("");
            defLink.setText("");
            defDescription.setText("");
        }
    }

    @Override
    protected void fillViewMenu(IMenuManager menuManager) {
        super.fillViewMenu(menuManager);
        menuManager.removeAll();
    }

    @Override
    protected IStatus validateItem(Object item) {
        return Status.OK_STATUS;
    }

    @Override
    protected ItemsFilter createFilter() {
        return new ItemsFilter() {
            public boolean matchItem(Object item) {
                TypeScriptDefinition matchItem = (TypeScriptDefinition) item;
                String pattern = patternMatcher.getPattern();
                if (pattern.indexOf("*") != 0 & pattern.indexOf("?") != 0 & pattern.indexOf(".") != 0) {
                    pattern = "*" + pattern;
                    patternMatcher.setPattern(pattern);
                }
                return patternMatcher.matches(matchItem.getName());
            }

            public boolean isConsistentItem(Object item) {
                return true;
            }
        };
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Comparator getItemsComparator() {
        return new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };
    }

    @Override
    protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
            IProgressMonitor progressMonitor) throws CoreException {
        if (definitions == null) {
            definitions = TypeScriptDefinitionsLoader.load(progressMonitor);
        }
        for (TypeScriptDefinition module : definitions) {
            contentProvider.add(module, itemsFilter);
        }
    }

    @Override
    public String getElementName(Object item) {
        return item instanceof TypeScriptDefinition ? ((TypeScriptDefinition) item).getName() : null;
    }
}

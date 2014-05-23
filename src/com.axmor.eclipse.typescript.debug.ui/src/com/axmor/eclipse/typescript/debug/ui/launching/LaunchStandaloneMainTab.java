/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.ui.launching;

import static com.axmor.eclipse.typescript.debug.ui.DebugUIConstants.IMG_MAIN_TAB;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.ide.IDE;

import com.axmor.eclipse.typescript.builder.builder.TypescriptNature;
import com.axmor.eclipse.typescript.core.ui.SWTFactory;
import com.axmor.eclipse.typescript.core.ui.TypeScriptElementSelectionDialog;
import com.axmor.eclipse.typescript.debug.launching.TypeScriptDebugConstants;
import com.axmor.eclipse.typescript.debug.ui.Activator;

/**
 * Main launcher tab for standalone mode.
 * 
 * @author Konstantin Zaitcev
 */
public class LaunchStandaloneMainTab extends AbstractLaunchConfigurationTab {

    private Text projectTxt;
    private Button projectBtn;
    private Text fileTxt;
    private Button fileBtn;

    @Override
    public void createControl(Composite parent) {
        Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);
        ((GridLayout) comp.getLayout()).verticalSpacing = 0;
        createProjectEditor(comp);
        createVerticalSpacer(comp, 1);
        createMainTypeEditor(comp);
        setControl(comp);
    }

    /**
     * Creates main file editor group.
     * 
     * @param parent
     *            parent control
     */
    private void createMainTypeEditor(Composite parent) {
        Group group = SWTFactory.createGroup(parent, "Main File:", 2, 1, GridData.FILL_HORIZONTAL);
        fileTxt = SWTFactory.createSingleText(group, 1);
        fileTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        fileBtn = createPushButton(group, "Browse...", null);
        fileBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectTxt.getText().trim());
                if (project != null && project.isAccessible()) {
                    IResource resource = new TypeScriptElementSelectionDialog(getShell(), "Main File Selection",
                            "Select a main executable TypeScript file", project).open(fileTxt.getText().trim(), false);
                    if (resource != null) {
                        fileTxt.setText(resource.getProjectRelativePath().toString());
                    }
                }
            }
        });
    }

    /**
     * Creates project selection editor group.
     * 
     * @param parent
     *            parent control
     */
    private void createProjectEditor(Composite parent) {
        Group group = SWTFactory.createGroup(parent, "Project:", 2, 1, GridData.FILL_HORIZONTAL);
        projectTxt = SWTFactory.createSingleText(group, 1);
        projectTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                fileTxt.setEnabled(!projectTxt.getText().trim().isEmpty());
                fileBtn.setEnabled(!projectTxt.getText().trim().isEmpty());
                updateLaunchConfigurationDialog();
            }
        });
        projectBtn = createPushButton(group, "Browse...", null);
        projectBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider() {
                    public Image getImage(Object element) {
                        return PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT);
                    }

                    public String getText(Object element) {
                        return ((IProject) element).getName();
                    }
                });
                dialog.setTitle("Project Selection");
                dialog.setMessage("Select a TypeScript project");
                List<IProject> projects = new ArrayList<>();
                for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
                    try {
                        if (project.hasNature(TypescriptNature.NATURE_ID)) {
                            projects.add(project);
                        }
                    } catch (CoreException ex) {
                        // ignore exception
                    }
                }
                dialog.setElements((IProject[]) projects.toArray(new IProject[projects.size()]));
                if (dialog.open() == Window.OK) {
                    projectTxt.setText(((IProject) dialog.getFirstResult()).getName());
                }
            }
        });
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            projectTxt.setText(configuration.getAttribute(TypeScriptDebugConstants.TS_LAUNCH_STANDALONE_PROJECT, ""));
        } catch (CoreException e) {
            projectTxt.setText("");
        }

        try {
            fileTxt.setText(configuration.getAttribute(TypeScriptDebugConstants.TS_LAUNCH_STANDALONE_FILE, ""));
        } catch (CoreException e) {
            fileTxt.setText("");
        }
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(TypeScriptDebugConstants.TS_LAUNCH_STANDALONE_FILE, fileTxt.getText().trim());
        configuration.setAttribute(TypeScriptDebugConstants.TS_LAUNCH_STANDALONE_PROJECT, projectTxt.getText().trim());
    }

    @Override
    public String getName() {
        return "Main";
    }

    @Override
    public Image getImage() {
        return Activator.getImage(IMG_MAIN_TAB);
    }

    @Override
    public String getId() {
        return "com.axmor.eclipse.typescript.debug.ui.standaloneMainTab";
    }
}

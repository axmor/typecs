/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.ui.launching;

import static com.axmor.eclipse.typescript.debug.ui.DebugUIConstants.IMG_MAIN_TAB;

import java.util.List;

import org.chromium.sdk.wip.WipBackend;
import org.chromium.sdk.wip.eclipse.BackendRegistry;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.axmor.eclipse.typescript.core.ui.SWTFactory;
import com.axmor.eclipse.typescript.debug.launching.TypeScriptDebugConstants;
import com.axmor.eclipse.typescript.debug.ui.Activator;

/**
 * Main launcher tab for web remote mode.
 * 
 * @author Konstantin Zaitcev
 */
public class LaunchWebMainTab extends AbstractLaunchConfigurationTab {

    private Text hostTxt;
    private Spinner portNum;
    private Combo wipSelection;

    @Override
    public void createControl(Composite parent) {
        Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);
        ((GridLayout) comp.getLayout()).verticalSpacing = 0;
        createConnectionEditor(comp);
        createVerticalSpacer(comp, 1);
        createBackendEditor(comp);
        setControl(comp);
    }

    /**
     * Creates connection editor group.
     * 
     * @param parent
     *            parent control
     */
    private void createConnectionEditor(Composite parent) {
        Group group = SWTFactory.createGroup(parent, "Connection", 10, 1, GridData.FILL_HORIZONTAL);
        SWTFactory.createLabel(group, "Host: ", 1);
        hostTxt = SWTFactory.createSingleText(group, 7);
        hostTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });

        SWTFactory.createLabel(group, "Port: ", 1);
        portNum = SWTFactory.createSpinner(group, 1);
        portNum.setMaximum(Integer.MAX_VALUE);
        portNum.setMinimum(1024);
        portNum.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
    }

    /**
     * Creates WIP backend editor group.
     * 
     * @param parent
     *            parent control
     */
    private void createBackendEditor(Composite parent) {
        Group group = SWTFactory.createGroup(parent, "WIP Backend", 2, 1, GridData.FILL_HORIZONTAL);
        SWTFactory.createLabel(group, "WIP: ", 1);
        List<? extends WipBackend> backends = BackendRegistry.INSTANCE.getBackends();
        String[] items = new String[backends.size()];
        for (int i = 0; i < backends.size(); i++) {
            items[i] = backends.get(i).getId();
        }
        wipSelection = SWTFactory.createCombo(group, SWT.READ_ONLY, 1, items);
        wipSelection.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
    }


    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            hostTxt.setText(configuration.getAttribute(TypeScriptDebugConstants.TS_LAUNCH_WEB_HOST, "localhost"));
        } catch (CoreException e) {
            hostTxt.setText("localhost");
        }

        try {
            portNum.setSelection(configuration.getAttribute(TypeScriptDebugConstants.TS_LAUNCH_WEB_PORT, 9222));
        } catch (CoreException e) {
            portNum.setSelection(9222);
        }

        try {
            wipSelection.select(configuration.getAttribute(TypeScriptDebugConstants.TS_LAUNCH_WEB_PORT, 0));
        } catch (CoreException e) {
            wipSelection.select(0);;
        }
}

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(TypeScriptDebugConstants.TS_LAUNCH_WEB_HOST, hostTxt.getText().trim());
        configuration.setAttribute(TypeScriptDebugConstants.TS_LAUNCH_WEB_PORT, portNum.getSelection());
        configuration.setAttribute(TypeScriptDebugConstants.TS_LAUNCH_WEB_WIP, wipSelection.getSelectionIndex());
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
        return "com.axmor.eclipse.typescript.debug.ui.webMainTab";
    }
}

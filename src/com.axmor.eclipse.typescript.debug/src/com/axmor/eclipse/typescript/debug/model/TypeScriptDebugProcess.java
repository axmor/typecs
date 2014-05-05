/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.debug.model;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.internal.core.commands.CommandAdapterFactory;
import org.eclipse.debug.internal.core.commands.TerminateCommand;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptDebugProcess implements IProcess {

    private ILaunch launch;
    private boolean terminated;
    private IDebugTarget debugTarget;
    private IProgressMonitor monitor;
    private TypeScriptStreamProxy typeScriptStreamProxy;

    public TypeScriptDebugProcess(ILaunch launch, IProgressMonitor monitor) throws CoreException {
        this.launch = launch;
        this.monitor = monitor;
        this.typeScriptStreamProxy = new TypeScriptStreamProxy();
        this.debugTarget = new TypeScriptDebugTarget(this);
        this.launch.addProcess(this);
        terminated = false;
        fireEvent(new DebugEvent(this, DebugEvent.CREATE));
    }
    
    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        System.out.println("getAdapter: " + adapter);
        if (adapter.equals(IProcess.class)) {
            return this;
        }
        if (adapter.equals(IDebugTarget.class)) {
            return debugTarget;
        }
        if (adapter.equals(ITerminateHandler.class)) {
            return null;
        }
        return null;
    }

    @Override
    public boolean canTerminate() {
        System.out.println("canTerminate");
        return !terminated;
    }

    @Override
    public boolean isTerminated() {
        return terminated;
    }

    @Override
    public void terminate() throws DebugException {
        System.out.println("terminate");
        terminated = true;
        fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
    }

    @Override
    public String getLabel() {
        System.out.println("get label");
        return "asdas";
    }

    @Override
    public ILaunch getLaunch() {
        return launch;
    }

    @Override
    public IStreamsProxy getStreamsProxy() {
        System.out.println("getStreamProxy");
        return typeScriptStreamProxy;
    }

    @Override
    public void setAttribute(String key, String value) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getAttribute(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getExitValue() throws DebugException {
        System.out.println("getExitValue");
        return 0;
    }

    /**
     * Fires the given debug event.
     * 
     * @param event debug event to fire
     */
    protected void fireEvent(DebugEvent event) {
        DebugPlugin manager= DebugPlugin.getDefault();
        if (manager != null) {
            manager.fireDebugEventSet(new DebugEvent[]{event});
        }
    }

}

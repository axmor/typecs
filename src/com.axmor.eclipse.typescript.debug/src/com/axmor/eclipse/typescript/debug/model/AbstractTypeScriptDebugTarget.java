/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.model;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;

/**
 * @author Konstantin Zaitcev
 * 
 */
public abstract class AbstractTypeScriptDebugTarget extends TypeScriptDebugElement implements IDebugTarget {
    private IProcess process;
    private ILaunch launch;
    private IThread[] threads;

    /**
     * @param target
     * @param launch
     * @param process
     */
    public AbstractTypeScriptDebugTarget(IProcess process, ILaunch launch) {
        super(null);
        this.process = process;
        this.launch = launch;
    }

    @Override
    public IDebugTarget getDebugTarget() {
        return this;
    }

    @Override
    public ILaunch getLaunch() {
        return launch;
    }

    @Override
    public IProcess getProcess() {
        return process;
    }

    @Override
    public boolean canTerminate() {
        return process.canTerminate();
    }

    @Override
    public boolean isTerminated() {
        return process.isTerminated();
    }

    @Override
    public void terminate() throws DebugException {
        process.terminate();
    }

    @Override
    public String getName() throws DebugException {
        return "TypeScript Application";
    }

    @Override
    public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
        return null;
    }

    @Override
    public boolean canDisconnect() {
        return false;
    }

    @Override
    public void disconnect() throws DebugException {
    }

    @Override
    public boolean isDisconnected() {
        return false;
    }

    @Override
    public boolean supportsStorageRetrieval() {
        return false;
    }

    @Override
    public boolean hasThreads() throws DebugException {
        return true;
    }

    @Override
    public boolean canResume() {
        return !isTerminated() && isSuspended();
    }

    @Override
    public boolean canSuspend() {
        return !isTerminated() && !isSuspended();
    }
    
    @Override
    public IThread[] getThreads() throws DebugException {
        return threads;
    }
    
    /**
     * @param threads the threads to set
     */
    public void setThreads(IThread[] threads) {
        this.threads = threads;
    }
    
    /**
     * Fires a debug event
     * 
     * @param event the event to be fired
     */
    protected void fireEvent(DebugEvent event) {
        DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {event});
    }
    
    /**
     * Fires a <code>CREATE</code> event for this element.
     */
    protected void fireCreationEvent() {
        fireEvent(new DebugEvent(this, DebugEvent.CREATE));
    }   
    
    /**
     * Fires a <code>RESUME</code> event for this element with
     * the given detail.
     * 
     * @param detail event detail code
     */
    public void fireResumeEvent(int detail) {
        fireEvent(new DebugEvent(this, DebugEvent.RESUME, detail));
    }

    /**
     * Fires a <code>SUSPEND</code> event for this element with
     * the given detail.
     * 
     * @param detail event detail code
     */
    public void fireSuspendEvent(int detail) {
        fireEvent(new DebugEvent(this, DebugEvent.SUSPEND, detail));
    }
    
    /**
     * Fires a <code>TERMINATE</code> event for this element.
     */
    protected void fireTerminateEvent() {
        fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
    }   
}

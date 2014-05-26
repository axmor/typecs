/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.model;

import org.chromium.sdk.SyncCallback;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

/**
 * @author Konstantin Zaitcev
 * 
 */
public class TypeScriptDebugThread extends TypeScriptDebugElement implements IThread {

    private boolean stepping;
    private IBreakpoint[] breakpoints;

    public TypeScriptDebugThread(IDebugTarget debugTarget) {
        super(debugTarget);
    }

    @Override
    public boolean canResume() {
        return isSuspended();
    }

    @Override
    public boolean canSuspend() {
        return !isSuspended();
    }

    @Override
    public boolean isSuspended() {
        return getDebugTarget().isSuspended();
    }

    @Override
    public void resume() throws DebugException {
        getDebugTarget().resume();
    }

    @Override
    public void suspend() throws DebugException {
        getDebugTarget().suspend();
    }

    @Override
    public boolean canStepInto() {
        return isSuspended();
    }

    @Override
    public boolean canStepOver() {
        return isSuspended();
    }

    @Override
    public boolean canStepReturn() {
        return isSuspended();
    }

    @Override
    public boolean isStepping() {
        return stepping;
    }

    @Override
    public void stepInto() throws DebugException {
    	stepInto(null);
    }

    public void stepInto(SyncCallback callback) throws DebugException {
        ((TypeScriptDebugTarget) getDebugTarget()).stepIn(callback);
    }

    @Override
    public void stepOver() throws DebugException {
    	stepOver(null);
    }

    public void stepOver(SyncCallback callback) throws DebugException {
        ((TypeScriptDebugTarget) getDebugTarget()).stepOver(callback);
    }

    @Override
    public void stepReturn() throws DebugException {
    	stepReturn(null);
    }

    public void stepReturn(SyncCallback callback) throws DebugException {
        ((TypeScriptDebugTarget) getDebugTarget()).stepOut(callback);
    }

    @Override
    public boolean canTerminate() {
        return getDebugTarget().canTerminate();
    }

    @Override
    public boolean isTerminated() {
        return getDebugTarget().isTerminated();
    }

    @Override
    public void terminate() throws DebugException {
        getDebugTarget().terminate();
    }

    @Override
    public IStackFrame[] getStackFrames() throws DebugException {
        return ((TypeScriptDebugTarget) getDebugTarget()).getStackFrames();
    }

    @Override
    public boolean hasStackFrames() throws DebugException {
        return isSuspended();
    }

    @Override
    public int getPriority() throws DebugException {
        return 0;
    }

    @Override
    public IStackFrame getTopStackFrame() throws DebugException {
        IStackFrame[] frames = getStackFrames();
        if (frames != null && frames.length > 0) {
            return frames[0];
        }
        return null;
    }

    @Override
    public String getName() throws DebugException {
        return "Main Thread";
    }

    @Override
    public IBreakpoint[] getBreakpoints() {
        if (breakpoints != null) {
            return breakpoints;
        }
        return new IBreakpoint[0];
    }
    
    /**
     * @param breakpoints the breakpoints to set
     */
    public void setBreakpoints(IBreakpoint[] breakpoints) {
        this.breakpoints = breakpoints;
    }
    
    /**
     * @param stepping the stepping to set
     */
    public void setStepping(boolean stepping) {
        this.stepping = stepping;
    }
}

/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.debug.model;

import org.chromium.sdk.CallFrame;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

/**
 * @author Konstantin Zaitcev
 *
 */
public class TypeScriptStackFrame implements IStackFrame {

    private CallFrame cframe;
    private IThread thread;

    /**
     * @param cframe
     */
    public TypeScriptStackFrame(IThread thread, CallFrame cframe) {
        this.thread = thread;
        this.cframe = cframe;
    }

    @Override
    public String getModelIdentifier() {
        return "com.axmor.eclipse.typescript.debug.thread.frame";
    }

    @Override
    public IDebugTarget getDebugTarget() {
        return thread.getDebugTarget();
    }

    @Override
    public ILaunch getLaunch() {
        return thread.getLaunch();
    }

    @Override
    public Object getAdapter(Class adapter) {
        return null;
    }

    @Override
    public boolean canStepInto() {
        return false;
    }

    @Override
    public boolean canStepOver() {
        return false;
    }

    @Override
    public boolean canStepReturn() {
        return false;
    }

    @Override
    public boolean isStepping() {
        return false;
    }

    @Override
    public void stepInto() throws DebugException {
    }

    @Override
    public void stepOver() throws DebugException {
    }

    @Override
    public void stepReturn() throws DebugException {
    }

    @Override
    public boolean canResume() {
        return false;
    }

    @Override
    public boolean canSuspend() {
        return false;
    }

    @Override
    public boolean isSuspended() {
        return false;
    }

    @Override
    public void resume() throws DebugException {
    }

    @Override
    public void suspend() throws DebugException {
    }

    @Override
    public boolean canTerminate() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public void terminate() throws DebugException {
    }

    @Override
    public IThread getThread() {
        return thread;
    }

    @Override
    public IVariable[] getVariables() throws DebugException {
        return new IVariable[0];
    }

    @Override
    public boolean hasVariables() throws DebugException {
        return false;
    }

    @Override
    public int getLineNumber() throws DebugException {
        return 0;
    }

    @Override
    public int getCharStart() throws DebugException {
        return 0;
    }

    @Override
    public int getCharEnd() throws DebugException {
        return 0;
    }

    @Override
    public String getName() throws DebugException {
        return cframe.getFunctionName();
    }

    @Override
    public IRegisterGroup[] getRegisterGroups() throws DebugException {
        return null;
    }

    @Override
    public boolean hasRegisterGroups() throws DebugException {
        return false;
    }

}

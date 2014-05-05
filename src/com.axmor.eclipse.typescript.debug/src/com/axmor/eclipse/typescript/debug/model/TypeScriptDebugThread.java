/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.debug.model;

import java.util.ArrayList;

import org.chromium.sdk.CallFrame;
import org.chromium.sdk.DebugContext;
import org.chromium.sdk.DebugContext.StepAction;
import org.chromium.sdk.DebugEventListener;
import org.chromium.sdk.JavascriptVm;
import org.chromium.sdk.Script;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

/**
 * @author Konstantin Zaitcev
 *
 */
public class TypeScriptDebugThread implements IThread, DebugEventListener {

    private IDebugTarget debugTarget;
    private DebugContext ctx = null;
    private JavascriptVm jsVm;

    public TypeScriptDebugThread(IDebugTarget debugTarget, JavascriptVm jsVm) {
        this.debugTarget = debugTarget;
        this.jsVm = jsVm;
    }

    @Override
    public String getModelIdentifier() {
        return "com.axmor.eclipse.typescript.debug.thread";
    }

    @Override
    public IDebugTarget getDebugTarget() {
        return debugTarget;
    }

    @Override
    public ILaunch getLaunch() {
        return debugTarget.getLaunch();
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        System.out.println("thread get adapter: " + adapter);
        return null;
    }

    @Override
    public boolean canResume() {
        return ctx != null;
    }

    @Override
    public boolean canSuspend() {
        return ctx == null;
    }

    @Override
    public boolean isSuspended() {
        return ctx != null;
    }

    @Override
    public void resume() throws DebugException {
        ctx.continueVm(StepAction.CONTINUE, 0, null, null);
        ctx = null;
    }

    @Override
    public void suspend() throws DebugException {
        jsVm.suspend(null);
    }

    @Override
    public boolean canStepInto() {
        return ctx != null;
    }

    @Override
    public boolean canStepOver() {
        return ctx != null;
    }

    @Override
    public boolean canStepReturn() {
        return ctx != null;
    }

    @Override
    public boolean isStepping() {
        return ctx != null;
    }

    @Override
    public void stepInto() throws DebugException {
        // TODO Auto-generated method stub
    }

    @Override
    public void stepOver() throws DebugException {
        // TODO Auto-generated method stub
    }

    @Override
    public void stepReturn() throws DebugException {
        // TODO Auto-generated method stub
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
    public IStackFrame[] getStackFrames() throws DebugException {
        ArrayList<IStackFrame> frames = new ArrayList<>();
        for (CallFrame cframe: ctx.getCallFrames()) {
            frames.add(new TypeScriptStackFrame(this, cframe));
        }
        return (IStackFrame[]) frames.toArray(new IStackFrame[frames.size()]);
    }

    @Override
    public boolean hasStackFrames() throws DebugException {
        return ctx != null;
    }

    @Override
    public int getPriority() throws DebugException {
        return 0;
    }

    @Override
    public IStackFrame getTopStackFrame() throws DebugException {
        return null;
    }

    @Override
    public String getName() throws DebugException {
        return "TypeScript V8 Thread";
    }

    @Override
    public IBreakpoint[] getBreakpoints() {
        return null;
    }

    @Override
    public void suspended(DebugContext ctx) {
        this.ctx = ctx;
        System.out.println("vm - suspended: " + ctx);
    }

    @Override
    public void disconnected() {
        System.out.println("vm - disconnected");
    }

    @Override
    public VmStatusListener getVmStatusListener() {
        System.out.println("vm - getVmStatusListener");
        return null;
    }

    @Override
    public void resumed() {
        System.out.println("vm - resumed");
    }

    @Override
    public void scriptCollected(Script script) {
        System.out.println("vm - scriptCollected: " + script);
    }

    @Override
    public void scriptContentChanged(Script script) {
        System.out.println("vm - scriptContentChanges: " + script);
    }

    @Override
    public void scriptLoaded(Script script) {
        System.out.println("vm - scriptLoaded: " + script);
    }

}

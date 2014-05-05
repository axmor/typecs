/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.model;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.chromium.sdk.BrowserFactory;
import org.chromium.sdk.DebugContext;
import org.chromium.sdk.DebugEventListener;
import org.chromium.sdk.JavascriptVm;
import org.chromium.sdk.Script;
import org.chromium.sdk.StandaloneVm;
import org.chromium.sdk.UnsupportedVersionException;
import org.chromium.sdk.DebugContext.StepAction;
import org.chromium.sdk.util.MethodIsBlockingException;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;

import com.axmor.eclipse.typescript.debug.Activator;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptDebugTarget extends TypeScriptDebugElement implements IDebugTarget {

    private boolean terminated;
    private IProcess process;
    private JavascriptVm javascriptVm;
    private TypeScriptDebugThread thread;

    public TypeScriptDebugTarget(IProcess process) throws CoreException {
        this.process = process;
        StandaloneVm standaloneVm = BrowserFactory.getInstance().createStandalone(
                new InetSocketAddress("localhost", 9222), null);
        this.javascriptVm = standaloneVm;
        this.thread = new TypeScriptDebugThread(this, javascriptVm);
        try {
            standaloneVm.attach(this.thread);
        } catch (Exception e) {
            throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
        }
    }

    @Override
    public boolean canTerminate() {
        System.out.println("d: canterminate");
        return !terminated;
    }

    @Override
    public boolean isTerminated() {
        System.out.println("d: isterminated");

        return terminated;
    }

    @Override
    public void terminate() throws DebugException {
        System.out.println("d: terminate");
        terminated = true;
        process.terminate();
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
    public void breakpointAdded(IBreakpoint breakpoint) {
        System.out.println("breakpoint added" + breakpoint);
    }

    @Override
    public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
    }

    @Override
    public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
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
    public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
        System.out.println("get memory block");
        return null;
    }

    @Override
    public IProcess getProcess() {
        return process;
    }

    @Override
    public IThread[] getThreads() throws DebugException {
        return new IThread[] {thread};
    }

    @Override
    public boolean hasThreads() throws DebugException {
        return true;
    }

    @Override
    public String getName() throws DebugException {
        return "V8";
    }

    @Override
    public boolean supportsBreakpoint(IBreakpoint breakpoint) {
        return true;
    }

    @Override
    public IDebugTarget getDebugTarget() {
        return this;
    }

    @Override
    public ILaunch getLaunch() {
        return process.getLaunch();
    }
}

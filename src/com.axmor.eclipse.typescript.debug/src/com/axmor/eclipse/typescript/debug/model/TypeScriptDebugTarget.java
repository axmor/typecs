/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.model;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;

import org.chromium.sdk.Breakpoint;
import org.chromium.sdk.Breakpoint.Target;
import org.chromium.sdk.Breakpoint.Target.ScriptName;
import org.chromium.sdk.Breakpoint.Target.Visitor;
import org.chromium.sdk.BrowserFactory;
import org.chromium.sdk.CallFrame;
import org.chromium.sdk.DebugContext;
import org.chromium.sdk.DebugContext.StepAction;
import org.chromium.sdk.DebugEventListener;
import org.chromium.sdk.JavascriptVm;
import org.chromium.sdk.Script;
import org.chromium.sdk.StandaloneVm;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

import com.axmor.eclipse.typescript.debug.launching.TypeScriptDebugConstants;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptDebugTarget extends AbstractTypeScriptDebugTarget implements DebugEventListener {

    private JavascriptVm vm;
    private TypeScriptDebugThread thread;
    private boolean suspended;
    private DebugContext ctx;

    public TypeScriptDebugTarget(ILaunch launch, IProcess process, int port) throws CoreException {
        super(process, launch);
        StandaloneVm standaloneVm = BrowserFactory.getInstance().createStandalone(
                new InetSocketAddress("localhost", port), null);
        this.vm = standaloneVm;
        this.thread = new TypeScriptDebugThread(this);
        this.setThreads(new IThread[] { this.thread });

        try {
            standaloneVm.attach(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        started();
        DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
    }

    @Override
    public boolean isSuspended() {
        return suspended;
    }

    @Override
    public void resume() throws DebugException {
        if (isSuspended() && ctx != null) {
            ctx.continueVm(StepAction.CONTINUE, 0, null, null);
        }
    }

    @Override
    public void suspend() throws DebugException {
        vm.suspend(null);
    }

    @Override
    public void breakpointAdded(IBreakpoint breakpoint) {
        System.out.println("breakpoint added" + breakpoint);
        if (supportsBreakpoint(breakpoint)) {
            TypeScriptLineBreakpoint lineBreakpoint = (TypeScriptLineBreakpoint) breakpoint;
            //Target t = new Tar
            try {
                ScriptName target = new ScriptName("d:\\Programs\\eclipse-4.3.1\\runtime-ts\\warship_sample\\web\\NodeApp.js");
                vm.setBreakpoint(target, lineBreakpoint.getLineNumber(), Breakpoint.EMPTY_VALUE, true, null, null, null);
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
    }

    @Override
    public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
    }

    @Override
    public boolean supportsBreakpoint(IBreakpoint breakpoint) {
        return true;
    }

    /**
     * 
     */
    public void step() {
        if (isSuspended() && ctx != null) {
            ctx.continueVm(StepAction.OVER, 0, null, null);
        }
    }

    /**
     * @return
     */
    public IStackFrame[] getStackFrames() {
        if (isSuspended() && ctx != null) {
            ArrayList<IStackFrame> frames = new ArrayList<>();
            for (CallFrame cframe : ctx.getCallFrames()) {
                frames.add(new TypeScriptStackFrame(thread, cframe));
            }
            return (IStackFrame[]) frames.toArray(new IStackFrame[frames.size()]);
        }
        return null;
    }

    // / Debug Event listener methods

    @Override
    public void disconnected() {
        suspended = false;
        DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
        fireTerminateEvent();
    }

    @Override
    public VmStatusListener getVmStatusListener() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void resumed() {
        resumedEvent(DebugEvent.STEP_OVER);
        thread.setBreakpoints(null);
    }

    @Override
    public void scriptCollected(Script script) {
        System.out.println("scriptCollected: " + script.getName());
    }

    @Override
    public void scriptContentChanged(Script script) {
        System.out.println("scriptContentChanged: " + script.getName());
    }

    @Override
    public void scriptLoaded(Script script) {
        System.out.println("scriptLoaded: " + script.getName());
    }

    @Override
    public void suspended(DebugContext ctx) {
        this.ctx = ctx;
        Collection<? extends Breakpoint> hits = ctx.getBreakpointsHit();
        if (hits.size() > 0) {
            for (Breakpoint hit : hits) {
                String name = hit.getTarget().accept(new ScriptNameVisitor());
                IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager()
                        .getBreakpoints(TypeScriptDebugConstants.TS_DEBUG_MODEL);
                for (IBreakpoint breakpoint : breakpoints) {
                    try {
                        if (breakpoint.isEnabled() && ((ILineBreakpoint) breakpoint).getLineNumber() == hit.getLineNumber()) {
                            thread.setBreakpoints(new IBreakpoint[] {breakpoint});
                        }
                    } catch (CoreException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(hit.getLineNumber());
            }
            suspendedEvent(DebugEvent.BREAKPOINT);
        } else {
            suspendedEvent(DebugEvent.STEP_END);
        }
        suspended = true;
    }

    // / Event notification methods

    private void started() {
        fireCreationEvent();
        IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager()
                .getBreakpoints(TypeScriptDebugConstants.TS_DEBUG_MODEL);
        for (IBreakpoint breakpoint : breakpoints) {
            breakpointAdded(breakpoint);
        }
//        try {
//            resume();
//        } catch (DebugException e) {
//            // ignore exception
//        }
    }

    private void resumedEvent(int detail) {
        this.ctx = null;
        suspended = false;
        fireResumeEvent(detail);
    }

    private void suspendedEvent(int detail) {
        suspended = true;
        fireSuspendEvent(detail);
    }
    
    private class ScriptNameVisitor implements Visitor<String> {

        @Override
        public String visitScriptName(String scriptName) {
            return scriptName;
        }

        @Override
        public String visitScriptId(Object scriptId) {
            return null;
        }

        @Override
        public String visitUnknown(Target target) {
            return null;
        }
    }
}

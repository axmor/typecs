/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.debug;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.core.NullStreamsProxy;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptDebugProcess implements IProcess {

    private ILaunch launch;
    private boolean terminated;

    public TypeScriptDebugProcess(ILaunch launch) {
        this.launch = launch;
        this.launch.addProcess(this);
        terminated = false;
    }
    
    @Override
    public Object getAdapter(Class adapter) {
        System.out.println("getAdapter: " + adapter);
        if (adapter.equals(IDebugTarget.class)) {
            return new IDebugTarget() {
                
                @Override
                public boolean supportsStorageRetrieval() {
                    // TODO Auto-generated method stub
                    return false;
                }
                
                @Override
                public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
                    // TODO Auto-generated method stub
                    return null;
                }
                
                @Override
                public boolean isDisconnected() {
                    // TODO Auto-generated method stub
                    return false;
                }
                
                @Override
                public void disconnect() throws DebugException {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public boolean canDisconnect() {
                    // TODO Auto-generated method stub
                    return false;
                }
                
                @Override
                public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void breakpointAdded(IBreakpoint breakpoint) {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void suspend() throws DebugException {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void resume() throws DebugException {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public boolean isSuspended() {
                    // TODO Auto-generated method stub
                    return false;
                }
                
                @Override
                public boolean canSuspend() {
                    // TODO Auto-generated method stub
                    return false;
                }
                
                @Override
                public boolean canResume() {
                    // TODO Auto-generated method stub
                    return false;
                }
                
                @Override
                public void terminate() throws DebugException {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public boolean isTerminated() {
                    // TODO Auto-generated method stub
                    return false;
                }
                
                @Override
                public boolean canTerminate() {
                    // TODO Auto-generated method stub
                    return false;
                }
                
                @Override
                public Object getAdapter(Class adapter) {
                    // TODO Auto-generated method stub
                    return null;
                }
                
                @Override
                public String getModelIdentifier() {
                    // TODO Auto-generated method stub
                    return null;
                }
                
                @Override
                public ILaunch getLaunch() {
                    return launch;
                }
                
                @Override
                public IDebugTarget getDebugTarget() {
                    return null;
                }
                
                @Override
                public boolean supportsBreakpoint(IBreakpoint breakpoint) {
                    // TODO Auto-generated method stub
                    return false;
                }
                
                @Override
                public boolean hasThreads() throws DebugException {
                    // TODO Auto-generated method stub
                    return false;
                }
                
                @Override
                public IThread[] getThreads() throws DebugException {
                    // TODO Auto-generated method stub
                    return null;
                }
                
                @Override
                public IProcess getProcess() {
                    // TODO Auto-generated method stub
                    return null;
                }
                
                @Override
                public String getName() throws DebugException {
                    // TODO Auto-generated method stub
                    return null;
                }
            };
        }
        return null;
    }

    @Override
    public boolean canTerminate() {
        System.out.println("canTerminate");
        return true;
    }

    @Override
    public boolean isTerminated() {
        System.out.println("isTerminated");
        return terminated;
    }

    @Override
    public void terminate() throws DebugException {
        System.out.println("terminate");
        terminated = true;
    }

    @Override
    public String getLabel() {
        return "asdas";
    }

    @Override
    public ILaunch getLaunch() {
        return launch;
    }

    @Override
    public IStreamsProxy getStreamsProxy() {
        System.out.println("getStreamProxy");
        return new TypeScriptStreamProxy();
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

}

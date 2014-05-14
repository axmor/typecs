/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.model;

import java.io.File;
import java.util.Map;

import org.chromium.sdk.CallFrame;
import org.chromium.sdk.Script;
import org.chromium.sdk.internal.ScriptBase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

import com.axmor.eclipse.typescript.debug.sourcemap.SourceMap;
import com.axmor.eclipse.typescript.debug.sourcemap.SourceMapItem;

/**
 * @author Konstantin Zaitcev
 * 
 */
public class TypeScriptStackFrame extends TypeScriptDebugElement implements IStackFrame {

    private String sourceName;
    private int lineNumber;
    private CallFrame cframe;
    private IThread thread;
    private Map<String, SourceMap> jsMappings;

    /**
     * @param cframe
     * @param jsMappings 
     */
    public TypeScriptStackFrame(IThread thread, CallFrame cframe, Map<String, SourceMap> jsMappings) {
        super(thread.getDebugTarget());
        this.thread = thread;
        this.cframe = cframe;
        this.jsMappings = jsMappings;
        initSourceNameAndLine();
    }

    /**
     * @return
     */
    private void initSourceNameAndLine() {
        Script script = cframe.getScript();
        String name = ((ScriptBase<?>) script).getName();
        this.sourceName = name;
        this.lineNumber = cframe.getStatementStartPosition().getLine();
        
        File file = new File(name);
        if (file.exists()) {
            if (jsMappings.containsKey(file.getPath())) {
                SourceMapItem item = jsMappings.get(file.getPath()).getItemByJSLine(lineNumber);
                if (item != null) {
                    this.sourceName = item.getTsFile();
                    this.lineNumber = item.getTsLine();
                }
            } else {
                IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(Path.fromOSString(file.getPath()));
                if (ifile != null && file.exists()) {
                    this.sourceName = ifile.getFullPath().toString();
                }
            }
        }
    }

    @Override
    public boolean canStepInto() {
        return getThread().canStepInto();
    }

    @Override
    public boolean canStepOver() {
        return getThread().canStepOver();
    }

    @Override
    public boolean canStepReturn() {
        return getThread().canStepReturn();
    }

    @Override
    public boolean isStepping() {
        return getThread().isStepping();
    }

    @Override
    public void stepInto() throws DebugException {
        getThread().stepInto();
    }

    @Override
    public void stepOver() throws DebugException {
        getThread().stepOver();
    }

    @Override
    public void stepReturn() throws DebugException {
        getThread().stepReturn();
    }

    @Override
    public boolean canResume() {
        return getThread().canResume();
    }

    @Override
    public boolean canSuspend() {
        return getThread().canSuspend();
    }

    @Override
    public boolean isSuspended() {
        return getThread().isSuspended();
    }

    @Override
    public void resume() throws DebugException {
        getThread().resume();
    }

    @Override
    public void suspend() throws DebugException {
        getThread().suspend();
    }

    @Override
    public boolean canTerminate() {
        return getThread().canTerminate();
    }

    @Override
    public boolean isTerminated() {
        return getThread().isTerminated();
    }

    @Override
    public void terminate() throws DebugException {
        getThread().terminate();
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
        return lineNumber;
    }

    @Override
    public int getCharStart() throws DebugException {
        return -1;
    }

    @Override
    public int getCharEnd() throws DebugException {
        return -1;
    }

    @Override
    public String getName() throws DebugException {
        return cframe.getFunctionName() + "() line: " + getLineNumber();
    }

    @Override
    public IRegisterGroup[] getRegisterGroups() throws DebugException {
        return null;
    }

    @Override
    public boolean hasRegisterGroups() throws DebugException {
        return false;
    }

    /**
     * @return the cframe
     */
    public CallFrame getCframe() {
        return cframe;
    }

    public String getSourceName() {
        return sourceName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TypeScriptStackFrame) {
            TypeScriptStackFrame sf = (TypeScriptStackFrame) obj;
            try {
                return sf.getSourceName().equals(getSourceName()) && sf.getLineNumber() == getLineNumber();
            } catch (DebugException e) {
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        try {
            return getSourceName().hashCode() + getLineNumber();
        } catch (DebugException e) {
            return getSourceName().hashCode();
        }
    }
}

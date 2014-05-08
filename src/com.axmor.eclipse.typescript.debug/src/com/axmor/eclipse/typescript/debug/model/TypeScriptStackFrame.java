/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.model;

import java.io.File;

import org.chromium.sdk.CallFrame;
import org.chromium.sdk.Script;
import org.chromium.sdk.internal.ScriptBase;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

/**
 * @author Konstantin Zaitcev
 * 
 */
public class TypeScriptStackFrame extends TypeScriptDebugElement implements IStackFrame {

    private CallFrame cframe;
    private IThread thread;

    /**
     * @param cframe
     */
    public TypeScriptStackFrame(IThread thread, CallFrame cframe) {
        super(thread.getDebugTarget());
        this.thread = thread;
        this.cframe = cframe;
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
        return cframe.getStatementStartPosition().getLine();
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
        Script script = cframe.getScript();
        if (script instanceof ScriptBase) {
            String name = ((ScriptBase<?>) script).getName();
            String prefix = "d:\\Programs\\eclipse-4.3.1\\runtime-ts\\warship_sample\\";
            if (name.startsWith(prefix)) {
                System.out.println(name);
                if (script.getSource().contains("//# sourceMappingURL=")) {
                    String source = script.getSource();
                    int idx = source.indexOf("//# sourceMappingURL=") + "//# sourceMappingURL=".length();
                    String mapFile = source.substring(idx, source.indexOf(".map", idx) + ".map".length());
                    System.out.println(mapFile);
                    new File(new File(name).getParentFile(), mapFile);
                }
                return name.substring(prefix.length()).replaceAll("\\\\", "/").replaceAll(".js", ".ts");
            }
            return name;
        }
        return script.getName();
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

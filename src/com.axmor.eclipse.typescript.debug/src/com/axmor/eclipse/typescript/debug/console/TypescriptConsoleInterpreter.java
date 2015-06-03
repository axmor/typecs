package com.axmor.eclipse.typescript.debug.console;

import org.chromium.debug.core.util.JsValueStringifier;
import org.chromium.sdk.JsEvaluateContext;
import org.chromium.sdk.JsVariable;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.axmor.eclipse.typescript.debug.console.TypeScriptConsoleDocumentListener.Callback;
import com.axmor.eclipse.typescript.debug.model.TypeScriptDebugTarget;
import com.axmor.eclipse.typescript.debug.model.TypeScriptStackFrame;

public class TypescriptConsoleInterpreter implements IDebugContextListener {

    private TypeScriptStackFrame frame;
    protected Callback<Object, String> onResponseReceived;
    private TypeScriptDebugTarget debugTarget;

    public void close() {

    }

    public TypeScriptStackFrame getFrame() {
        return this.frame;
    }

    public void exec(String userInput, Callback<Object, String> onContentsReceived) {
        onResponseReceived = onContentsReceived;
        if (frame != null) {
            final JsEvaluateContext evaluateContext = frame.getCframe().getEvaluateContext();            
            try {
                evaluateContext.evaluateSync(userInput, null, new JsEvaluateContext.EvaluateCallback() {
                    @Override
                    public void success(JsVariable resp) {
                        String value = JsValueStringifier.toVisibleString(resp.getValue());
                        onResponseReceived.call(value);
                    }

                    @Override
                    public void failure(String error) {
                        onResponseReceived.call(error);
                    }

                });
            } catch (Exception e) {
                onResponseReceived.call("undefined");
            }
        }
    }

    public void setFrame(IStackFrame iStackFrame) {
        this.frame = (TypeScriptStackFrame) iStackFrame;
    }

    @Override
    public void debugContextChanged(DebugContextEvent event) {
        if (event.getFlags() == DebugContextEvent.ACTIVATED) {
            updateContext(getDebugContextElementForSelection(event.getContext()));
        }
    }

    private void updateContext(IAdaptable context) {
        if (context != frame && context instanceof TypeScriptStackFrame) {
            TypeScriptStackFrame stackFrame = (TypeScriptStackFrame) context;
            if (!stackFrame.isTerminated() && stackFrame.isSuspended()) {
                frame = stackFrame;
            }
        }
    }

    private static IAdaptable getDebugContextElementForSelection(ISelection activeContext) {
        if (activeContext instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) activeContext;
            if (!selection.isEmpty()) {
                Object firstElement = selection.getFirstElement();
                if (firstElement instanceof IAdaptable) {
                    return (IAdaptable) firstElement;
                }
            }
        }
        return null;
    }

    public void setTarget(TypeScriptDebugTarget debugTarget) {
        this.debugTarget = debugTarget;
    }

    public TypeScriptDebugTarget getTarget() {
        return this.debugTarget;
    }
}

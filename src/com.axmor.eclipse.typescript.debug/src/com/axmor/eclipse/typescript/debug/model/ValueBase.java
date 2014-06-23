// Copyright (c) 2011 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.axmor.eclipse.typescript.debug.model;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import org.chromium.debug.core.ChromiumDebugPlugin;
import org.chromium.sdk.JsEvaluateContext;
import org.chromium.sdk.JsScope;
import org.chromium.sdk.JsValue;
import org.chromium.sdk.JsVariable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

import com.axmor.eclipse.typescript.debug.model.Variable.Real.HostObject;

/**
 * A base implementation of all Chromium values. This class merely adds new interface,
 * not behavior. The actual class may be based on {@link JsValue} (regular case) or other data
 * sources.
 */
public abstract class ValueBase extends TypeScriptDebugElement implements IValue {
  /**
   * Downcasts IValue to ValueBase if possible or returns null. This should be used in context
   * where IValue is available, but it's unknown whether it's Chromium value or not.
   * Clients should use this method rather that do instanceof themselves, because the latter is
   * unsafe technique (you cannot track manual downcasts and consequently cannot refactor/manage
   * them).
   */
  public static ValueBase cast(IValue value) {
    if (value instanceof ValueBase) {
      return (ValueBase) value;
    } else {
      return null;
    }
  }

  private JsEvaluateContext evaluateContext;

  public JsEvaluateContext getEvaluateContext() {
	return evaluateContext;
  }

  protected ValueBase(JsEvaluateContext evaluateContext, IDebugTarget debugTarget) {
	  super(debugTarget);
    this.evaluateContext = evaluateContext;
  }

  /**
   * Downcasts to Value or return null.
   */
  public abstract Value asRealValue();

  public abstract String getValueString();


/**
   * Represents a value as {@link HostObject}. This will be passed to value subproperties.
   */
  interface ValueAsHostObject extends Variable.Real.HostObject {
  }

  /**
   * A base implementation of Value that lazily calculates its inner variables.
   */
  public static abstract class ValueWithLazyVariables extends ValueBase {
    static final IVariable[] EMPTY_VARIABLES = new IVariable[0];

    private final AtomicReference<IVariable[]> variablesRef =
        new AtomicReference<IVariable[]>(null);

    protected ValueWithLazyVariables(JsEvaluateContext evaluateContext, IDebugTarget debugTarget) {
      super(evaluateContext, debugTarget);
    }

    // This method could be blocking -- it gets called from a Worker thread.
    // All data should be prepared here.
    public IVariable[] getVariables() throws DebugException {
      try {
        // TODO: support clearing with cache clear.
        IVariable[] result = variablesRef.get();
        if (result != null) {
          return result;
        }
        IVariable[] variables = calculateVariables();
        variablesRef.compareAndSet(null, variables);
        return variablesRef.get();
      } catch (RuntimeException e) {
        // Log it, because Eclipse is likely to ignore it.
        ChromiumDebugPlugin.log(e);
        // We shouldn't throw RuntimeException from here, because calling
        // ElementContentProvider#update will forget to call update.done().
        throw new DebugException(new Status(IStatus.ERROR, ChromiumDebugPlugin.PLUGIN_ID,
            "Failed to read variables", e)); //$NON-NLS-1$
      }
    }

    // Consider making it more lazy for IIndexedValue implementation's benefit.
    protected abstract IVariable[] calculateVariables();
  }

  /**
   * Wraps {@link JsScope} as a Value. Scope's variables are inner variables of the Value.
   */
  static class ScopeValue extends ValueBase.ValueWithLazyVariables {
    private final JsScope jsScope;
    private final ValueBase.ValueAsHostObject selfAsHostObject;

    ScopeValue(JsEvaluateContext evaluateContext, IDebugTarget debugTarget, JsScope jsScope,
        ValueBase.ValueAsHostObject selfAsHostObject) {
      super(evaluateContext, debugTarget);
      this.jsScope = jsScope;
      this.selfAsHostObject = selfAsHostObject;
    }
    @Override public String getReferenceTypeName() throws DebugException {
      return "#Scope";
    }
    @Override public String getValueString() {
      return null;
    }
    @Override public boolean isAllocated() throws DebugException {
      return true;
    }
    @Override public boolean hasVariables() throws DebugException {
      return true;
    }
    @Override public Value asRealValue() {
      return null;
    }

    @Override
    protected IVariable[] calculateVariables() {
      return TypeScriptStackFrame.wrapVariables(getEvaluateContext(), getDebugTarget(), jsScope.getVariables(),
          Collections.<String>emptySet(), Collections.<JsVariable>emptyList(), selfAsHostObject,
          null);
    }
  }

  /**
   * Wraps string error message as a Value. The value has no inner variables.
   */
  static class ErrorMessageValue extends ValueBase {
    private final String message;

    ErrorMessageValue(JsEvaluateContext evaluateContext, IDebugTarget debugTarget, String message) {
      super(evaluateContext, debugTarget);
      this.message = message;
    }
    @Override public String getReferenceTypeName() throws DebugException {
      return REFERENCE_TYPE_NAME;
    }
    @Override public String getValueString() {
      return message;
    }
    @Override public boolean isAllocated() throws DebugException {
      return true;
    }
    @Override public IVariable[] getVariables() throws DebugException {
      return Value.EMPTY_VARIABLES;
    }
    @Override public boolean hasVariables() throws DebugException {
      return false;
    }
    @Override public Value asRealValue() {
      return null;
    }

    private static final String REFERENCE_TYPE_NAME = "#Error Message";
  }
}
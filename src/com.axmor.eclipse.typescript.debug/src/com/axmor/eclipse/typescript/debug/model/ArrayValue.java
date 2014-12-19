// Copyright (c) 2009 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.axmor.eclipse.typescript.debug.model;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.chromium.sdk.JsArray;
import org.chromium.sdk.JsEvaluateContext;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IIndexedValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * An IIndexedValue implementation for an array element range using a JsArray
 * instance.
 */
public class ArrayValue extends Value implements IIndexedValue {

	private final AtomicReference<IVariable[]> elementsRef = new AtomicReference<IVariable[]>(
			null);

	public ArrayValue(JsEvaluateContext evaluateContext,
			IDebugTarget debugTarget, JsArray array,
			ValueBase.ValueAsHostObject hostObject) {
		super(evaluateContext, debugTarget, array, hostObject);
	}

	private IVariable[] createElements() {
		JsArray jsArray = (JsArray) getJsValue();
		return TypeScriptStackFrame.wrapVariables(getEvaluateContext(),
				getDebugTarget(), jsArray.getProperties(),
				ARRAY_HIDDEN_PROPERTY_NAMES,
				// Do not show internal properties for arrays (this may be an
				// option).
				null, getHostObject(), null);
	}

	private IVariable[] getElements() {
		IVariable[] result = elementsRef.get();
		if (result == null) {
			result = createElements();
			elementsRef.compareAndSet(null, result);
			return elementsRef.get();
		} else {
			return result;
		}
	}

	public int getInitialOffset() {
		return 0;
	}

	public int getSize() throws DebugException {
		return getElements().length;
	}

	public IVariable getVariable(int offset) throws DebugException {
		return getElements()[offset];
	}

	public IVariable[] getVariables(int offset, int length)
			throws DebugException {
		IVariable[] result = new IVariable[length];
		System.arraycopy(getElements(), offset, result, 0, length);
		return result;
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		return getElements();
	}

	@Override
	public boolean hasVariables() throws DebugException {
		return getElements().length > 0;
	}

	private static final Set<String> ARRAY_HIDDEN_PROPERTY_NAMES = Collections
			.singleton("length");
}

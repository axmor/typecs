/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.chromium.debug.core.ChromiumDebugPlugin;
import org.chromium.sdk.CallFrame;
import org.chromium.sdk.JsEvaluateContext;
import org.chromium.sdk.JsScope;
import org.chromium.sdk.JsVariable;
import org.chromium.sdk.Script;
import org.chromium.sdk.SyncCallback;
import org.chromium.sdk.internal.ScriptBase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.TypeScriptAPIFactory;
import com.axmor.eclipse.typescript.debug.sourcemap.SourceMap;
import com.axmor.eclipse.typescript.debug.sourcemap.SourceMapItem;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

/**
 * @author Konstantin Zaitcev
 * 
 */
public class TypeScriptStackFrame extends TypeScriptDebugElement implements
		IStackFrame {

	private String sourceName;
	private int lineNumber;
	private CallFrame cframe;
	private IThread thread;
	private Map<String, SourceMap> jsMappings;
	private IVariable[] variables;
	private Set<String> classes = Sets.newHashSet();

	private final static Comparator<Variable> VARIABLE_COMPARATOR = new Comparator<Variable>() {
		public int compare(Variable var1, Variable var2) {
			return compareNameObjects(getNameObject(var1), getNameObject(var2));
		}

		// Get property name as String or Long.
		private Object getNameObject(Variable var) {
			String name = var.getName();
			int len = name.length();
			if (len >= 3 && name.charAt(0) == '['
					&& name.charAt(len - 1) == ']') {
				return Long.valueOf(name.substring(1, len - 1));
			}
			return name;
		}

		// Compare property name (either string or long).
		private int compareNameObjects(Object nameObj1, Object nameObj2) {
			if (nameObj1 instanceof Long) {
				Long n1 = (Long) nameObj1;
				if (nameObj2 instanceof Long) {
					Long n2 = (Long) nameObj2;
					return n1.compareTo(n2);
				} else {
					return COMPARE_INT_WITH_STRING;
				}
			} else {
				String s1 = (String) nameObj1;
				if (nameObj2 instanceof String) {
					String s2 = (String) nameObj2;
					return s1.compareTo(s2);
				} else {
					return -COMPARE_INT_WITH_STRING;
				}
			}
		}

		// Strings go before numbers.
		private static final int COMPARE_INT_WITH_STRING = 1;
	};
	
	private static final Set<String> SYSTEM_KEYWORDS = new HashSet<String>() {
		private static final long serialVersionUID = 8246525688616431555L;
		{
			add("<GLOBAL>");
			add("require");
			add("module");
			add("exports");
			add("__filename");
			add("__extends");
			add("__dirname");
			add("__extends");
			add("_super");
			add("__proto__");
			add("constructor");
		}};

	/**
	 * @param cframe
	 * @param jsMappings
	 */
	public TypeScriptStackFrame(IThread thread, CallFrame cframe,
			Map<String, SourceMap> jsMappings) {
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
				SourceMapItem item = jsMappings.get(file.getPath())
						.getItemByJSLine(lineNumber);
				if (item != null) {
					this.sourceName = item.getTsFile();
					this.lineNumber = item.getTsLine();
				}
			} else {
				IFile ifile = ResourcesPlugin.getWorkspace().getRoot()
						.getFileForLocation(Path.fromOSString(file.getPath()));
				if (ifile != null && file.exists()) {
					this.sourceName = ifile.getFullPath().toString();
				}
			}
		} else {
			for (SourceMap mapping : jsMappings.values()) {
				try {
					if (Arrays.equals(TypeScriptDebugTarget.digest(script
							.getSource().getBytes()), TypeScriptDebugTarget
							.digest(Files.toByteArray(new File(mapping
									.getFile()))))) {
						SourceMapItem item = mapping
								.getItemByJSLine(lineNumber);
						if (item != null) {
							this.sourceName = item.getTsFile();
							this.lineNumber = item.getTsLine();
						}
						return;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		Object se = this.getLaunch().getSourceLocator().getSourceElement(this);
		if (se instanceof IFile) {
			JSONArray model = TypeScriptAPIFactory.getTypeScriptAPI(((IFile) se).getProject()).getScriptModel((IFile) se);
			for (int i = 0; i < model.length(); i++) {
				try {
					Object elem = model.get(i);
					if (elem instanceof JSONObject && "class".equals(((JSONObject) elem).get("kind"))) {
						classes.add((String) ((JSONObject) elem).get("name"));
					}
				} catch (JSONException e) {
					// Ignore
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
		IThread thread = getThread();
		if (thread instanceof TypeScriptDebugThread) {
			final IStackFrame tsf = thread.getTopStackFrame();
			final TypeScriptDebugThread tsThread = (TypeScriptDebugThread) thread;
			final SyncCallback[] callback = new SyncCallback[1];
			callback[0] = new SyncCallback() {
				@Override
				public void callbackDone(RuntimeException e) {
					try {
						if (tsf.equals(tsThread.getTopStackFrame())
								&& tsThread.canStepOver()) {
							tsThread.stepInto(callback[0]);
						}
					} catch (DebugException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			};
			tsThread.stepInto(callback[0]);
		} else {
			thread.stepInto();
		}
	}

	@Override
	public void stepOver() throws DebugException {
		IThread thread = getThread();
		if (thread instanceof TypeScriptDebugThread) {
			final IStackFrame tsf = thread.getTopStackFrame();
			final TypeScriptDebugThread tsThread = (TypeScriptDebugThread) thread;
			final SyncCallback[] callback = new SyncCallback[1];
			callback[0] = new SyncCallback() {
				@Override
				public void callbackDone(RuntimeException e) {
					try {
						if (tsf.equals(tsThread.getTopStackFrame())
								&& tsThread.canStepOver()) {
							tsThread.stepOver(callback[0]);
						}
					} catch (DebugException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			};
			tsThread.stepOver(callback[0]);
		} else {
			thread.stepOver();
		}
	}

	@Override
	public void stepReturn() throws DebugException {
		IThread thread = getThread();
		if (thread instanceof TypeScriptDebugThread) {
			final IStackFrame tsf = thread.getTopStackFrame();
			final TypeScriptDebugThread tsThread = (TypeScriptDebugThread) thread;
			final SyncCallback[] callback = new SyncCallback[1];
			callback[0] = new SyncCallback() {
				@Override
				public void callbackDone(RuntimeException e) {
					try {
						if (tsf.equals(tsThread.getTopStackFrame())
								&& tsThread.canStepOver()) {
							tsThread.stepReturn(callback[0]);
						}
					} catch (DebugException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			};
			tsThread.stepReturn(callback[0]);
		} else {
			thread.stepReturn();
		}
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

	public IVariable[] getVariables() throws DebugException {
		if (variables == null) {
			try {
				variables = wrapScopes(cframe.getEvaluateContext(),
						thread.getDebugTarget(), cframe.getVariableScopes(),
						cframe.getReceiverVariable(), classes);
			} catch (RuntimeException e) {
				// We shouldn't throw RuntimeException from here, because
				// calling
				// ElementContentProvider#update will forget to call
				// update.done().
				throw new DebugException(new Status(IStatus.ERROR,
						ChromiumDebugPlugin.PLUGIN_ID,
						"Failed to read variables", e)); //$NON-NLS-1$
			}
		}
		return variables;
	}

	static IVariable[] wrapVariables(JsEvaluateContext evaluateContext,
			IDebugTarget debugTarget, Collection<? extends JsVariable> jsVars,
			Set<? extends String> propertyNameBlackList,
			Collection<? extends JsVariable> jsInternalProperties,
			Variable.Real.HostObject hostObject,
			Collection<? extends Variable> additional) {
		List<Variable> vars = new ArrayList<Variable>(jsVars.size());
		for (JsVariable jsVar : jsVars) {
			if (propertyNameBlackList.contains(jsVar.getName())) {
				continue;
			}
			filterAddVariable(vars, Variable.forRealValue(evaluateContext, debugTarget, jsVar,
					false, hostObject));
		}
		// Sort all regular properties by name.
		Collections.sort(vars, VARIABLE_COMPARATOR);
		// Always put internal properties in the end.
		if (jsInternalProperties != null) {
			for (JsVariable jsMetaVar : jsInternalProperties) {
				filterAddVariable(vars, Variable.forRealValue(evaluateContext, debugTarget,
						jsMetaVar, true, hostObject));
			}
		}
		if (additional != null) {
			filterAddVariable(vars, additional);
		}
		return vars.toArray(new IVariable[vars.size()]);
	}

	static void filterAddVariable(List<Variable> vars,
			Collection<? extends Variable> additional) {
		for (Variable var : additional) {
			filterAddVariable(vars, var);
		}
	}

	static void filterAddVariable(List<Variable> vars, Variable var) {
		filterAddVariable(vars, var, null);
	}

	static void filterAddVariable(List<Variable> vars, Variable var, Set<String> classes) {
		if (SYSTEM_KEYWORDS.contains(var.getName())) {
			return;
		}
		if (classes != null && classes.contains(var.getName())) {
			return;
		}
		try {
			if ("this".equals(var.getName()) && var.getValue().getVariables().length == 0) {
				return;
			}
		} catch (DebugException e) {
			// Ignore
		}
		vars.add(var);
	}

	static IVariable[] wrapScopes(JsEvaluateContext jsEvaluateContext,
			IDebugTarget debugTarget, List<? extends JsScope> jsScopes,
			JsVariable receiverVariable) {
		return wrapScopes(jsEvaluateContext, debugTarget, jsScopes, receiverVariable, null);
	}
	static IVariable[] wrapScopes(JsEvaluateContext jsEvaluateContext,
			IDebugTarget debugTarget, List<? extends JsScope> jsScopes,
			JsVariable receiverVariable, Set<String> classes) {
		List<Variable> vars = new ArrayList<Variable>();

		for (JsScope scope : jsScopes) {
			if (scope.getType() == JsScope.Type.GLOBAL) {
				if (receiverVariable != null) {
					filterAddVariable(vars, Variable.forRealValue(
							jsEvaluateContext, debugTarget, receiverVariable,
							false, null), classes);
					receiverVariable = null;
				}
				// Probably there is no better expression for referring the
				// global object in JS.
				ValueBase.ValueAsHostObject hostObject = new ValueBase.ValueAsHostObject() {
					@Override
					public String getExpression() {
						return "((function(){return this;})())";
					}
				};
				filterAddVariable(vars, Variable.forScope(jsEvaluateContext, debugTarget,
						scope, hostObject), classes);
			} else if (scope.asWithScope() != null) {
				JsScope.WithScope withScope = scope.asWithScope();
				filterAddVariable(vars, Variable.forWithScope(jsEvaluateContext, debugTarget,
						withScope), classes);
			} else {
				int startPos = vars.size();
				for (JsVariable var : scope.getVariables()) {
					filterAddVariable(vars, Variable.forRealValue(jsEvaluateContext,
							debugTarget, var, false, null), classes);
				}
				int endPos = vars.size();
				List<Variable> sublist = vars.subList(startPos, endPos);
				Collections.sort(sublist, VARIABLE_COMPARATOR);
			}
		}
		if (receiverVariable != null) {
			filterAddVariable(vars, Variable.forRealValue(jsEvaluateContext, debugTarget,
					receiverVariable, false, null), classes);
		}

		IVariable[] result = new IVariable[vars.size()];
		// Return in reverse order.
		for (int i = 0; i < result.length; i++) {
			result[result.length - i - 1] = vars.get(i);
		}
		return result;
	}

	public boolean hasVariables() throws DebugException {
		return cframe.getReceiverVariable() != null
				|| cframe.getVariableScopes().size() > 0;
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
				return sf.getSourceName().equals(getSourceName())
						&& sf.getLineNumber() == getLineNumber();
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

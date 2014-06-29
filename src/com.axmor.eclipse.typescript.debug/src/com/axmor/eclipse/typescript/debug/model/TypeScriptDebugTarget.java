/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.model;

import static com.axmor.eclipse.typescript.debug.launching.TypeScriptDebugConstants.TS_DEBUG_MODEL;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.chromium.debug.core.model.JavascriptVmEmbedder;
import org.chromium.debug.core.model.JavascriptVmEmbedder.Listener;
import org.chromium.debug.core.model.JavascriptVmEmbedder.VmConnector;
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
import org.chromium.sdk.JavascriptVm.ScriptsCallback;
import org.chromium.sdk.Script;
import org.chromium.sdk.StandaloneVm;
import org.chromium.sdk.SyncCallback;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.elements.adapters.StackFrameSourceDisplayAdapter;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;
import org.eclipse.ui.IWorkbenchPage;

import com.axmor.eclipse.typescript.core.TypeScriptResources;
import com.axmor.eclipse.typescript.debug.launching.TypeScriptDebugConstants;
import com.axmor.eclipse.typescript.debug.sourcemap.SourceMap;
import com.axmor.eclipse.typescript.debug.sourcemap.SourceMapItem;
import com.axmor.eclipse.typescript.debug.sourcemap.SourceMapParser;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * @author Konstantin Zaitcev
 */
@SuppressWarnings("restriction")
public class TypeScriptDebugTarget extends AbstractTypeScriptDebugTarget
		implements DebugEventListener {

	private JavascriptVm vm;
	private TypeScriptDebugThread thread;
	private boolean suspended;
	private DebugContext ctx;
	private Map<String, SourceMap> tsMappings;
	private Map<String, SourceMap> jsMappings;
	private final AtomicBoolean needResumeAtomic;
	private final CountDownLatch resumeSignal;
	private IStackFrame[] frames = new IStackFrame[0];
	private Map<String, String> nameMapping = Maps.newConcurrentMap();

	private static final ISourceDisplay sourceDisplay = new ISourceDisplay() {
	    private final StackFrameSourceDisplayAdapter sDisplay = new StackFrameSourceDisplayAdapter();
        @Override
        public void displaySource(Object element, IWorkbenchPage page, boolean forceSourceLookup) {
            if (element instanceof TypeScriptDebugTarget) {
                IStackFrame[] frames = ((TypeScriptDebugTarget) element).getStackFrames();
                if (frames.length > 0) {
                    sDisplay.displaySource(frames[0], page, forceSourceLookup);
                }
            } else if (element instanceof TypeScriptDebugThread) {
                IStackFrame frame = ((TypeScriptDebugThread) element).getTopStackFrame();
                if (frame != null) {
                    sDisplay.displaySource(frame, page, forceSourceLookup);
                }
            }
            
        }
    };

	public TypeScriptDebugTarget(ILaunch launch, IProcess process, int port)
			throws CoreException {
		super(process, launch);
		this.tsMappings = new HashMap<String, SourceMap>();
		this.jsMappings = new HashMap<String, SourceMap>();
		StandaloneVm standaloneVm = BrowserFactory.getInstance()
				.createStandalone(new InetSocketAddress("localhost", port),
						null);
		this.vm = standaloneVm;
		this.thread = new TypeScriptDebugThread(this);
		this.setThreads(new IThread[] { this.thread });

			try {
				Thread.sleep(1000);
				((StandaloneVm) vm).attach(this);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
		this.needResumeAtomic = new AtomicBoolean(true);
		this.resumeSignal = new CountDownLatch(1);
		started();
		resumeSignal.countDown();
		DebugPlugin.getDefault().getBreakpointManager()
				.addBreakpointListener(this);
	}

	@SuppressWarnings("rawtypes")
    @Override
	public Object getAdapter(Class adapter) {
	    if (adapter == ISourceDisplay.class) {
	        return sourceDisplay;
	    }
	    return super.getAdapter(adapter);
	}
	
	public TypeScriptDebugTarget(ILaunch launch, VmConnector connector)
			throws CoreException {
		super(null, launch);
		this.tsMappings = new HashMap<String, SourceMap>();
		this.jsMappings = new HashMap<String, SourceMap>();

		Listener embedderListener = new Listener() {

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}

			@Override
			public void closed() {
				// TODO Auto-generated method stub

			}

		};
		final JavascriptVmEmbedder embedder = connector.attach(
				embedderListener, this);

		this.vm = embedder.getJavascriptVm();
		this.thread = new TypeScriptDebugThread(this);
		this.setThreads(new IThread[] { this.thread });
		this.needResumeAtomic = new AtomicBoolean(false);
		this.resumeSignal = new CountDownLatch(0);
		started();
		resume();
		DebugPlugin.getDefault().getBreakpointManager()
				.addBreakpointListener(this);
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
	public void terminate() throws DebugException {
		super.terminate();
	}


    @Override
    public boolean canTerminate() {
    	if (vm instanceof StandaloneVm) {
			return super.canTerminate();
		} else {
			return false;
		}
    }

    @Override
    public boolean isTerminated() {
    	if (vm instanceof StandaloneVm) {
			return super.isTerminated();
		} else {
			return !vm.isAttached();
		}
    }

    @Override
    public boolean canDisconnect() {
    	if (vm instanceof StandaloneVm) {
			return false;
		} else {
			return vm.isAttached();
		}
    }

    @Override
    public void disconnect() throws DebugException {
    	vm.detach();
    	disconnected();
    }

    @Override
    public boolean isDisconnected() {
        return !vm.isAttached();
    }
    
    private static MessageDigest digester;

    static {
        try {
            digester = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    
    public static synchronized byte[] digest(byte[] input) {
    	return digester.digest(input);
    }
    
    @Override
	public void breakpointAdded(IBreakpoint breakpoint) {
		if (supportsBreakpoint(breakpoint)) {
			TypeScriptLineBreakpoint lineBreakpoint = (TypeScriptLineBreakpoint) breakpoint;
			IResource resource = breakpoint.getMarker().getResource();
			String path = resource.getFullPath().toString();
			SourceMap sourceMap = tsMappings.get(path);

			try {
				final SourceMapItem item = sourceMap.getItemByTSLine(lineBreakpoint
						.getLineNumber());
				if (item != null) {
					if (vm instanceof StandaloneVm) {
						ScriptName target = new ScriptName(sourceMap.getFile());
						vm.setBreakpoint(target, item.getJsLine(),
								Breakpoint.EMPTY_VALUE, true, null, null, null);
						
					} else {
						final byte[] md5 = digest(Files.toByteArray(new File(sourceMap.getFile())));
						vm.getScripts(new ScriptsCallback() {
	
							@Override
							public void success(Collection<Script> scripts) {
								for (Script script : scripts) {
									if (Arrays.equals(digest(script.getSource().getBytes()), md5)) {
										ScriptName target = new ScriptName(script.getName());
										vm.setBreakpoint(target, item.getJsLine(),
												Breakpoint.EMPTY_VALUE, true, null, null, null);
									}
								}
							}
	
							@Override
							public void failure(String errorMessage) {
								// TODO Auto-generated method stub
								
							}
							
						});
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (IOException e) {
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
		if (TS_DEBUG_MODEL.equals(breakpoint.getModelIdentifier())) {
			IResource resource = breakpoint.getMarker().getResource();
			String path = resource.getFullPath().toString();
			if (!tsMappings.containsKey(path)) {
				String mapFilePath = TypeScriptResources
						.getSourceMapFilePath(resource.getFullPath().toString());
				if (mapFilePath == null) {
					return false;
				}
				IFile mapFile = ResourcesPlugin.getWorkspace().getRoot()
						.getFile(new Path(mapFilePath));
				SourceMap mapping = new SourceMapParser().parse(mapFile
						.getLocation().toFile());
				if (!jsMappings.containsKey(mapping.getFile())) {
					tsMappings.put(path, mapping);
					jsMappings.put(mapping.getFile(), mapping);
				} else {
					tsMappings.put(path, jsMappings.get(mapping.getFile()));
				}
			}
			return tsMappings.containsKey(path);
		}
		return false;
	}

	/**
	 * @param callback 
     * 
     */
	public void stepOver(SyncCallback callback) {
		if (isSuspended() && ctx != null) {
			ctx.continueVm(StepAction.OVER, 0, null, callback);
		}
	}

	/**
	 * @param callback 
     * 
     */
	public void stepIn(SyncCallback callback) {
		if (isSuspended() && ctx != null) {
			ctx.continueVm(StepAction.IN, 0, null, callback);
		}
	}

	/**
	 * @param callback 
     * 
     */
	public void stepOut(SyncCallback callback) {
		if (isSuspended() && ctx != null) {
			ctx.continueVm(StepAction.OUT, 0, null, callback);
		}
	}

	/**
	 * @return
	 */
	public IStackFrame[] getStackFrames() {
	    return frames;
	}

	// / Debug Event listener methods

	@Override
	public void disconnected() {
		suspended = false;
		DebugPlugin.getDefault().getBreakpointManager()
				.removeBreakpointListener(this);
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

		ArrayList<IStackFrame> frames = new ArrayList<>();
        for (CallFrame cframe : ctx.getCallFrames()) {
            frames.add(new TypeScriptStackFrame(thread, cframe, jsMappings));
        }
        this.frames = (IStackFrame[]) frames.toArray(new IStackFrame[frames.size()]);
		
		Collection<? extends Breakpoint> hits = ctx.getBreakpointsHit();
		if (hits.size() > 0) {
			for (Breakpoint hit : hits) {
				String name = hit.getTarget().accept(new ScriptNameVisitor());
				if (jsMappings.containsKey(name)) {
					SourceMapItem item = jsMappings.get(name).getItemByJSLine(
							hit.getLineNumber());
					if (item != null) {
						IBreakpoint[] breakpoints = DebugPlugin
								.getDefault()
								.getBreakpointManager()
								.getBreakpoints(
										TypeScriptDebugConstants.TS_DEBUG_MODEL);
						for (IBreakpoint breakpoint : breakpoints) {
							try {
								if (breakpoint.isEnabled()
										&& ((ILineBreakpoint) breakpoint)
												.getLineNumber() == item
												.getTsLine()) {
									thread.setBreakpoints(new IBreakpoint[] { breakpoint });
								}
							} catch (CoreException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			suspendedEvent(DebugEvent.BREAKPOINT);
		} else {
			suspendedEvent(DebugEvent.STEP_END);
		}
		suspended = true;

		boolean needResume = needResumeAtomic.getAndSet(false);
		if (needResume) {
			try {
				resumeSignal.await();
				resume();
				return;
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} catch (DebugException e) {
				// Ignore
			}
		}
	}

	// / Event notification methods

	private void started() {
		fireCreationEvent();
		IBreakpoint[] breakpoints = DebugPlugin.getDefault()
				.getBreakpointManager()
				.getBreakpoints(TypeScriptDebugConstants.TS_DEBUG_MODEL);
		for (IBreakpoint breakpoint : breakpoints) {
			breakpointAdded(breakpoint);
		}
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

	public JavascriptVm getJavascriptVm() {
		return vm;
	}

	public synchronized void setScriptNewName(String oldName, String newName) {
		nameMapping.put(newName, oldName);
	}

	public Map<String, String> getNameMapping() {
		return nameMapping;
	}
}

/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.chromium.sdk.JavascriptVm;
import org.chromium.sdk.JavascriptVm.ScriptsCallback;
import org.chromium.sdk.Script;
import org.chromium.sdk.StandaloneVm;
import org.chromium.sdk.SyncCallback;
import org.chromium.sdk.UpdatableScript.ChangeDescription;
import org.chromium.sdk.UpdatableScript.UpdateCallback;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;

import com.axmor.eclipse.typescript.core.Activator;
import com.axmor.eclipse.typescript.debug.model.TypeScriptDebugTarget;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

public class JavaScriptVmSynchronizer implements IResourceChangeListener, IResourceVisitor {

	private final Map<IPath, byte[]> cache = Maps.newConcurrentMap();
	
    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        try {
            event.getDelta().accept(new IResourceDeltaVisitor() {
                @Override
                public boolean visit(IResourceDelta delta) throws CoreException {
                    IResource resource = delta.getResource();
                    if (resource != null && resource.getType() == IResource.FILE) {
                        if (resource.getName().toLowerCase().endsWith(".js")) {
                            IPath location = resource.getLocation();
							if (delta.getKind() == IResourceDelta.REMOVED) {
                            	cache.remove(location);
                            } else {
	                            final File file = location.toFile();
								final byte[] array;
								try {
									array = Files.toByteArray(file);
								} catch (IOException e1) {
									return false;
									// Ignore;
								}
								final String md5 = TypeScriptDebugTarget.stringDigest(cache.get(location));
	                            if (delta.getKind() == IResourceDelta.CHANGED) {
	                            	ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
	                            	ILaunch[] launches = lm.getLaunches();
	                            	for (ILaunch launch : launches) {
	                            		final IDebugTarget target = launch.getDebugTarget();
										if (target instanceof TypeScriptDebugTarget) {
											final JavascriptVm jsVm = ((TypeScriptDebugTarget) target).getJavascriptVm();
											jsVm.getScripts(new ScriptsCallback() {
	
												@Override
												public void success(
														Collection<Script> scripts) {
													for (final Script script : scripts) {
														if (jsVm instanceof StandaloneVm) {
															updateScriptOnStandAlone(file,
																	array,
																	target,
																	script);
														} else {
															updateScriptOnBrowser(
																	array, md5,
																	target,
																	script);
														}
													}
													
												}
												@Override
												public void failure(
														String errorMessage) {
													// Ignore
												}
												
											});
	                            		}
	                            	}
	                            }
	            				cache.put(location, array);
                            }
                        }
                    }
                    return true;
                }
            });
        } catch (CoreException e) {
            Activator.error(e);
        }
    }

	private void updateScriptOnStandAlone(
			final File file,
			final byte[] array,
			final IDebugTarget target,
			final Script script) {
		String name = script.getName();
		do {
			if (file.getAbsolutePath().equalsIgnoreCase(name)) {
				UpdateCallback callback = new UpdateCallback(){

					@Override
					public void success(
							Object report,
							ChangeDescription changeDescription) {
						String name = changeDescription.getCreatedScriptName();
						if (name != null) {
							((TypeScriptDebugTarget) target).setScriptNewName(script.getName(), name);
						}
					}

					@Override
					public void failure(
							String message) {
						// TODO Auto-generated method stub
						
					}
					
				};
				SyncCallback syncCallback = new SyncCallback(){

					@Override
					public void callbackDone(
							RuntimeException e) {
						// TODO Auto-generated method stub
						
					}
					
				};
				script.setSourceOnRemote(new String(array), callback, syncCallback);
			}
		} while ((name = ((TypeScriptDebugTarget) target).getNameMapping().get(name)) != null);
	}

	private void updateScriptOnBrowser(
			final byte[] array,
			final String md5,
			final IDebugTarget target,
			final Script script) {
		String name = TypeScriptDebugTarget.stringDigest(script.getSource().getBytes());
		do {
			if (name.equals(md5)) {
				UpdateCallback callback = new UpdateCallback(){

					@Override
					public void success(
							Object report,
							ChangeDescription changeDescription) {
						String name = changeDescription.getCreatedScriptName();
						if (name != null) {
							((TypeScriptDebugTarget) target).setScriptNewName(TypeScriptDebugTarget.stringDigest(script.getSource().getBytes()), TypeScriptDebugTarget.stringDigest(array));
						}
					}

					@Override
					public void failure(
							String message) {
						// TODO Auto-generated method stub
						
					}
					
				};
				SyncCallback syncCallback = new SyncCallback(){

					@Override
					public void callbackDone(
							RuntimeException e) {
						// TODO Auto-generated method stub
						
					}
					
				};
				script.setSourceOnRemote(new String(array), callback, syncCallback);
			}
		} while ((name = ((TypeScriptDebugTarget) target).getNameMapping().get(name)) != null);
	}
	
	@Override
	public boolean visit(IResource resource) throws CoreException {
		if (resource.getName().toLowerCase().endsWith(".js")) {
			
			try {
				cache.put(resource.getLocation(), Files.toByteArray(resource.getLocation().toFile()));
			} catch (IOException e) {
				// ignore
			}
			
			return false;
		} else {
			return true;
		}
	}

}

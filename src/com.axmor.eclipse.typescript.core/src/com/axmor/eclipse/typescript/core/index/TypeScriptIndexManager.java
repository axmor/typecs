/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.core.index;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple4;

import com.axmor.eclipse.typescript.core.Activator;
import com.axmor.eclipse.typescript.core.TypeScriptAPIFactory;
import com.axmor.eclipse.typescript.core.TypeScriptResources;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptIndexManager implements IResourceChangeListener {
    /** Index job. */
    private IndexJob job;

    /**
     * Starts indexing process.
     */
    public void startIndex() {
        job = new IndexJob();
        job.setSystem(true);
        job.schedule();

        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    }

    /**
     * Stops indexing process.
     */
    public void stopIndex() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        if (job.cancel()) {
            job.getIndexer().close();
        }
    }
    
    /**
     * Flushes changes on disk.
     */
    public void flush() {
        job.getIndexer().flush();
    }

	public Iterable<IndexInfo> searchByName(final String name) {
		return Iterables.transform(Iterables.filter(job.getIndexer().idxTypes,
				new Predicate<Fun.Tuple4<String, String, String, IndexInfo>>() {
					@Override
					public boolean apply(Tuple4<String, String, String, IndexInfo> input) {
						if (name.startsWith("*")) {
							return name.length() < 2 || input.b.toLowerCase().contains(name.substring(1).toLowerCase());
						} else {
							return input.b.toLowerCase().startsWith(name.toLowerCase());
						}
					}
				}), new Function<Fun.Tuple4<String, String, String, IndexInfo>, IndexInfo>() {
			@Override
			public IndexInfo apply(Tuple4<String, String, String, IndexInfo> input) {
				return input.d;
			}
		});
	}

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        try {
            event.getDelta().accept(new IResourceDeltaVisitor() {
                @Override
                public boolean visit(IResourceDelta delta) throws CoreException {
                    IResource resource = delta.getResource();
                    if (resource != null && resource.getType() == IResource.FILE) {
                        if (TypeScriptResources.isTypeScriptFile(resource.getName())) {
                            if (delta.getKind() == IResourceDelta.REMOVED) {
                                job.getIndexer().removeFromIndex(resource.getFullPath().toString());
                            } else {
                                job.getChangedResources().add(resource.getFullPath().toString());
                            }
                            if (delta.getKind() == IResourceDelta.ADDED) {
                                TypeScriptAPIFactory.getTypeScriptAPI(resource.getProject()).addFile((IFile) resource);
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
}

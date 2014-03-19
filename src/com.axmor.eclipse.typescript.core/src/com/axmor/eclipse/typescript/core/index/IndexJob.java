/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.core.index;

import java.util.Set;

import org.apache.lucene.store.NRTCachingDirectory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.axmor.eclipse.typescript.core.TypeScriptResources;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * @author Konstantin Zaitcev
 */
public class IndexJob extends Job {

    /** Wait period between index task. */
    private static final int WAIT_PERIOD_MS = 5000;
    
    /** Resources was changed in workspace and should be indexed. */
    private Set<String> changedResources = Sets.newConcurrentHashSet();
    /** Indexer. */
    private TypeScriptIndexer indexer;
    /** Caching indexed directory. */
    private NRTCachingDirectory idxDir;
    
    /**
     * @param name
     */
    public IndexJob() {
        super("Indexing TypeScript source files");
        if (indexer == null) {
            this.indexer = new TypeScriptIndexer();
            idxDir = indexer.getIdxDir();
        }
    }
    
    /**
     * @return the indexer
     */
    public TypeScriptIndexer getIndexer() {
        return indexer;
    }
    
    /**
     * @return the changedResources
     */
    public Set<String> getChangedResources() {
        return changedResources;
    }
    
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        if (changedResources.size() == 0) {
            try {
                ResourcesPlugin.getWorkspace().getRoot().accept(new IResourceProxyVisitor() {
                    @Override
                    public boolean visit(IResourceProxy proxy) throws CoreException {
                        if (proxy.getType() == IResource.FILE 
                                && TypeScriptResources.isTypeScriptFile(proxy.getName())) {
                            String path = proxy.requestFullPath().toString();
                            if (indexer.checkFile(path, proxy.getModificationStamp())) {
                                changedResources.add(path);
                            }
                        }
                        return true;
                    }
                }, 0);
            } catch (CoreException e) {
                throw Throwables.propagate(e);
            }
        }
        
        ImmutableSet<String> changed = null;
        
        synchronized (changedResources) {
            changed = ImmutableSet.copyOf(changedResources);
            changedResources.clear();
        }

        try {
            monitor.beginTask("Indexing TypeScript source files", changed.size());
            for (String path : changed) {
                monitor.worked(1);
                indexer.indexFile(path);
                if (monitor.isCanceled()) {
                    indexer.close();
                    return Status.CANCEL_STATUS;
                }
            }
            monitor.done();
            return Status.OK_STATUS;
        } finally {
            schedule(WAIT_PERIOD_MS);
        }
    }
    
    /**
     * Get index directory
     * 
     * @return caching directory
     */
    public NRTCachingDirectory getIndexDirectory() {
        return idxDir;
    }
}

/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

/**
 * @author Konstantin Zaitcev
 */
public class DelayedDocumentListener extends Job implements IDocumentListener {

    /** Delay in milliseconds. */
    private static final int DELAY_MS = 500;

    /** List of processors that notified on document change. */
    private List<IDocumentProcessor> processors;

    /** Listened document. */
    private IDocument doc;

    /** File related to the document. */
    private IFile file;

    /**
     * The constructor
     * 
     * @param file 
     * @param doc 
     */
    public DelayedDocumentListener(IFile file, IDocument doc) {
        super("Document listener");
        this.file = file;
        this.doc = doc;
        this.processors = Collections.synchronizedList(new ArrayList<IDocumentProcessor>());
    }

    @Override
    public void documentAboutToBeChanged(DocumentEvent event) {
        // empty block
    }

    @Override
    public void documentChanged(DocumentEvent event) {
        cancel();
        schedule(DELAY_MS);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        for (IDocumentProcessor processor : processors) {
            processor.processDocument(file, doc);
        }
        return Status.OK_STATUS;
    }

    /**
     * Adds processor to list of notification.
     * 
     * @param processor 
     */
    public void addDocumentProcessor(IDocumentProcessor processor) {
        processors.add(processor);
    }

    /**
     * Removes processor from list of notification.
     * 
     * @param processor 
     */
    public void removeDocumentProcessor(IDocumentProcessor processor) {
        processors.remove(processor);
    }
}

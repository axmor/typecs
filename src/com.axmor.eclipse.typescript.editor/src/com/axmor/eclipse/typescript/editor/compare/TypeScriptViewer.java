/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.compare;

import java.io.InputStreamReader;

import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.axmor.eclipse.typescript.editor.Activator;
import com.axmor.eclipse.typescript.editor.TypeScriptEditorConfiguration;
import com.axmor.eclipse.typescript.editor.parser.TypeScriptPartitionScanner;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptViewer extends Viewer {

    /** Source viewer. */
    private SourceViewer fSourceViewer;
    /** Object ot view. */
    private Object fInput;

    /**
     * Constructor.
     * 
     * @param parent
     *            parent composite
     */
    TypeScriptViewer(Composite parent) {
        fSourceViewer = new SourceViewer(parent, null, SWT.LEFT_TO_RIGHT | SWT.H_SCROLL | SWT.V_SCROLL);
        fSourceViewer.configure(new TypeScriptEditorConfiguration());
        fSourceViewer.setEditable(false);
    }

    @Override
    public Control getControl() {
        return fSourceViewer.getControl();
    }

    @Override
    public void setInput(Object input) {
        if (input instanceof IStreamContentAccessor) {
            Document document = new Document(getString(input));
            IDocumentPartitioner partitioner = new FastPartitioner(new TypeScriptPartitionScanner(),
                    TypeScriptPartitionScanner.TS_PARTITION_TYPES);
            partitioner.connect(document);
            document.setDocumentPartitioner(partitioner);
            fSourceViewer.setDocument(document);
        }
        fInput = input;
    }

    @Override
    public Object getInput() {
        return fInput;
    }

    @Override
    public ISelection getSelection() {
        return null;
    }

    @Override
    public void setSelection(ISelection s, boolean reveal) {
    }

    @Override
    public void refresh() {
    }

    /**
     * @param input
     *            input source
     * @return string content of input
     */
    private static String getString(Object input) {

        if (input instanceof IStreamContentAccessor) {
            IStreamContentAccessor sca = (IStreamContentAccessor) input;
            try {
                return CharStreams.toString(new InputStreamReader(sca.getContents(), Charsets.UTF_8));
            } catch (Exception e) {
                Activator.error(e);
            }
        }
        return "";
    }
}

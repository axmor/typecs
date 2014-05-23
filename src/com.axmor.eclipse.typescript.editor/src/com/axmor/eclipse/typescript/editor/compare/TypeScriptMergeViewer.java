/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;

import com.axmor.eclipse.typescript.editor.TypeScriptEditorConfiguration;
import com.axmor.eclipse.typescript.editor.parser.TypeScriptPartitionScanner;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptMergeViewer extends TextMergeViewer {

    /**
     * @param parent
     *            parent
     * @param configuration
     *            configuration
     */
    public TypeScriptMergeViewer(Composite parent, CompareConfiguration configuration) {
        super(parent, configuration);
    }

    @Override
    protected void configureTextViewer(TextViewer textViewer) {
        ((SourceViewer) textViewer).configure(new TypeScriptEditorConfiguration());
    }

    @Override
    protected IDocumentPartitioner getDocumentPartitioner() {
        return new FastPartitioner(new TypeScriptPartitionScanner(), TypeScriptPartitionScanner.TS_PARTITION_TYPES);
    }
    
    @Override
    protected String getDocumentPartitioning() {
        return "__ts_partitioner";
    }
    
    @Override
    public String getTitle() {
        return "TypeScript Source Compare";
    }
}

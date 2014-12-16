/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;
import org.osgi.service.prefs.BackingStoreException;

import com.axmor.eclipse.typescript.editor.Activator;
import com.axmor.eclipse.typescript.editor.TypeScriptEditorConfiguration;
import com.axmor.eclipse.typescript.editor.parser.TypeScriptPartitionScanner;

/**
 * @author kudrin
 *
 */
public class TypescriptTemplatePreferencePage extends TemplatePreferencePage {
    
    public static final String TEMPLATES_USE_CODEFORMATTER = "com.axmor.eclipse.typescript.editor.preferences.format"; //$NON-NLS-1$
    
    public TypescriptTemplatePreferencePage() {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setTemplateStore(TypescriptTemplateAccess.getDefault().getTemplateStore());
        setContextTypeRegistry(TypescriptTemplateAccess.getDefault().getContextTypeRegistry());
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk() {
          boolean ok = super.performOk();
          try {
              InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).flush();
          } catch (BackingStoreException e) {
              Activator.error(e);
          }
          return ok;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#createViewer(org.eclipse.swt.widgets.Composite)
     */
    protected SourceViewer createViewer(Composite parent) {
        SourceViewer viewer = new SourceViewer(parent, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
          
        SourceViewerConfiguration configuration = new TypeScriptEditorConfiguration();        
        IDocument document = new Document();       
        //new AntDocumentSetupParticipant().setup(document);
        IDocumentPartitioner partitioner = new FastPartitioner(new TypeScriptPartitionScanner(), TypeScriptPartitionScanner.TS_PARTITION_TYPES);
        partitioner.connect(document);
        document.setDocumentPartitioner(partitioner);
        viewer.configure(configuration);
        viewer.setDocument(document);
        viewer.setEditable(false);  
        Font font = JFaceResources.getFont(JFaceResources.TEXT_FONT);
        viewer.getTextWidget().setFont(font);    
                
        return viewer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#getFormatterPreferenceKey()
     */
    protected String getFormatterPreferenceKey() {
        return TEMPLATES_USE_CODEFORMATTER;
    }
    
    /*
     * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#updateViewerInput()
     */
    protected void updateViewerInput() {
        IStructuredSelection selection = (IStructuredSelection) getTableViewer().getSelection();
        SourceViewer viewer = getViewer();
        
        if (selection.size() == 1 && selection.getFirstElement() instanceof TemplatePersistenceData) {
            TemplatePersistenceData data = (TemplatePersistenceData) selection.getFirstElement();
            Template template= data.getTemplate();
            viewer.getDocument().set(template.getPattern());       
        } else {
            viewer.getDocument().set(""); //$NON-NLS-1$
        }       
    }
    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#isShowFormatterSetting()
     */
    protected boolean isShowFormatterSetting() {
        return false;
    }

}

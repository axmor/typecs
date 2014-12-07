/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor;

import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * Source viewer for TypeScript editor
 * 
 * @author Asya Vorobyova
 */
public class TypeScriptProjectionViewer extends ProjectionViewer {

    /**
     * Text operation code for requesting the quick outline for the current input.
     */
    public static final int QUICK_OUTLINE = 513;

    /**
     * Presenter to generate quick outline dialog
     */
    private IInformationPresenter outlinePresenter;

	private TypeScriptEditor editor;

    /**
	 * Constructor
	 * 
	 * @param editor
	 * 
	 * @param parent
	 *            the SWT parent control
	 * @param ruler
	 *            the vertical ruler
	 * @param overviewRuler
	 *            the overview ruler
	 * @param showsAnnotationOverview
	 *            <code>true</code> if the overview ruler should be shown
	 * @param styles
	 *            the SWT style bits
	 */
	public TypeScriptProjectionViewer(TypeScriptEditor editor, Composite parent, IVerticalRuler ruler,
			IOverviewRuler overviewRuler,
            boolean showsAnnotationOverview, int styles) {
        super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
		this.editor = editor;
    }
    
    @Override
    public void doOperation(int operation) {
        // Ensure underlying text widget is defined
        if ((getTextWidget() == null) || getTextWidget().isDisposed()) {
            return;
        }
        // Handle quick outline operation
        if (operation == QUICK_OUTLINE) {
            if (outlinePresenter != null) {
                outlinePresenter.showInformation();
            }
            return;
        }
        // Handle default operations
        super.doOperation(operation);
    }

    @Override
    public boolean canDoOperation(int operation) {
        // Verify quick outline operation
        if (operation == QUICK_OUTLINE) {
            if (outlinePresenter == null) {
                return false;
            }
            return true;
        }
        // Verify default operations
        return super.canDoOperation(operation);
    }

    @Override
    public void configure(SourceViewerConfiguration configuration) {
        // Ensure underlying text widget is defined
        if ((getTextWidget() == null) || getTextWidget().isDisposed()) {
            return;
        }
        // Configure default operations
        super.configure(configuration);
        // Configure quick outline operation for the source viewer only if the
        // given source viewer supports it
        if (configuration instanceof TypeScriptEditorConfiguration) {
            TypeScriptEditorConfiguration sourceConfiguration = (TypeScriptEditorConfiguration) configuration;
            outlinePresenter = sourceConfiguration.getOutlinePresenter(this);
            if (outlinePresenter != null) {
                outlinePresenter.install(this);
            }
        }
    }

    @Override
    public void unconfigure() {
        // Unconfigure quick outline operation
        if (outlinePresenter != null) {
            outlinePresenter.uninstall();
            outlinePresenter = null;
        }
        // Unconfigure default operations
        super.unconfigure();
    }

	/**
	 * @return the editor
	 */
	public TypeScriptEditor getEditor() {
		return editor;
	}
}

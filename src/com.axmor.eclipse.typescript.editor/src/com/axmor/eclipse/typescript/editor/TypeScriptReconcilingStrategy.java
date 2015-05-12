package com.axmor.eclipse.typescript.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;

public class TypeScriptReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {
    
    private IProgressMonitor monitor;
    private IDocument document;
    private ISourceViewer viewer;
    private ITextEditor editor;
    
    public TypeScriptReconcilingStrategy(ISourceViewer viewer, ITextEditor editor) {
        this.viewer = viewer;
        this.editor = editor;
    }

    @Override
    public void initialReconcile() {
        reconcile(new Region(0, document.getLength()));
    }

    @Override
    public void setProgressMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void reconcile(IRegion partition) {
        if (editor instanceof TypeScriptEditor) {
            ((TypeScriptEditor) editor).updateSemanticHigliting();
        }
    }

    @Override
    public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
        reconcile(dirtyRegion);
    }

    @Override
    public void setDocument(IDocument document) {
        this.document = document;
    }

}

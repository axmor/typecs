package com.axmor.eclipse.typescript.editor.console;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;

public class ScriptConsolePartitioner implements IConsoleDocumentPartitioner {
    
    private static final String[] LEGAL_CONTENT_TYPES = new String[] { IDocument.DEFAULT_CONTENT_TYPE };

    @Override
    public ITypedRegion[] computePartitioning(int offset, int length) {
        return new TypedRegion[] { new TypedRegion(offset, length, IDocument.DEFAULT_CONTENT_TYPE) };
    }

    @Override
    public void connect(IDocument document) {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void documentAboutToBeChanged(DocumentEvent event) {
    }

    @Override
    public boolean documentChanged(DocumentEvent event) {
        return false;
    }

    @Override
    public String getContentType(int offset) {
        return IDocument.DEFAULT_CONTENT_TYPE;
    }

    @Override
    public String[] getLegalContentTypes() {
        return LEGAL_CONTENT_TYPES;
    }

    @Override
    public ITypedRegion getPartition(int offset) {
        return new TypedRegion(offset, 1, IDocument.DEFAULT_CONTENT_TYPE);
    }

    @Override
    public StyleRange[] getStyleRanges(int offset, int length) {    
        return new StyleRange[0];
    }

    @Override
    public boolean isReadOnly(int offset) {
        return false;
    }

}

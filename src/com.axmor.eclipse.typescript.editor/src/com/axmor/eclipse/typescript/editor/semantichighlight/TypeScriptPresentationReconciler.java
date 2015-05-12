package com.axmor.eclipse.typescript.editor.semantichighlight;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.PresentationReconciler;

public class TypeScriptPresentationReconciler extends PresentationReconciler {

    /** Last used document */
    private IDocument fLastDocument;

    /**
     * Constructs a "repair description" for the given damage and returns this description as a text
     * presentation.
     * <p>
     * NOTE: Should not be used if this reconciler is installed on a viewer.
     * </p>
     */
    public TextPresentation createRepairDescription(IRegion damage, IDocument document) {
        if (document != fLastDocument) {
            setDocumentToDamagers(document);
            setDocumentToRepairers(document);
            fLastDocument = document;
        }
        return createPresentation(damage, document);
    }

}

package com.axmor.eclipse.typescript.editor.occurrence;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.texteditor.IDocumentProvider;

import com.axmor.eclipse.typescript.editor.TypeScriptEditor;

/**
 * Cancels the occurrences finder job upon document changes.
 */
public class OccurrencesFinderJobCanceler implements IDocumentListener, ITextInputListener {

	private final TypeScriptEditor editor;
	private OccurrencesFinderJob fOccurrencesFinderJob;

	public OccurrencesFinderJobCanceler(TypeScriptEditor editor, OccurrencesFinderJob fOccurrencesFinderJob) {
		this.editor = editor;
		this.fOccurrencesFinderJob = fOccurrencesFinderJob;
	}

	public void install() {
		ISourceViewer sourceViewer = editor.getViewer();
		if (sourceViewer == null) {
			return;
		}

		StyledText text = sourceViewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			return;
		}

		sourceViewer.addTextInputListener(this);

		IDocument document = sourceViewer.getDocument();
		if (document != null) {
			document.addDocumentListener(this);
		}
	}

	public void uninstall() {
		ISourceViewer sourceViewer = editor.getViewer();
		if (sourceViewer != null) {
			sourceViewer.removeTextInputListener(this);
		}

		IDocumentProvider documentProvider = editor.getDocumentProvider();
		if (documentProvider != null) {
			IDocument document = documentProvider.getDocument(editor.getEditorInput());
			if (document != null) {
				document.removeDocumentListener(this);
			}
		}
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		if (fOccurrencesFinderJob != null) {
			fOccurrencesFinderJob.doCancel();
		}
	}

	@Override
	public void documentChanged(DocumentEvent event) {
		// do nothing
	}

	@Override
	public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
		if (oldInput == null) {
			return;
		}

		oldInput.removeDocumentListener(this);
	}

	@Override
	public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		if (newInput == null) {
			return;
		}
		newInput.addDocumentListener(this);
	}
}
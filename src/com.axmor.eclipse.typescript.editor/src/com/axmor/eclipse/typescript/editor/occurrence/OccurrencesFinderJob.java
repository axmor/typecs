package com.axmor.eclipse.typescript.editor.occurrence;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ISelectionValidator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.texteditor.IDocumentProvider;

import com.axmor.eclipse.typescript.editor.TypeScriptEditor;

/**
 * Finds and marks occurrence annotations.
 */
public class OccurrencesFinderJob extends Job {

	private TypeScriptEditor editor;
	private IDocument fDocument;
	private ISelection fSelection;
	private ISelectionValidator fPostSelectionValidator;
	private boolean fCanceled = false;
	private IProgressMonitor fProgressMonitor;
	private List<Position> fPositions;

	public OccurrencesFinderJob(TypeScriptEditor typeScriptEditor, IDocument document, List<Position> positions,
			ISelection selection) {
		super("Occurrences Marker"); //$NON-NLS-1$
		editor = typeScriptEditor;
		fDocument = document;
		fSelection = selection;
		fPositions = positions;

		if (editor.getSelectionProvider() instanceof ISelectionValidator)
			fPostSelectionValidator = (ISelectionValidator) editor.getSelectionProvider();
	}

	// cannot use cancel() because it is declared final
	void doCancel() {
		fCanceled = true;
		cancel();
	}

	private boolean isCanceled() {
		return fCanceled || fProgressMonitor.isCanceled() || fPostSelectionValidator != null
				&& !(fPostSelectionValidator.isValid(fSelection)) || LinkedModeModel.hasInstalledModel(fDocument);
	}

	/*
	 * @see Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus run(IProgressMonitor progressMonitor) {

		fProgressMonitor = progressMonitor;

		if (isCanceled())
			return Status.CANCEL_STATUS;

		ITextViewer textViewer = editor.getViewer();
		if (textViewer == null)
			return Status.CANCEL_STATUS;

		IDocument document = textViewer.getDocument();
		if (document == null)
			return Status.CANCEL_STATUS;

		IDocumentProvider documentProvider = editor.getDocumentProvider();
		if (documentProvider == null)
			return Status.CANCEL_STATUS;

		IAnnotationModel annotationModel = documentProvider.getAnnotationModel(editor.getEditorInput());
		if (annotationModel == null)
			return Status.CANCEL_STATUS;

		// Add occurrence annotations
		int length = fPositions.size();
		Map<Annotation, Position> annotationMap = new HashMap<Annotation, Position>(length);
		for (int i = 0; i < length; i++) {

			if (isCanceled())
				return Status.CANCEL_STATUS;

			String message;
			Position position = fPositions.get(i);

			// Create & add annotation
			try {
				message = document.get(position.offset, position.length);
			} catch (BadLocationException ex) {
				// Skip this match
				continue;
			}
			annotationMap.put(new Annotation("org.eclipse.jdt.ui.occurrences", false, message), //$NON-NLS-1$
					position);
		}

		if (isCanceled()) {
			return Status.CANCEL_STATUS;
		}

		updateAnnotations(annotationModel, annotationMap);

		return Status.OK_STATUS;
	}

	private void updateAnnotations(IAnnotationModel annotationModel, Map<Annotation, Position> annotationMap) {
		if (annotationModel instanceof IAnnotationModelExtension) {
			((IAnnotationModelExtension) annotationModel).replaceAnnotations(editor.fOccurrenceAnnotations, annotationMap);
		} else {
			editor.removeOccurrenceAnnotations();
			Iterator<Map.Entry<Annotation, Position>> iter = annotationMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Annotation, Position> mapEntry = iter.next();
				annotationModel.addAnnotation(mapEntry.getKey(), mapEntry.getValue());
			}
		}
		editor.fOccurrenceAnnotations = annotationMap.keySet().toArray(new Annotation[annotationMap.keySet().size()]);
	}
}
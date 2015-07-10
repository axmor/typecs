package com.axmor.eclipse.typescript.editor.semantichighlight;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.FileEditorInput;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.editor.Activator;
import com.axmor.eclipse.typescript.editor.TypeScriptEditor;
import com.axmor.eclipse.typescript.editor.semantichighlight.TypeScriptSemanticManager.HighlightedPosition;
import com.axmor.eclipse.typescript.editor.semantichighlight.TypeScriptSemanticManager.Highlighting;

public class TypeScriptSemanticReconciler implements ITextInputListener {

    /** The Java editor this semantic highlighting reconciler is installed on */
    private TypeScriptEditor fEditor;
    /** The source viewer this semantic highlighting reconciler is installed on */
    private ISourceViewer fSourceViewer;
    /** The semantic highlighting presenter */
    private TypeScriptSemanticPresenter fPresenter;
    /** Semantic highlightings */
    private TypeScriptSemanticHighlighting[] fSemanticHighlightings;
    /** Highlightings */
    private Highlighting[] fHighlightings;

    /** Background job's added highlighted positions */
    private List<Position> fAddedPositions = new ArrayList<Position>();
    /** Background job's removed highlighted positions */
    private List<Position> fRemovedPositions = new ArrayList<Position>();
    /** Number of removed positions */
    private int fNOfRemovedPositions;

    /** Background job */
    private Job fJob;
    /** Background job lock */
    private final Object fJobLock = new Object();
    /**
     * Reconcile operation lock.
     */
    private final Object fReconcileLock = new Object();
    /**
     * <code>true</code> if any thread is executing <code>reconcile</code>, <code>false</code>
     * otherwise.
     */
    private boolean fIsReconciling = false;

    /**
     * The semantic highlighting presenter - cache for background thread, only valid during
     */
    private TypeScriptSemanticPresenter fJobPresenter;
    /**
     * Semantic highlightings - cache for background thread, only valid during
     */
    private TypeScriptSemanticHighlighting[] fJobSemanticHighlightings;
    /**
     * Highlightings - cache for background thread, only valid during
     */
    private Highlighting[] fJobHighlightings;

    /*
     * @see org.eclipse.jdt.internal.ui.text.java.IJavaReconcilingListener# aboutToBeReconciled()
     */
    public void aboutToBeReconciled() {
        // Do nothing
    }

    /*
     * @see org.eclipse.jdt.internal.ui.text.java.IJavaReconcilingListener#reconciled
     * (CompilationUnit, boolean, IProgressMonitor)
     */
    public void reconciled(IFile file, boolean forced, IProgressMonitor progressMonitor) {
        // ensure at most one thread can be reconciling at any time
        synchronized (fReconcileLock) {
            if (fIsReconciling) {
                return;
            } else {
                fIsReconciling = true;
            }
        }
        fJobPresenter = fPresenter;
        fJobSemanticHighlightings = fSemanticHighlightings;
        fJobHighlightings = fHighlightings;

        try {
            if (fJobPresenter == null || fJobSemanticHighlightings == null || fJobHighlightings == null) {
                return;
            }

            fJobPresenter.setCanceled(progressMonitor.isCanceled());

            if (file == null || fJobPresenter.isCanceled()) {
                return;
            }

            JSONArray nodes = fEditor.getApi().getIdentifiers(file);
            if (nodes == null || nodes.length() == 0) {
                return;
            }

            startReconcilingPositions();

            if (!fJobPresenter.isCanceled()) {
                reconcilePositions(nodes);
            }

            TextPresentation textPresentation = null;
            if (!fJobPresenter.isCanceled()) {
                textPresentation = fJobPresenter.createPresentation(fAddedPositions, fRemovedPositions);
            }

            if (!fJobPresenter.isCanceled()) {
                updatePresentation(textPresentation, fAddedPositions, fRemovedPositions);
            }

            stopReconcilingPositions();
        } finally {
            fJobPresenter = null;
            fJobSemanticHighlightings = null;
            fJobHighlightings = null;
            synchronized (fReconcileLock) {
                fIsReconciling = false;
            }
        }
    }

    /**
     * Start reconciling positions.
     */
    private void startReconcilingPositions() {
        fJobPresenter.addAllPositions(fRemovedPositions);
        fNOfRemovedPositions = fRemovedPositions.size();
    }

    /**
     * Reconcile positions
     * 
     */
    private void reconcilePositions(JSONArray nodes) {
        for (int i = 0; i < nodes.length(); i++) {
            try {
                if (nodes.get(i) instanceof JSONObject) {
                    JSONObject obj = (JSONObject) nodes.get(i);
                    visit(obj);
                }
            } catch (JSONException e) {
                Activator.error(e);
            }
        }

        List<Position> oldPositions = fRemovedPositions;
        List<Position> newPositions = new ArrayList<Position>(fNOfRemovedPositions);
        for (int i = 0, n = oldPositions.size(); i < n; i++) {
            Position current = oldPositions.get(i);
            if (current != null) {
                newPositions.add(current);
            }
        }
        fRemovedPositions = newPositions;
    }

    private boolean visit(JSONObject node) {
        for (int i = 0, n = fJobSemanticHighlightings.length; i < n; i++) {
            TypeScriptSemanticHighlighting semanticHighlighting = fJobSemanticHighlightings[i];
            if (fJobHighlightings[i].isEnabled() && semanticHighlighting.consumes(node)) {
                int offset;
                int length;
                try {
                    length = node.getInt("length");
                    offset = node.getInt("offset");
                    if (offset > -1 && length > 0) {
                        addPosition(offset, length, fJobHighlightings[i]);
                    }
                    break;
                } catch (JSONException e) {
                    Activator.error(e);
                }
            }
        }
        return true;
    }

    /**
     * Add a position with the given range and highlighting iff it does not exist already.
     *
     * @param offset
     *            The range offset
     * @param length
     *            The range length
     * @param highlighting
     *            The highlighting
     */
    private void addPosition(int offset, int length, Highlighting highlighting) {
        boolean isExisting = false;
		// System.out.println(offset + ", " + length + ", " + highlighting.getName());
        for (int i = 0, n = fRemovedPositions.size(); i < n; i++) {
            HighlightedPosition position = (HighlightedPosition) fRemovedPositions.get(i);
            if (position == null) {
                continue;
            }
            if (position.isEqual(offset, length, highlighting)) {
                isExisting = true;
                fRemovedPositions.set(i, null);
                fNOfRemovedPositions--;
                break;
            }
        }

        if (!isExisting) {
            Position position = fJobPresenter.createHighlightedPosition(offset, length, highlighting);
            fAddedPositions.add(position);
        }
    }

    /**
     * Update the presentation.
     *
     * @param textPresentation
     *            the text presentation
     * @param addedPositions
     *            the added positions
     * @param removedPositions
     *            the removed positions
     */
    private void updatePresentation(TextPresentation textPresentation, List<Position> addedPositions,
            List<Position> removedPositions) {
        Runnable runnable = fJobPresenter.createUpdateRunnable(textPresentation, addedPositions, removedPositions);
        if (runnable == null) {
            return;
        }

        TypeScriptEditor editor = fEditor;
        if (editor == null) {
            return;
        }

        IWorkbenchPartSite site = editor.getSite();
        if (site == null) {
            return;
        }

        Shell shell = site.getShell();
        if (shell == null || shell.isDisposed()) {
            return;
        }

        Display display = shell.getDisplay();
        if (display == null || display.isDisposed()) {
            return;
        }

        display.asyncExec(runnable);
    }

    /**
     * Stop reconciling positions.
     */
    private void stopReconcilingPositions() {
        fRemovedPositions.clear();
        fNOfRemovedPositions = 0;
        fAddedPositions.clear();
    }

    /**
     * Install this reconciler on the given editor, presenter and highlightings.
     *
     * @param editor
     *            the editor
     * @param sourceViewer
     *            the source viewer
     * @param presenter
     *            the semantic highlighting presenter
     * @param semanticHighlightings
     *            the semantic highlightings
     * @param highlightings
     *            the highlightings
     */
    public void install(TypeScriptEditor editor, ISourceViewer sourceViewer, TypeScriptSemanticPresenter presenter,
            TypeScriptSemanticHighlighting[] semanticHighlightings, Highlighting[] highlightings) {
        fPresenter = presenter;
        fSemanticHighlightings = semanticHighlightings;
        fHighlightings = highlightings;

        fEditor = editor;
        fSourceViewer = sourceViewer;

        fSourceViewer.addTextInputListener(this);
        fEditor.updateSemanticHigliting();
        if (fEditor == null) {
            scheduleJob();
        }
    }

    /**
     * Uninstall this reconciler from the editor
     */
    public void uninstall() {
        if (fPresenter != null) {
            fPresenter.setCanceled(true);
        }

        if (fEditor != null) {
            fSourceViewer.removeTextInputListener(this);
            fEditor = null;
        }

        fSourceViewer = null;
        fSemanticHighlightings = null;
        fHighlightings = null;
        fPresenter = null;
    }

    /**
     * Schedule a background job for retrieving the AST and reconciling the Semantic Highlighting
     * model.
     */
    private void scheduleJob() {

        synchronized (fJobLock) {
            final Job oldJob = fJob;
            if (fJob != null) {
                fJob.cancel();
                fJob = null;
            }

            fJob = new Job("JavaEditorMessages.SemanticHighlighting_job") {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    if (oldJob != null) {
                        try {
                            oldJob.join();
                        } catch (InterruptedException e) {
                            Activator.error(e);
                            return Status.CANCEL_STATUS;
                        }
                    }
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    IFile file = ((FileEditorInput) fEditor.getEditorInput()).getFile();
                    reconciled(file, false, monitor);
                    synchronized (fJobLock) {
                        // allow the job to be gc'ed
                        if (fJob == this) {
                            fJob = null;
                        }
                    }
                    return Status.OK_STATUS;
                }
            };
            fJob.setSystem(true);
            fJob.setPriority(Job.DECORATE);
            fJob.schedule();
        }
    }

    /*
     * @see org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged
     * (org.eclipse.jface .text.IDocument, org.eclipse.jface.text.IDocument)
     */
    @Override
    public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
        synchronized (fJobLock) {
            if (fJob != null) {
                fJob.cancel();
                fJob = null;
            }
        }
    }

    /*
     * @see org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse
     * .jface.text.IDocument , org.eclipse.jface.text.IDocument)
     */
    @Override
    public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
        if (newInput != null) {
            scheduleJob();
        }
    }

    /**
     * Refreshes the highlighting.
     */
    public void refresh() {
        scheduleJob();
    }

}

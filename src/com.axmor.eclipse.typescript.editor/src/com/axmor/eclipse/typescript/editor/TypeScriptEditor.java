package com.axmor.eclipse.typescript.editor;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.TypeScriptAPI;
import com.axmor.eclipse.typescript.core.TypeScriptAPIFactory;
import com.axmor.eclipse.typescript.editor.parser.TypeScriptModelKinds;
import com.google.common.base.Throwables;

/**
 * The actual editor implementation for Eclipse's TypeScript integration.
 * 
 * @author Konstantin Zaitcev
 */
public class TypeScriptEditor extends TextEditor implements IDocumentProcessor {

    /**
     * The preference key for the matching character painter
     */
    public static final String EDITOR_MATCHING_BRACKETS = "matchingBrackets";

    /**
     * The preference key for the color used by the matching character painter
     */
    public static final String EDITOR_MATCHING_BRACKETS_COLOR = "matchingBracketsColor";

    /**
     * An outline page for the editor's content
     */
    private TypeScriptContentOutlinePage contentOutlinePage;

    /**
     * Supports the configuration of projection capabilities
     */
    private ProjectionSupport projectionSupport;

    /**
     * API for bridge integration
     */
    private TypeScriptAPI api;

    /**
     * A flag for selection setting
     */
    private boolean selectionSetFromOutline = false;

    /**
     * A store for annotations to perform folding
     */
    private Annotation[] oldAnnotations;

    /**
     * The projection annotation model
     */
    private ProjectionAnnotationModel annotationModel;

    /**
     * @return the api
     */
    public TypeScriptAPI getApi() {
        return api;
    }

    /**
     * @param api
     *            the api to set
     */
    public void setApi(TypeScriptAPI api) {
        this.api = api;
    }

    /**
     * Selection changed listener for the outline view.
     */
    private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
            selectionSetFromOutline = false;
            doSelectionChanged(event);
            selectionSetFromOutline = true;
        }
    };

    /**
     * A constructor
     */
    public TypeScriptEditor() {
        super();
        setSourceViewerConfiguration(new TypeScriptEditorConfiguration());
    }

    @Override
    protected void doSetInput(IEditorInput input) throws CoreException {
        super.doSetInput(input);
        final IFile file = ((FileEditorInput) input).getFile();
        api = TypeScriptAPIFactory.getTypeScriptAPI(file.getProject());
        setOutlinePageInput(file);
        IDocument doc = getDocumentProvider().getDocument(input);
        DelayedDocumentListener listener = new DelayedDocumentListener(file, doc);
        listener.addDocumentProcessor(this);
        doc.addDocumentListener(listener);
        ((TypeScriptEditorConfiguration) getSourceViewerConfiguration()).setFile(file);
        ((TypeScriptEditorConfiguration) getSourceViewerConfiguration()).setEditor(this);
    }

    /**
     * Sets a model for outline page
     * 
     * @param file
     *            file corresponding to required model
     */
    private void setOutlinePageInput(IFile file) {
        if (contentOutlinePage != null) {
            contentOutlinePage.setPageInput(api.getScriptModel(file));
        }
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
        if (key.equals(IContentOutlinePage.class)) {
            return getOutlinePage();
        } else {
            return super.getAdapter(key);
        }
    }

    /**
     * Gets a document partitioning
     * 
     * @return the configured partitioning
     */
    public String getConfiguredDocumentPartitioning() {
        return ((TypeScriptEditorConfiguration) getSourceViewerConfiguration()).getConfiguredDocumentPartitioning(this
                .getSourceViewer());
    }

    /**
     * Gets an outline page
     * 
     * @return an outline page
     */
    private TypeScriptContentOutlinePage getOutlinePage() {
        if (contentOutlinePage == null) {
            contentOutlinePage = new TypeScriptContentOutlinePage();
            contentOutlinePage.addPostSelectionChangedListener(selectionChangedListener);
            setOutlinePageInput(((FileEditorInput) getEditorInput()).getFile());
        }
        return contentOutlinePage;
    }

    /**
     * Performs actions for outline navigation
     * 
     * @param event
     *            a selection event
     */
    private void doSelectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();

        JSONObject selectedTSElement = (JSONObject) selection.getFirstElement();
        if (selectedTSElement != null) {
            setSelection(selectedTSElement, true);
        }
    }

    /**
     * Highlights and moves to a corresponding element in editor
     * 
     * @param reference
     *            corresponding entity in editor
     * @param moveCursor
     *            if true, moves cursor to the reference
     */
    private void setSelection(JSONObject reference, boolean moveCursor) {
        if (selectionSetFromOutline) {
            // the work has all just been done via a selection setting in the outline
            selectionSetFromOutline = false;
            return;
        }
        if (reference == null) {
            return;
        }

        if (moveCursor) {
            markInNavigationHistory();
        }

        ISourceViewer sourceViewer = getSourceViewer();
        if (sourceViewer == null) {
            return;
        }
        StyledText textWidget = sourceViewer.getTextWidget();
        if (textWidget == null) {
            return;
        }
        try {
            int offset = Integer.parseInt(reference.getString("minChar"));
            if (offset < 0) {
                return;
            }
            int length = Integer.parseInt(reference.getString("limChar")) - offset;
            if (length < 0) {
                return;
            }

            textWidget.setRedraw(false);

            if (length > 0) {
                setHighlightRange(offset, length, moveCursor);
            }

            if (!moveCursor) {
                return;
            }

            if (offset > -1 && length > 0) {
                sourceViewer.revealRange(offset, length);
                // Selected region begins one index after offset
                sourceViewer.setSelectedRange(offset, length);
                markInNavigationHistory();
            }
        } catch (JSONException e) {
            throw Throwables.propagate(e);
        } finally {
            textWidget.setRedraw(true);
        }
    }

    @Override
    public void processDocument(IFile file, IDocument doc) {
        api.updateFileContent(file, doc.get());
        ((TypeScriptEditorConfiguration) getSourceViewerConfiguration()).setFile(file);
        ((TypeScriptEditorConfiguration) getSourceViewerConfiguration()).setEditor(this);
        if (contentOutlinePage != null) {
            JSONArray model = api.getScriptModel(file);
            contentOutlinePage.refresh(model);
            ArrayList<Position> positions = getPositions(model);
            updateFoldingStructure(positions);
        }
    }

    /**
     * Gets positions for folding update
     * 
     * @param model
     *            a document model
     * @return a list of positions
     */
    private ArrayList<Position> getPositions(JSONArray model) {
        ArrayList<Position> positions = new ArrayList<>();
        for (int i = 0; i < model.length(); i++) {
            if (!model.isNull(i)) {
                try {
                    if (model.get(i) instanceof JSONObject) {
                        JSONObject obj = (JSONObject) model.get(i);
                        String kind = obj.getString("kind");
                        if (!kind.isEmpty() && !kind.equals(TypeScriptModelKinds.Kinds.PROPERTY.toString())
                                && !kind.equals(TypeScriptModelKinds.Kinds.VAR.toString())) {
                            int offset = Integer.parseInt(obj.getString("minChar"));
                            positions.add(new Position(offset, Integer.parseInt(obj.getString("limChar")) - offset));
                        }
                    }
                } catch (JSONException e) {
                    throw Throwables.propagate(e);
                }
            }
        }
        return positions;
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();

        projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
        projectionSupport.install();
        projectionSupport.setHoverControlCreator(new IInformationControlCreator() {
            @Override
            public IInformationControl createInformationControl(Shell parent) {
                return new DefaultInformationControl(parent, true);
            }
        });

        // turn projection mode on
        viewer.doOperation(ProjectionViewer.TOGGLE);

        annotationModel = viewer.getProjectionAnnotationModel();
        ArrayList<Position> positions = 
                getPositions(api.getScriptModel(((FileEditorInput) getEditorInput()).getFile()));
        updateFoldingStructure(positions);
    }

    @Override
    protected void initializeKeyBindingScopes() {
        setKeyBindingScopes(new String[] { "com.axmor.eclipse.typescript.editor.TypeScriptEditorScope" }); //$NON-NLS-1$
    }

    @Override
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        ISourceViewer viewer = 
                new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);

        // ensure decoration support has been created and configured.
        getSourceViewerDecorationSupport(viewer);

        return viewer;
    }

    /**
     * Updates folding structure
     * 
     * @param positions a list of positions
     */
    public void updateFoldingStructure(ArrayList<Position> positions) {
        Annotation[] annotations = new Annotation[positions.size()];

        // this will hold the new annotations along
        // with their corresponding positions
        HashMap<ProjectionAnnotation, Position> newAnnotations = new HashMap<ProjectionAnnotation, Position>();

        for (int i = 0; i < positions.size(); i++) {
            ProjectionAnnotation annotation = new ProjectionAnnotation();
            newAnnotations.put(annotation, positions.get(i));
            annotations[i] = annotation;
        }

        annotationModel.modifyAnnotations(oldAnnotations, newAnnotations, null);

        oldAnnotations = annotations;
    }

    @Override
    protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
        super.configureSourceViewerDecorationSupport(support);

        char[] matchChars = { '(', ')', '[', ']', '{', '}' }; // which brackets to match
        ICharacterPairMatcher matcher = new DefaultCharacterPairMatcher(matchChars,
                IDocumentExtension3.DEFAULT_PARTITIONING);
        support.setCharacterPairMatcher(matcher);
        support.setMatchingCharacterPainterPreferenceKeys(EDITOR_MATCHING_BRACKETS, EDITOR_MATCHING_BRACKETS_COLOR);

        // Enable bracket highlighting in the preference store
        IPreferenceStore store = getPreferenceStore();
        store.setDefault(EDITOR_MATCHING_BRACKETS, true);
        store.setDefault(EDITOR_MATCHING_BRACKETS_COLOR, "128,128,128");
    }

}

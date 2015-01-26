package com.axmor.eclipse.typescript.editor;

import static com.axmor.eclipse.typescript.editor.TypeScriptEditorUtils.getPosition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.TypeScriptAPI;
import com.axmor.eclipse.typescript.core.TypeScriptAPIFactory;
import com.axmor.eclipse.typescript.core.TypeScriptResources;
import com.axmor.eclipse.typescript.core.TypeScriptUtils;
import com.axmor.eclipse.typescript.editor.actions.ToggleMarkOccurrencesAction;
import com.axmor.eclipse.typescript.editor.compare.TypeScriptBracketInserter;
import com.axmor.eclipse.typescript.editor.occurrence.OccurrencesFinderJob;
import com.axmor.eclipse.typescript.editor.occurrence.OccurrencesFinderJobCanceler;
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

	/** Constant for marker type. */
	private static final String MARKER_TYPE = "com.axmor.eclipse.typescript.editor.tsDiagnostic";

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
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			selectionSetFromOutline = false;
			doSelectionChanged(event);
			selectionSetFromOutline = true;
		}
	};

	private class EditorSelectionChangedListener implements ISelectionChangedListener {

		public void install(ISelectionProvider selectionProvider) {
			if (selectionProvider == null) {
				return;
			}

			if (selectionProvider instanceof IPostSelectionProvider) {
				IPostSelectionProvider provider = (IPostSelectionProvider) selectionProvider;
				provider.addPostSelectionChangedListener(this);
			} else {
				selectionProvider.addSelectionChangedListener(this);
			}
		}

		public void uninstall(ISelectionProvider selectionProvider) {
			if (selectionProvider == null) {
				return;
			}

			if (selectionProvider instanceof IPostSelectionProvider) {
				IPostSelectionProvider provider = (IPostSelectionProvider) selectionProvider;
				provider.removePostSelectionChangedListener(this);
			} else {
				selectionProvider.removeSelectionChangedListener(this);
			}
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection = event.getSelection();
			if (selection instanceof ITextSelection) {
				ITextSelection textSelection = (ITextSelection) selection;
				updateOccurrenceAnnotations(textSelection);
			}
		}
	}

	private EditorSelectionChangedListener editorSelectionChangedListener;

	private ShellAdapter activationListener = new ShellAdapter() {
		@Override
		public void shellActivated(ShellEvent e) {
			if (fMarkOccurrenceAnnotations && isActivePart()) {
				ISelection selection = getSelectionProvider().getSelection();
				if (selection instanceof ITextSelection) {
					fForcedMarkOccurrencesSelection = (ITextSelection) selection;
					updateOccurrenceAnnotations(fForcedMarkOccurrencesSelection);
				}
			}
		}

		@Override
		public void shellDeactivated(ShellEvent e) {
			if (fMarkOccurrenceAnnotations && isActivePart())
				removeOccurrenceAnnotations();
		}
	};

	private boolean fMarkOccurrenceAnnotations;

	public Annotation[] fOccurrenceAnnotations;

	private OccurrencesFinderJob fOccurrencesFinderJob;

	private OccurrencesFinderJobCanceler fOccurrencesFinderJobCanceler;

	private ITextSelection fForcedMarkOccurrencesSelection;
	
	private TypeScriptBracketInserter fBracketInserter;
	
	private IPropertyChangeListener propertyChangedListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			TypeScriptEditorConfiguration sourceViewerConfiguration= (TypeScriptEditorConfiguration)getSourceViewerConfiguration();
	        if (sourceViewerConfiguration != null) {
	        	sourceViewerConfiguration.adaptToPreferenceChange(event);
	        	getViewer().invalidateTextPresentation();
	        }	        
		}
	};
	
	private IPropertyChangeListener propertyBracketsChangedListener = new IPropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent event) {            
            if (event.getProperty().equals("insertCloseBrackets")) {
                fBracketInserter.setInsertCloseBrackets((boolean) event.getNewValue());
            }
        }
    };

	/**
	 * A constructor
	 */
	public TypeScriptEditor() {
		super();
		setRulerContextMenuId("#TypeScriptEditorRulerContext"); //$NON-NLS-1$
		setEditorContextMenuId("#TypeScriptEditorContext"); //$NON-NLS-1$
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
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(propertyChangedListener);
		com.axmor.eclipse.typescript.core.Activator.getDefault().getPreferenceStore()
		    .addPropertyChangeListener(propertyBracketsChangedListener);
	}

	@Override
	public boolean isEditable() {
		return getEditorInput().getName().endsWith(TypeScriptResources.TS_STD_LIB) ? false : super.isEditable();
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
	public TypeScriptContentOutlinePage getOutlinePage() {
		if (contentOutlinePage == null) {
			contentOutlinePage = new TypeScriptContentOutlinePage();
			contentOutlinePage.addPostSelectionChangedListener(selectionChangedListener);
			setOutlinePageInput(((FileEditorInput) getEditorInput()).getFile());
		}
		return contentOutlinePage;
	}

	/**
	 * Returns current source viewer
	 * 
	 * @return source viewer
	 */
	public ISourceViewer getViewer() {
		return getSourceViewer();
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
			if (selection instanceof ITextSelection) {
				ITextSelection textSelection = (ITextSelection) selection;
				updateOccurrenceAnnotations(textSelection);
			}
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
			// the work has all just been done via a selection setting in the
			// outline
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

			Position pos = getPosition(reference);
			if (pos.offset < 0 || pos.length < 0) {
				return;
			}
			textWidget.setRedraw(false);

			String documentPart = sourceViewer.getDocument().get(pos.offset, pos.length);

			// Try to find name because position returns for whole block
			String name = reference.getString(TypeScriptUtils.isTypeScriptLegacyVersion() ? "name" : "text");
			if (name != null) {
				int nameoffset = documentPart.indexOf(name);
				if (nameoffset != -1) {
					pos.offset += nameoffset;
					pos.length = name.length();
				}
			}
			if (pos.length > 0) {
				setHighlightRange(pos.offset, pos.length, moveCursor);
			}

			if (!moveCursor) {
				return;
			}

			if (pos.offset > -1 && pos.length > 0) {
				sourceViewer.revealRange(pos.offset, pos.length);
				// Selected region begins one index after offset
				sourceViewer.setSelectedRange(pos.offset, pos.length);
				markInNavigationHistory();
			}
		} catch (JSONException | BadLocationException e) {
			throw Throwables.propagate(e);
		} finally {
			textWidget.setRedraw(true);
		}
	}

	@Override
	public void processDocument(IFile file, IDocument doc) {
		api.updateFileContent(file, doc.get());
		((TypeScriptEditorConfiguration) getSourceViewerConfiguration())
				.setFile(file);
		((TypeScriptEditorConfiguration) getSourceViewerConfiguration())
				.setEditor(this);
		if (contentOutlinePage != null) {
			JSONArray model = api.getScriptModel(file);
			contentOutlinePage.refresh(model);
			ArrayList<Position> positions = getPositions(model);
			updateFoldingStructure(positions);
			try {
				for (IResource resource : file.getProject().members()) {
					if ((resource instanceof IFile)
							&& (TypeScriptResources.isTypeScriptFile(resource
									.getName()))) {
						IFile currentFile = (IFile) resource;
						try {
							JSONArray diagnostics = api
									.getSemanticDiagnostics(currentFile);
							if (diagnostics != null) {
								currentFile.deleteMarkers(MARKER_TYPE, true,
										IResource.DEPTH_INFINITE);
								for (int i = 0; i < diagnostics.length(); i++) {
									JSONObject diagnostic = diagnostics
											.getJSONObject(i);
									IMarker marker = currentFile
											.createMarker(MARKER_TYPE);
									String message = "";
									if (TypeScriptUtils
											.isTypeScriptLegacyVersion()) {
										message = diagnostic
												.getString("diagnosticCode");
										if (diagnostic.has("arguments")) {
											JSONArray arguments = diagnostic
													.getJSONArray("arguments");
											for (int j = 0; j < arguments
													.length(); j++) {
												message = message
														.replaceAll(
																"\\{" + j
																		+ "\\}",
																Matcher.quoteReplacement(arguments
																		.getString(j)));
											}
										}
									} else {
										message = diagnostic.getString("messageText");
									}
									marker.setAttribute(IMarker.MESSAGE,
											message);
									marker.setAttribute(IMarker.SEVERITY,
											IMarker.SEVERITY_ERROR);

									marker.setAttribute(IMarker.CHAR_START,
											diagnostic.getInt("start"));
									marker.setAttribute(
											IMarker.CHAR_END,
											diagnostic.getInt("start")
													+ diagnostic
															.getInt("length"));
								}
							}
						} catch (JSONException | CoreException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		if (TypeScriptUtils.isTypeScriptLegacyVersion()) {
			for (int i = 0; i < model.length(); i++) {
				if (!model.isNull(i)) {
					try {
						if (model.get(i) instanceof JSONObject) {
							JSONObject obj = (JSONObject) model.get(i);
							String kind = obj.getString("kind");
							if (!kind.isEmpty() && !kind.equals(TypeScriptModelKinds.Kinds.PROPERTY.toString())
									&& !kind.equals(TypeScriptModelKinds.Kinds.VAR.toString())) {
								int offset = Integer.parseInt(obj.getString("minChar"));
								positions
										.add(new Position(offset, Integer.parseInt(obj.getString("limChar")) - offset));
							}
						}
					} catch (JSONException e) {
						throw Throwables.propagate(e);
					}
				}
			}
		} else {
			addChildPositions(positions, model);
		}
		return positions;
	}

	private void addChildPositions(List<Position> positions, JSONArray childItems) {
		for (int i = 0; i < childItems.length(); i++) {
			if (!childItems.isNull(i)) {
				try {
					if (childItems.get(i) instanceof JSONObject) {
						JSONObject item = (JSONObject) childItems.get(i);
						String kind = item.getString("kind");
						if (!kind.isEmpty() && !kind.equals(TypeScriptModelKinds.Kinds.PROPERTY.toString())
								&& !kind.equals(TypeScriptModelKinds.Kinds.VAR.toString())) {
							if (item.has("spans")) {
								JSONArray spans = (JSONArray) item.get("spans");
								if (spans != null && spans.length() > 0) {
									JSONObject span = (JSONObject) spans.get(0);
									positions.add(new Position(span.getInt("start"), span.getInt("length")));
								}
							}
						}
						if (item.has("childItems") && !item.isNull("childItems")
								&& item.get("childItems") instanceof JSONArray) {
							addChildPositions(positions, (JSONArray) item.get("childItems"));
						}
					}
				} catch (JSONException e) {
					throw Throwables.propagate(e);
				}

			}
		}
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
		ArrayList<Position> positions = getPositions(api.getScriptModel(((FileEditorInput) getEditorInput()).getFile()));
		updateFoldingStructure(positions);
		if (fMarkOccurrenceAnnotations) {
			installOccurrencesFinder();
		}
		getEditorSite().getShell().addShellListener(activationListener);

		editorSelectionChangedListener = new EditorSelectionChangedListener();
		editorSelectionChangedListener.install(getSelectionProvider());
		
		fBracketInserter = new TypeScriptBracketInserter();
		fBracketInserter.setInsertCloseBrackets(com.axmor.eclipse.typescript.core.Activator.getDefault()
		        .getPreferenceStore().getBoolean("insertCloseBrackets"));
		viewer.prependVerifyKeyListener(fBracketInserter);
		fBracketInserter.setViewer(viewer);
	}

	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "com.axmor.eclipse.typescript.editor.TypeScriptEditorScope" }); //$NON-NLS-1$
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		ISourceViewer viewer = new TypeScriptProjectionViewer(this, parent, ruler, getOverviewRuler(),
				isOverviewRulerVisible(), styles);

		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}

	/**
	 * Updates folding structure
	 * 
	 * @param positions
	 *            a list of positions
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

		char[] matchChars = { '(', ')', '[', ']', '{', '}' }; // which brackets
																// to match
		ICharacterPairMatcher matcher = new DefaultCharacterPairMatcher(matchChars,
				IDocumentExtension3.DEFAULT_PARTITIONING);
		support.setCharacterPairMatcher(matcher);
		support.setMatchingCharacterPainterPreferenceKeys(EDITOR_MATCHING_BRACKETS, EDITOR_MATCHING_BRACKETS_COLOR);

		// Enable bracket highlighting in the preference store
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(EDITOR_MATCHING_BRACKETS, true);
		store.setDefault(EDITOR_MATCHING_BRACKETS_COLOR, "128,128,128");
	}

	public boolean isMarkingOccurrences() {
		return fMarkOccurrenceAnnotations;
	}

	@Override
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		String property = event.getProperty();

		if (ToggleMarkOccurrencesAction.TOGGLE_MARK_OCCURRENCE.equals(property)) {
			boolean newBooleanValue = Boolean.valueOf(event.getNewValue().toString()).booleanValue();
			if (newBooleanValue != fMarkOccurrenceAnnotations) {
				fMarkOccurrenceAnnotations = newBooleanValue;
				if (fMarkOccurrenceAnnotations) {
					installOccurrencesFinder();
				} else {
					uninstallOccurrencesFinder();
				}
			}
			return;
		}		
		super.handlePreferenceStoreChanged(event);
	}

	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		fMarkOccurrenceAnnotations = getPreferenceStore()
				.getBoolean(ToggleMarkOccurrencesAction.TOGGLE_MARK_OCCURRENCE);
	}

	@Override
	public void dispose() {
		if (editorSelectionChangedListener != null) {
			editorSelectionChangedListener.uninstall(getSelectionProvider());
			editorSelectionChangedListener = null;
		}

		uninstallOccurrencesFinder();

		if (activationListener != null) {
			Shell shell = getEditorSite().getShell();
			if (shell != null && !shell.isDisposed()) {
				shell.removeShellListener(activationListener);
			}
			activationListener = null;
		}
		
		if (propertyChangedListener != null) {
			getPreferenceStore().removePropertyChangeListener(propertyChangedListener);
			propertyChangedListener = null;
		}
		
		if (fBracketInserter != null) {
		    ((TextViewer) fBracketInserter.getViewer()).removeVerifyKeyListener(fBracketInserter);
        }
		super.dispose();
	}

	private void uninstallOccurrencesFinder() {
		fMarkOccurrenceAnnotations = false;

		if (fOccurrencesFinderJob != null) {
			fOccurrencesFinderJob.cancel();
			fOccurrencesFinderJob = null;
		}

		if (fOccurrencesFinderJobCanceler != null) {
			fOccurrencesFinderJobCanceler.uninstall();
			fOccurrencesFinderJobCanceler = null;
		}

		removeOccurrenceAnnotations();
	}

	private void installOccurrencesFinder() {
		fMarkOccurrenceAnnotations = true;

		if (getSelectionProvider() != null) {
			ISelection selection = getSelectionProvider().getSelection();
			if (selection instanceof ITextSelection) {
				fForcedMarkOccurrencesSelection = (ITextSelection) selection;
				updateOccurrenceAnnotations(fForcedMarkOccurrencesSelection);
			}
		}
		if (fOccurrencesFinderJobCanceler == null) {
			fOccurrencesFinderJobCanceler = new OccurrencesFinderJobCanceler(this, fOccurrencesFinderJob);
			fOccurrencesFinderJobCanceler.install();
		}
	}

	private void updateOccurrenceAnnotations(ITextSelection selection) {
		if (fOccurrencesFinderJob != null)
			fOccurrencesFinderJob.cancel();

		if (!fMarkOccurrenceAnnotations) {
			return;
		}

		if (selection == null) {
			return;
		}

		IFile file = ((FileEditorInput) getEditorInput()).getFile();
		JSONArray occurrences = api.getOccurrencesAtPosition(file, selection.getOffset());

		if (occurrences == null || occurrences.length() == 0) {
			removeOccurrenceAnnotations();
			return;
		}

		List<Position> positions = new ArrayList<>(occurrences.length());
		try {
			for (int i = 0; i < occurrences.length(); i++) {
				positions.add(getPosition(occurrences.getJSONObject(i)));
			}
		} catch (JSONException e) {
			Activator.error(e);
		}

		fOccurrencesFinderJob = new OccurrencesFinderJob(this, getViewer().getDocument(), positions, selection);
		fOccurrencesFinderJob.run(new NullProgressMonitor());
	}

	public void removeOccurrenceAnnotations() {
		IDocumentProvider documentProvider = getDocumentProvider();
		if (documentProvider == null) {
			return;
		}

		IAnnotationModel annotationModel = documentProvider.getAnnotationModel(getEditorInput());
		if (annotationModel == null || fOccurrenceAnnotations == null) {
			return;
		}

		updateAnnotationModelForRemoves(annotationModel);
	}

	public IPreferenceStore getEditorPreferenceStore() {
		return getPreferenceStore();
	}

	private void updateAnnotationModelForRemoves(IAnnotationModel annotationModel) {
		if (annotationModel instanceof IAnnotationModelExtension) {
			((IAnnotationModelExtension) annotationModel).replaceAnnotations(fOccurrenceAnnotations, null);
		} else {
			for (int i = 0, length = fOccurrenceAnnotations.length; i < length; i++) {
				annotationModel.removeAnnotation(fOccurrenceAnnotations[i]);
			}
		}
		fOccurrenceAnnotations = null;
	}

	private boolean isActivePart() {
		IWorkbenchPart part = getActivePart();
		return part != null && part.equals(this);
	}

	private IWorkbenchPart getActivePart() {
		IWorkbenchWindow window = getSite().getWorkbenchWindow();
		IPartService service = window.getPartService();
		return service.getActivePart();
	}
}

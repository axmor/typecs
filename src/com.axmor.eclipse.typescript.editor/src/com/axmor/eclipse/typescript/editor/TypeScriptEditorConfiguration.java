package com.axmor.eclipse.typescript.editor;

import static org.eclipse.jface.text.IDocument.DEFAULT_CONTENT_TYPE;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

import com.axmor.eclipse.typescript.editor.contentassist.TypeScriptAssistProcessor;
import com.axmor.eclipse.typescript.editor.hover.TypeScriptTextHover;
import com.axmor.eclipse.typescript.editor.parser.TypeScriptPartitionScanner;
import com.axmor.eclipse.typescript.editor.parser.TypeScriptSyntaxScanner;

/**
 * Source viewer configuration for the TypeScript text editor.
 * 
 * @author Konstantin Zaitcev
 */
public class TypeScriptEditorConfiguration extends TextSourceViewerConfiguration {

    /**
     * A content assistant for code completion
     */
    private ContentAssistant assistant;

    /**
     * A current file
     */
    private IFile file;

    /**
     * TypeScript editor
     */
    private TypeScriptEditor editor;
    
    /**
     * TypeScript syntax scanner
     */
    private TypeScriptSyntaxScanner syntaxScanner;

    /**
     * An outline presenter to implement quick outline functionality
     */
    private InformationPresenter outlinePresenter;

    /**
     * @param file
     *            the file to set
     */
    public void setFile(IFile file) {
        this.file = file;
    }

    /**
     * @param editor
     *            the editor to set
     */
    public void setEditor(TypeScriptEditor editor) {
        this.editor = editor;
    }
    
    private TypeScriptSyntaxScanner getSyntaxScanner() {
    	if (syntaxScanner == null ) {
    		syntaxScanner = new TypeScriptSyntaxScanner();
    	}
    	return syntaxScanner;
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new PresentationReconciler();
        reconciler.setDocumentPartitioning(IDocumentExtension3.DEFAULT_PARTITIONING);

        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getSyntaxScanner());
        reconciler.setDamager(dr, DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, DEFAULT_CONTENT_TYPE);

        dr = new DefaultDamagerRepairer(getSyntaxScanner());
        reconciler.setDamager(dr, TypeScriptPartitionScanner.TS_JAVA_DOC);
        reconciler.setRepairer(dr, TypeScriptPartitionScanner.TS_JAVA_DOC);

        dr = new DefaultDamagerRepairer(getSyntaxScanner());
        reconciler.setDamager(dr, TypeScriptPartitionScanner.TS_COMMENT);
        reconciler.setRepairer(dr, TypeScriptPartitionScanner.TS_COMMENT);

        dr = new DefaultDamagerRepairer(getSyntaxScanner());
        reconciler.setDamager(dr, TypeScriptPartitionScanner.TS_REFERENCE);
        reconciler.setRepairer(dr, TypeScriptPartitionScanner.TS_REFERENCE);

        return reconciler;
    }

    /**
     * Creates presenter to show quick outline dialog at the text viewer's current document
     * position.
     * 
     * @param sourceViewer
     *            current viewer
     * @return presenter
     */
    public IInformationPresenter getOutlinePresenter(ISourceViewer sourceViewer) {
        if (editor.getOutlinePage() == null) {
            return null;
        }
        if (outlinePresenter != null) {
            return outlinePresenter;
        }
        // Define a new outline presenter
        outlinePresenter = new InformationPresenter(getOutlinePresenterControlCreator(sourceViewer));
        outlinePresenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
        outlinePresenter.setAnchor(AbstractInformationControlManager.ANCHOR_GLOBAL);
        // Define a new outline provider
        IInformationProvider provider = new TypeScriptSourceInfoProvider(editor);
        // Set the provider on all defined content types
        String[] contentTypes = getConfiguredContentTypes(sourceViewer);
        for (int i = 0; i < contentTypes.length; i++) {
            outlinePresenter.setInformationProvider(provider, contentTypes[i]);
        }
        // Set the presenter size constraints
        outlinePresenter.setSizeConstraints(50, 20, true, false);

        return outlinePresenter;
    }

    /**
     * Returns the outline presenter control creator. The creator is a factory creating outline
     * presenter controls for the given source viewer.
     * 
     * @param sourceViewer
     *            the source viewer to be configured by this configuration
     * @return an information control creator
     */
    private IInformationControlCreator getOutlinePresenterControlCreator(ISourceViewer sourceViewer) {
        return new IInformationControlCreator() {
            @Override
			public IInformationControl createInformationControl(Shell parent) {
                int shellStyle = SWT.RESIZE;
                TypeScriptQuickOutlineDialog dialog = new TypeScriptQuickOutlineDialog(parent, shellStyle, editor);
                return dialog;
            }
        };
    }

    @Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        String[] tsTypes = new String[TypeScriptPartitionScanner.TS_PARTITION_TYPES.length + 1];
        for (int i = 0; i < tsTypes.length - 1; i++) {
            tsTypes[i] = TypeScriptPartitionScanner.TS_PARTITION_TYPES[i];
        }
        tsTypes[TypeScriptPartitionScanner.TS_PARTITION_TYPES.length] = IDocument.DEFAULT_CONTENT_TYPE;
        return tsTypes;
    }

    @Override
    public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
        return new String[] { "//", "" }; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
        return new DefaultAnnotationHover();
    }

    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
    	if (editor != null && assistant == null) {
    		assistant = new ContentAssistant();
            assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
            assistant.setContentAssistProcessor(new TypeScriptAssistProcessor(editor.getApi(), file),
                    IDocument.DEFAULT_CONTENT_TYPE);
            assistant.enableAutoActivation(true);
            assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
            assistant.enableAutoInsert(true);
            assistant.enableColoredLabels(true);
            assistant.setShowEmptyList(true);
			assistant.setInformationControlCreator(new IInformationControlCreator() {
				@Override
				public IInformationControl createInformationControl(Shell parent) {
					return new DefaultInformationControl(parent, true);
				}
			});
    	}       

        return assistant;
    }

    @Override
    public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
        IHyperlinkDetector[] registeredDetectors = getRegisteredHyperlinkDetectors(sourceViewer);
        IHyperlinkDetector[] result = new IHyperlinkDetector[registeredDetectors.length + 1];
        int i = 0;
        for (IHyperlinkDetector detector : registeredDetectors) {
            result[i] = detector;
            i++;
        }
        result[result.length - 1] = new URLHyperlinkDetector();
        return result;
    }

    @Override
    protected Map<String, IAdaptable> getHyperlinkDetectorTargets(ISourceViewer sourceViewer) {
        @SuppressWarnings("unchecked")
        Map<String, IAdaptable> targets = super.getHyperlinkDetectorTargets(sourceViewer);
        targets.put("com.axmor.eclipse.typescript.sourceFiles", editor); //$NON-NLS-1$
        return targets;
    }
    
	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new TypeScriptTextHover(sourceViewer);
	}
	
	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
	    return TypeScriptPartitionScanner.TS_COMMENT.equals(contentType) ? new IAutoEditStrategy[] { 
	        new DefaultIndentLineAutoEditStrategy() } : new IAutoEditStrategy[] { new TypeScriptAutoIndentStrategy() };
	    }

    /**
	 * Preference colors have changed.  
	 * Update the default tokens of the scanners.
	 */
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		if (syntaxScanner == null) {
			return; //property change before the editor is fully created
		}
		syntaxScanner.adaptToPreferenceChange(event);		
	}
}

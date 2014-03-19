package com.axmor.eclipse.typescript.editor;

import static org.eclipse.jface.text.IDocument.DEFAULT_CONTENT_TYPE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.editor.parser.TypeScriptModelKinds;
import com.axmor.eclipse.typescript.editor.parser.TypeScriptPartitionScanner;
import com.axmor.eclipse.typescript.editor.parser.TypeScriptSyntaxScanner;
import com.google.common.base.Throwables;

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

    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new PresentationReconciler();
        reconciler.setDocumentPartitioning(IDocumentExtension3.DEFAULT_PARTITIONING);

        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(new TypeScriptSyntaxScanner());
        reconciler.setDamager(dr, DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, DEFAULT_CONTENT_TYPE);

        dr = new DefaultDamagerRepairer(new TypeScriptSyntaxScanner());
        reconciler.setDamager(dr, TypeScriptPartitionScanner.TS_JAVA_DOC);
        reconciler.setRepairer(dr, TypeScriptPartitionScanner.TS_JAVA_DOC);

        dr = new DefaultDamagerRepairer(new TypeScriptSyntaxScanner());
        reconciler.setDamager(dr, TypeScriptPartitionScanner.TS_COMMENT);
        reconciler.setRepairer(dr, TypeScriptPartitionScanner.TS_COMMENT);

        dr = new DefaultDamagerRepairer(new TypeScriptSyntaxScanner());
        reconciler.setDamager(dr, TypeScriptPartitionScanner.TS_REFERENCE);
        reconciler.setRepairer(dr, TypeScriptPartitionScanner.TS_REFERENCE);

        return reconciler;
    }

    /**
     * Creates presenter to show quick outline dialog at the text viewer's
     * current document position.
     *  
     * @param sourceViewer current viewer
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
     * Returns the outline presenter control creator. The creator is a 
     * factory creating outline presenter controls for the given source viewer. 
     *
     * @param sourceViewer the source viewer to be configured by this configuration
     * @return an information control creator
     */
    private IInformationControlCreator getOutlinePresenterControlCreator(ISourceViewer sourceViewer) {
        return new IInformationControlCreator() {
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

    // @Override
    // public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
    // return new TypeScriptTextHover();
    // //return super.getTextHover(sourceViewer, contentType);
    // }

    @Override
    public ContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        if (assistant == null) {
            assistant = new ContentAssistant();
            assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
            assistant.setContentAssistProcessor(new TypeScriptAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
            assistant.enableAutoActivation(true);
            assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
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

    /**
     * A content assist processor which computes completions and sets code completion preferences
     * 
     * @author Asya Vorobyova
     */
    private class TypeScriptAssistProcessor implements IContentAssistProcessor {

        @Override
        public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
            JSONObject completionList = editor.getApi().getCompletion(file, offset);
            try {
                TypeScriptUIImages imagesFactory = new TypeScriptUIImages();
                String replacement = extractPrefix(viewer.getDocument().get(), offset);
                if (!completionList.has("entries")) {
                    return new ICompletionProposal[0];
                }
                JSONArray completions = completionList.getJSONArray("entries");
                int completionsLength = completions.length();
                List<ICompletionProposal> result = new ArrayList<ICompletionProposal>(completionsLength);
                for (int i = 0; i < completionsLength; i++) {
                    if (completions.getJSONObject(i).getString("name").startsWith(replacement)) {
                        String entryName = completions.getJSONObject(i).getString("name");
                        JSONObject details = editor.getApi().getCompletionDetails(file, offset, entryName);
                        String displayString = entryName;
                        if ((details != null)
                                && details.has("kind")
                                && !details.getString("kind").equals(
                                        TypeScriptModelKinds.Kinds.PRIMITIVE_TYPE.toString())
                                && !details.getString("kind").equals(TypeScriptModelKinds.Kinds.KEYWORD.toString())) {
                            if (!details.getString("kind").equals(TypeScriptModelKinds.Kinds.METHOD.toString())
                                    && !details.getString("kind")
                                            .equals(TypeScriptModelKinds.Kinds.FUNCTION.toString())) {
                                displayString += ":";
                            }
                            displayString += details.getString("type");
                            String fullSymbolName = details.getString("fullSymbolName");
                            String[] parts = fullSymbolName.split("\\.");
                            if (parts.length > 1) {
                                String parentName = fullSymbolName.substring(0, fullSymbolName.length()
                                        - parts[parts.length - 1].length() - 1);
                                displayString += " - " + parentName;
                            }
                        }
                        result.add(new CompletionProposal(completions.getJSONObject(i).getString("name"), offset
                                - replacement.length(), replacement.length(), completions.getJSONObject(i)
                                .getString("name").length(), imagesFactory.getImageForModelObject(completions
                                .getJSONObject(i)), displayString, null, null));
                    }
                }
                ICompletionProposal[] resultedArray = new ICompletionProposal[result.size()];
                return result.toArray(resultedArray);

            } catch (JSONException e) {
                throw Throwables.propagate(e);
            }
        }

        /**
         * Calculates word part before a position corresponding to an offset
         * 
         * @param text
         *            a document to get word in
         * @param offset
         *            the given offset
         * @return the word
         */
        private String extractPrefix(String text, int offset) {
            String currentPrefix;
            int startOfWordToken = offset;

            char token = 'a';
            if (startOfWordToken > 0) {
                token = text.charAt(startOfWordToken - 1);
            }

            while (startOfWordToken > 0 && (Character.isJavaIdentifierPart(token)) && !('$' == token)) {
                startOfWordToken--;
                if (startOfWordToken == 0) {
                    break; // word goes right to the beginning of the doc
                }
                token = text.charAt(startOfWordToken - 1);
            }

            if (startOfWordToken != offset) {
                currentPrefix = text.substring(startOfWordToken, offset);
            } else {
                currentPrefix = "";
            }
            return currentPrefix;
        }

        @Override
        public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
            return null;
        }

        @Override
        public char[] getCompletionProposalAutoActivationCharacters() {
            return ".".toCharArray();
        }

        @Override
        public char[] getContextInformationAutoActivationCharacters() {
            return null;
        }

        @Override
        public String getErrorMessage() {
            return null;
        }

        @Override
        public IContextInformationValidator getContextInformationValidator() {
            return null;
        }

    }

    // private class TypeScriptTextHover implements ITextHover, ITextHoverExtension,
    // ITextHoverExtension2 {
    //
    // @Override
    // public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
    // String info = getHoverInfo(textViewer, hoverRegion);
    // return null;
    // }
    //
    // @Override
    // public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
    // JSONObject obj = api.getSignature(file, hoverRegion.getOffset());
    // return null;
    // }
    //
    // @Override
    // public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
    // Point selection= textViewer.getSelectedRange();
    // if (selection.x <= offset && offset < selection.x + selection.y)
    // return new Region(selection.x, selection.y);
    // return new Region(offset, 0);
    // }
    //
    // @Override
    // public IInformationControlCreator getHoverControlCreator() {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // }
}

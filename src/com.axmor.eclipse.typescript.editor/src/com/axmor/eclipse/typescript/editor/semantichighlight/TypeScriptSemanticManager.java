package com.axmor.eclipse.typescript.editor.semantichighlight;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlighting;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import com.axmor.eclipse.typescript.editor.TypeScriptEditor;
import com.axmor.eclipse.typescript.editor.TypeScriptEditorConfiguration;
import com.axmor.eclipse.typescript.editor.color.ColorManager;

@SuppressWarnings("restriction")
public class TypeScriptSemanticManager implements IPropertyChangeListener {

    /**
     * Highlighting.
     */
    static class Highlighting {

        /** Text attribute */
        private TextAttribute fTextAttribute;
		/** Name. */
		private String name;
        /** Enabled state */
        private boolean fIsEnabled;

        /**
         * Initialize with the given text attribute.
         * 
         * @param textAttribute
         *            The text attribute
         * @param isEnabled
         *            the enabled state
         */
		public Highlighting(TextAttribute textAttribute, String name, boolean isEnabled) {
			setTextAttribute(textAttribute);
			setEnabled(isEnabled);
			this.name = name;
        }

        /**
         * @return Returns the text attribute.
         */
        public TextAttribute getTextAttribute() {
            return fTextAttribute;
        }

        /**
		 * @param textAttribute
		 *            The background to set.
		 */
		public void setTextAttribute(TextAttribute textAttribute) {
			fTextAttribute = textAttribute;
		}

		/**
		 * @return the enabled state
		 */
        public boolean isEnabled() {
            return fIsEnabled;
        }

        /**
		 * @param isEnabled
		 *            the new enabled state
		 */
		public void setEnabled(boolean isEnabled) {
			fIsEnabled = isEnabled;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
    }

    /**
     * Highlighted Positions.
     */
    static class HighlightedPosition extends Position {

        /** Highlighting of the position */
        private Highlighting fStyle;

        /** Lock object */
        private Object fLock;

        /**
         * Initialize the styled positions with the given offset, length and foreground color.
         *
         * @param offset
         *            The position offset
         * @param length
         *            The position length
         * @param highlighting
         *            The position's highlighting
         * @param lock
         *            The lock object
         */
        public HighlightedPosition(int offset, int length, Highlighting highlighting, Object lock) {
            super(offset, length);
            fStyle = highlighting;
            fLock = lock;
        }

        /**
         * @return Returns a corresponding style range.
         */
        public StyleRange createStyleRange() {
            int len = getLength();

            TextAttribute textAttribute = fStyle.getTextAttribute();
            int style = textAttribute.getStyle();
            int fontStyle = style & (SWT.ITALIC | SWT.BOLD | SWT.NORMAL);
            StyleRange styleRange = new StyleRange(getOffset(), len, textAttribute.getForeground(),
                    textAttribute.getBackground(), fontStyle);
            styleRange.strikeout = (style & TextAttribute.STRIKETHROUGH) != 0;
            styleRange.underline = (style & TextAttribute.UNDERLINE) != 0;

            return styleRange;
        }

        /**
         * Uses reference equality for the highlighting.
         *
         * @param off
         *            The offset
         * @param len
         *            The length
         * @param highlighting
         *            The highlighting
         * @return <code>true</code> iff the given offset, length and highlighting are equal to the
         *         internal ones.
         */
        public boolean isEqual(int off, int len, Highlighting highlighting) {
            synchronized (fLock) {
                return !isDeleted() && getOffset() == off && getLength() == len && fStyle == highlighting;
            }
        }

        /**
         * Is this position contained in the given range (inclusive)? Synchronizes on position
         * updater.
         *
         * @param off
         *            The range offset
         * @param len
         *            The range length
         * @return <code>true</code> iff this position is not delete and contained in the given
         *         range.
         */
        public boolean isContained(int off, int len) {
            synchronized (fLock) {
                return !isDeleted() && off <= getOffset() && off + len >= getOffset() + getLength();
            }
        }

        public void update(int off, int len) {
            synchronized (fLock) {
                super.setOffset(off);
                super.setLength(len);
            }
        }

        /*
         * @see org.eclipse.jface.text.Position#setLength(int)
         */
        @Override
        public void setLength(int length) {
            synchronized (fLock) {
                super.setLength(length);
            }
        }

        /*
         * @see org.eclipse.jface.text.Position#setOffset(int)
         */
        @Override
        public void setOffset(int offset) {
            synchronized (fLock) {
                super.setOffset(offset);
            }
        }

        /*
         * @see org.eclipse.jface.text.Position#delete()
         */
        @Override
        public void delete() {
            synchronized (fLock) {
                super.delete();
            }
        }

        /*
         * @see org.eclipse.jface.text.Position#undelete()
         */
        @Override
        public void undelete() {
            synchronized (fLock) {
                super.undelete();
            }
        }

        /**
         * @return Returns the highlighting.
         */
        public Highlighting getHighlighting() {
            return fStyle;
        }
    }

    /**
     * Highlighted ranges.
     */
    public static class HighlightedRange extends Region {
        /** The highlighting key as returned by {@link SemanticHighlighting#getPreferenceKey()}. */
		private String fKey;

        /**
         * Initialize with the given offset, length and highlighting key.
         * 
         * @param offset
         *            the offset
         * @param length
         *            the length
         * @param key
         *            the highlighting key as returned by
         *            {@link SemanticHighlighting#getPreferenceKey()}
         */
        public HighlightedRange(int offset, int length, String key) {
            super(offset, length);
            fKey = key;
        }

        /**
         * @return the highlighting key as returned by
         *         {@link SemanticHighlighting#getPreferenceKey()}
         */
        public String getKey() {
            return fKey;
        }

        /*
         * @see org.eclipse.jface.text.Region#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object o) {
            return super.equals(o) && o instanceof HighlightedRange && fKey.equals(((HighlightedRange) o).getKey());
        }

        /*
         * @see org.eclipse.jface.text.Region#hashCode()
         */
        @Override
        public int hashCode() {
            return super.hashCode() | fKey.hashCode();
        }
    }

    private TypeScriptSemanticReconciler fReconciler;
    private TypeScriptEditor fEditor;
    private ISourceViewer fSourceViewer;
    private ColorManager fColorManager;
    private IPreferenceStore fPreferenceStore;
    private TypeScriptSemanticHighlighting[] fSemanticHighlightings;
    private Highlighting[] fHighlightings;
    private TypeScriptEditorConfiguration fConfiguration;
    private TypeScriptPresentationReconciler fPresentationReconciler;
    private TypeScriptSemanticPresenter fPresenter;
    private HighlightedRange[][] fHardcodedRanges;

    public void install(TypeScriptEditor typeScriptEditor, ISourceViewer sourceViewer, ColorManager colorManager,
            IPreferenceStore preferenceStore) {
        fEditor = typeScriptEditor;
        fSourceViewer = sourceViewer;
        fColorManager = colorManager;
        fPreferenceStore = preferenceStore;
        fConfiguration = new TypeScriptEditorConfiguration();
        fPresentationReconciler = (TypeScriptPresentationReconciler) fConfiguration
                .getPresentationReconciler(sourceViewer);
        fPreferenceStore.addPropertyChangeListener(this);
        if (isEnabled()) {
            enable();
        }
    }

    public void install(ISourceViewer sourceViewer, ColorManager colorManager, IPreferenceStore preferenceStore,
            HighlightedRange[][] hardcodedRanges) {
        fHardcodedRanges = hardcodedRanges;
        install(null, sourceViewer, colorManager, preferenceStore);
    }

    /**
     * Enable semantic highlighting.
     */
    private void enable() {
        initializeHighlightings();

        fPresenter = new TypeScriptSemanticPresenter();
        fPresenter.install(fSourceViewer, fPresentationReconciler);

        if (fEditor != null) {
            fReconciler = new TypeScriptSemanticReconciler();
            fReconciler.install(fEditor, fSourceViewer, fPresenter, fSemanticHighlightings, fHighlightings);
        } else {
            fPresenter.updatePresentation(null, createHardcodedPositions(), new HighlightedPosition[0]);
        }
    }

    private HighlightedPosition[] createHardcodedPositions() {
        List<HighlightedPosition> positions = new ArrayList<HighlightedPosition>();
        for (int i = 0; i < fHardcodedRanges.length; i++) {
            HighlightedRange range = null;
            Highlighting hl = null;
            for (int j = 0; j < fHardcodedRanges[i].length; j++) {
                hl = getHighlighting(fHardcodedRanges[i][j].getKey());
                if (hl.isEnabled()) {
                    range = fHardcodedRanges[i][j];
                    break;
                }
            }

            if (range != null) {
                positions.add(fPresenter.createHighlightedPosition(range.getOffset(), range.getLength(), hl));
            }
        }
        return positions.toArray(new HighlightedPosition[positions.size()]);
    }

    /**
     * Returns the highlighting corresponding to the given key.
     */
    private Highlighting getHighlighting(String key) {
        for (int i = 0; i < fSemanticHighlightings.length; i++) {
            TypeScriptSemanticHighlighting semanticHighlighting = fSemanticHighlightings[i];
            if (key.equals(semanticHighlighting.getPreferenceKey())) {
                return fHighlightings[i];
            }
        }
        return null;
    }

    public TypeScriptSemanticReconciler getReconciler() {
        return fReconciler;
    }    

    private boolean isEnabled() {
        return TypeScriptSemanticHighlightings.isEnabled(fPreferenceStore);
    }

    /**
     * Initialize semantic highlighting.
     */
    private void initializeHighlightings() {
        fSemanticHighlightings = TypeScriptSemanticHighlightings.getSemanticHighlightings();
        fHighlightings = new Highlighting[fSemanticHighlightings.length];

        for (int i = 0, n = fSemanticHighlightings.length; i < n; i++) {
            TypeScriptSemanticHighlighting semanticHighlighting = fSemanticHighlightings[i];
            String colorKey = TypeScriptSemanticHighlightings.getColorPreferenceKey(semanticHighlighting);

            String boldKey = TypeScriptSemanticHighlightings.getBoldPreferenceKey(semanticHighlighting);
            int style = fPreferenceStore.getBoolean(boldKey) ? SWT.BOLD : SWT.NORMAL;

            String italicKey = TypeScriptSemanticHighlightings.getItalicPreferenceKey(semanticHighlighting);
            if (fPreferenceStore.getBoolean(italicKey)) {
                style |= SWT.ITALIC;
            }

            boolean isEnabled = fPreferenceStore.getBoolean(TypeScriptSemanticHighlightings
                    .getEnabledPreferenceKey(semanticHighlighting));
            fHighlightings[i] = new Highlighting(new TextAttribute(fColorManager.getColor(PreferenceConverter.getColor(
					fPreferenceStore, colorKey)), null, style), semanticHighlighting.getDisplayName(), isEnabled);
        }
    }

    /**
     * Uninstall the semantic highlighting
     */
    public void uninstall() {
        disable();

        if (fPreferenceStore != null) {
            fPreferenceStore.removePropertyChangeListener(this);
            fPreferenceStore = null;
        }

        fEditor = null;
        fSourceViewer = null;
        fColorManager = null;
        fConfiguration = null;
        fPresentationReconciler = null;
        fHardcodedRanges = null;
    }

    /**
     * Disable semantic highlighting.
     */
    private void disable() {
        if (fReconciler != null) {
            fReconciler.uninstall();
            fReconciler = null;
        }

        if (fPresenter != null) {
            fPresenter.uninstall();
            fPresenter = null;
        }

        if (fSemanticHighlightings != null) {
            disposeHighlightings();
        }
    }

    /**
     * Dispose the semantic highlightings.
     */
    private void disposeHighlightings() {      
        fSemanticHighlightings = null;
        fHighlightings = null;
    }

    /*
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.
     * PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        handlePropertyChangeEvent(event);
        fSourceViewer.invalidateTextPresentation();
    }

    /**
     * Handle the given property change event
     *
     * @param event
     *            The event
     */
    private void handlePropertyChangeEvent(PropertyChangeEvent event) {
        if (fPreferenceStore == null) {
            return; // Uninstalled during event notification
        }

        if (fConfiguration != null) {
            fConfiguration.handlePropertyChangeEvent(event);
        }

        if (TypeScriptSemanticHighlightings.affectsEnablement(fPreferenceStore, event)) {
            if (isEnabled()) {
                enable();
            } else {
                disable();
            }
        }

        if (!isEnabled()) {
            return;
        }

        boolean refreshNeeded = false;

        for (int i = 0, n = fSemanticHighlightings.length; i < n; i++) {
            TypeScriptSemanticHighlighting semanticHighlighting = fSemanticHighlightings[i];

            String colorKey = TypeScriptSemanticHighlightings.getColorPreferenceKey(semanticHighlighting);
            if (colorKey.equals(event.getProperty())) {
                adaptToTextForegroundChange(fHighlightings[i], event);
                fPresenter.highlightingStyleChanged(fHighlightings[i]);
                refreshNeeded = true;
                continue;
            }

            String boldKey = TypeScriptSemanticHighlightings.getBoldPreferenceKey(semanticHighlighting);
            if (boldKey.equals(event.getProperty())) {
                adaptToTextStyleChange(fHighlightings[i], event, SWT.BOLD);
                fPresenter.highlightingStyleChanged(fHighlightings[i]);
                refreshNeeded = true;
                continue;
            }

            String italicKey = TypeScriptSemanticHighlightings.getItalicPreferenceKey(semanticHighlighting);
            if (italicKey.equals(event.getProperty())) {
                adaptToTextStyleChange(fHighlightings[i], event, SWT.ITALIC);
                fPresenter.highlightingStyleChanged(fHighlightings[i]);
                refreshNeeded = true;
                continue;
            }

            String enabledKey = TypeScriptSemanticHighlightings.getEnabledPreferenceKey(semanticHighlighting);
            if (enabledKey.equals(event.getProperty())) {
                adaptToEnablementChange(fHighlightings[i], event);
                fPresenter.highlightingStyleChanged(fHighlightings[i]);
                refreshNeeded = true;
                continue;
            }
        }

        if (refreshNeeded && fReconciler != null) {
            fReconciler.refresh();
        }
    }

    private void adaptToEnablementChange(Highlighting highlighting, PropertyChangeEvent event) {
        Object value = event.getNewValue();
        boolean eventValue;
        if (value instanceof Boolean) {
            eventValue = ((Boolean) value).booleanValue();
        } else if (IPreferenceStore.TRUE.equals(value)) {
            eventValue = true;
        } else {
            eventValue = false;
        }
        highlighting.setEnabled(eventValue);
    }

    private void adaptToTextForegroundChange(Highlighting highlighting, PropertyChangeEvent event) {
        RGB rgb = null;

        Object value = event.getNewValue();
        if (value instanceof RGB) {
            rgb = (RGB) value;
        } else if (value instanceof String) {
            rgb = StringConverter.asRGB((String) value);
        }

        if (rgb != null) {            
            Color color = fColorManager.getColor(rgb);
            TextAttribute oldAttr = highlighting.getTextAttribute();
            highlighting.setTextAttribute(new TextAttribute(color, oldAttr.getBackground(), oldAttr.getStyle()));
        }
    }

    private void adaptToTextStyleChange(Highlighting highlighting, PropertyChangeEvent event, int styleAttribute) {
        boolean eventValue = false;
        Object value = event.getNewValue();
        if (value instanceof Boolean) {
            eventValue = ((Boolean) value).booleanValue();
        } else if (IPreferenceStore.TRUE.equals(value)) {
            eventValue = true;
        }

        TextAttribute oldAttr = highlighting.getTextAttribute();
        boolean activeValue = (oldAttr.getStyle() & styleAttribute) == styleAttribute;

        if (activeValue != eventValue) {
            highlighting.setTextAttribute(new TextAttribute(oldAttr.getForeground(), oldAttr.getBackground(),
                    eventValue ? oldAttr.getStyle() | styleAttribute : oldAttr.getStyle() & ~styleAttribute));
        }
    }    

}

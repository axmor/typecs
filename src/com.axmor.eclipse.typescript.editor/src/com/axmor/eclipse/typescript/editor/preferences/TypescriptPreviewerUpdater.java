package com.axmor.eclipse.typescript.editor.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;

import com.axmor.eclipse.typescript.editor.TypeScriptEditorConfiguration;

public class TypescriptPreviewerUpdater {	

	public TypescriptPreviewerUpdater(final SourceViewer viewer, final TypeScriptEditorConfiguration configuration, final IPreferenceStore preferenceStore) {		
		final IPropertyChangeListener propertyChangeListener= new IPropertyChangeListener() {
			/*
			 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
			 */
			public void propertyChange(PropertyChangeEvent event) {
				configuration.adaptToPreferenceChange(event);
				viewer.invalidateTextPresentation();
			}
		};
		viewer.getTextWidget().addDisposeListener(new DisposeListener() {
			/*
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				preferenceStore.removePropertyChangeListener(propertyChangeListener);
			}
		});
		preferenceStore.addPropertyChangeListener(propertyChangeListener);
	}	
}
	

/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import com.axmor.eclipse.typescript.editor.TypeScriptEditor;
import com.axmor.eclipse.typescript.editor.TypeScriptUIImages;
import com.axmor.eclipse.typescript.editor.parser.TypeScriptImageKeys;

/**
 * @author Konstantin Zaitcev
 */
public class ToggleMarkOccurrencesAction extends TextEditorAction implements IPropertyChangeListener {
	public static final String TOGGLE_MARK_OCCURRENCE = "toggle_mark_occurrence";

	private IPreferenceStore fStore;

	/**
	 * Constructs and updates the action.
	 */
	public ToggleMarkOccurrencesAction() {
		super(Messages.getResourceBundle(), "ToggleMarkOccurrencesAction.", null, IAction.AS_CHECK_BOX);  //$NON-NLS-1$
		setImageDescriptor(TypeScriptUIImages.getImageDescriptor(TypeScriptImageKeys.IMG_TOGGLE_OCCURRENCE));
		setToolTipText(Messages.ToggleMarkOccurrencesAction_label); 
		update();
	}

	@Override
	public void run() {
		fStore.setValue(TOGGLE_MARK_OCCURRENCE, isChecked());
	}

	@Override
	public void update() {
		ITextEditor editor = getTextEditor();

		boolean checked = false;
		boolean enabled = false;
		if (editor instanceof TypeScriptEditor) {
			checked = ((TypeScriptEditor) editor).isMarkingOccurrences();
			enabled = true;
		}

		setChecked(checked);
		setEnabled(enabled);
	}

	@Override
	public void setEditor(ITextEditor editor) {

		super.setEditor(editor);

		if (editor != null) {
			if (fStore == null) {
				fStore = ((TypeScriptEditor) editor).getEditorPreferenceStore();
				fStore.addPropertyChangeListener(this);
			}

		} else if (fStore != null) {
			fStore.removePropertyChangeListener(this);
			fStore = null;
		}

		update();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(TOGGLE_MARK_OCCURRENCE))
			setChecked(Boolean.valueOf(event.getNewValue().toString()).booleanValue());
	}
}
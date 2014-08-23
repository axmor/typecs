package com.axmor.eclipse.typescript.editor;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;

import com.axmor.eclipse.typescript.editor.actions.ToggleMarkOccurrencesAction;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptEditorActionContributor extends
		BasicTextEditorActionContributor {

	private ToggleMarkOccurrencesAction fToggleMarkOccurrencesAction;

	public TypeScriptEditorActionContributor() {
		super();
		fToggleMarkOccurrencesAction = new ToggleMarkOccurrencesAction();
	}
	
	@Override
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);
		if (fToggleMarkOccurrencesAction != null) {
			fToggleMarkOccurrencesAction.setEditor((ITextEditor) part);
		} 
	}
	
	@Override
	public void init(IActionBars bars, IWorkbenchPage page) {
		super.init(bars, page);
		bars.setGlobalActionHandler("com.axmor.eclipse.typescript.toggleMarkOccurrences", fToggleMarkOccurrencesAction); 
	}
}

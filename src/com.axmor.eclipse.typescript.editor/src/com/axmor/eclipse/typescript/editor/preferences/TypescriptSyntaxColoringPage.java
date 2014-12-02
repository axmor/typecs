/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.axmor.eclipse.typescript.editor.preferences;

import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_BOLD_SUFFIX;
import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_BRACKETS;
import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_COMMENT;
import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_DEFAULT;
import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_ITALIC_SUFFIX;
import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_JAVA_DOC;
import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_KEYWORD;
import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_NUMBER;
import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_REFERENCE;
import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_STRING;

import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.model.WorkbenchViewerComparator;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import com.axmor.eclipse.typescript.editor.Activator;
import com.axmor.eclipse.typescript.editor.TypeScriptEditorConfiguration;
import com.axmor.eclipse.typescript.editor.TypeScriptEditorUtils;
import com.axmor.eclipse.typescript.editor.actions.Messages;
import com.axmor.eclipse.typescript.editor.parser.TypeScriptPartitionScanner;
import com.axmor.eclipse.typescript.editor.preferences.OverlayPreferenceStore.OverlayKey;

/**
 * @author Kudrin Pavel
 */
public class TypescriptSyntaxColoringPage extends PreferencePage implements IWorkbenchPreferencePage {
	
	/**
	 * Item in the highlighting color list. Copied from org.eclipse.ant.internal.ui.preferences.AntEditorPreferencePage.
	 * 
	 * @since 3.0
	 */
	private class HighlightingColorListItem {
		/** Display name */
		private String fDisplayName;
		/** Color preference key */
		private String fColorKey;
		/** Bold preference key */
		private String fBoldKey;
		/** Italic preference key */
		private String fItalicKey;
		/** Item color */
		private Color fItemColor;
		
		/**
		 * Initialize the item with the given values.
		 * 
		 * @param displayName the display name
		 * @param colorKey the color preference key
		 * @param boldKey the bold preference key
		 * @param italicKey the italic preference key
		 * @param itemColor the item color
		 */
		public HighlightingColorListItem(String displayName, String colorKey, String boldKey, String italicKey, Color itemColor) {
			fDisplayName= displayName;
			fColorKey= colorKey;
			fBoldKey= boldKey;
			fItalicKey= italicKey;
			fItemColor= itemColor;
		}
		
		/**
		 * @return the bold preference key
		 */
		public String getBoldKey() {
			return fBoldKey;
		}
		
		/**
		 * @return the bold preference key
		 */
		public String getItalicKey() {
			return fItalicKey;
		}
		
		/**
		 * @return the color preference key
		 */
		public String getColorKey() {
			return fColorKey;
		}
		
		/**
		 * @return the display name
		 */
		public String getDisplayName() {
			return fDisplayName;
		}
		
		/**
		 * @return the item color
		 */
		@SuppressWarnings("unused")
		public Color getItemColor() {
			return fItemColor;
		}
	}
	
	/**
	 * Color list label provider.
	 *
	 * @since 3.0
	 */
	private class ColorListLabelProvider extends LabelProvider {
		/*
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		@Override
		public String getText(Object element) {
			if (element instanceof String)
				return (String) element;
			return ((HighlightingColorListItem)element).getDisplayName();
		}
	}
	
	/**
	 * Color list content provider.
	 * 
	 * @since 3.0
	 */
	private class ColorListContentProvider implements IStructuredContentProvider {

		/*
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		@SuppressWarnings("rawtypes")
		public Object[] getElements(Object inputElement) {
			return ((java.util.List)inputElement).toArray();
		}

		/*
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			fOverlayStore.stop();			
		}

		/*
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	private Button fBoldCheckBox;
	private Button fItalicCheckBox;
	private ColorEditor fSyntaxForegroundColorEditor;
	private OverlayPreferenceStore fOverlayStore;
	
	private TableViewer fHighlightingColorListViewer;
	private SourceViewer fPreviewViewer;
	/**
	 * Highlighting color list
	 */
	private final java.util.List<HighlightingColorListItem> fHighlightingColorList= new ArrayList<HighlightingColorListItem>();
	
	/**
	 * The keys of the overlay store.
	 */ 
	private final String[][] fSyntaxColorListModel= new String[][] {
			{ Messages.TypescriptSyntaxColoringPage_default, TS_DEFAULT },
			{ Messages.TypescriptEditorPreferencePage_comments, TS_COMMENT },
			{ Messages.TypescriptEditorPreferencePage_references, TS_REFERENCE },
			{ Messages.TypescriptEditorPreferencePage_string, TS_STRING },
			{ Messages.TypescriptEditorPreferencePage_numbers, TS_NUMBER },
			{ Messages.TypescriptEditorPreferencePage_keywords, TS_KEYWORD },
			{ Messages.TypescriptEditorPreferencePage_java_doc, TS_JAVA_DOC },
			{ Messages.TypescriptEditorPreferencePage_brackets, TS_BRACKETS },
	};
	
	public TypescriptSyntaxColoringPage () {
		super();
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		for (int i= 0, n= fSyntaxColorListModel.length; i < n; i++) {
			fHighlightingColorList.add(
				new HighlightingColorListItem (fSyntaxColorListModel[i][0], fSyntaxColorListModel[i][1],
						fSyntaxColorListModel[i][1] + TS_BOLD_SUFFIX,
						fSyntaxColorListModel[i][1] + TS_ITALIC_SUFFIX, null));
		}
		fOverlayStore= createOverlayStore();
	}

	@Override
	public void init(IWorkbench arg0) {		
	}
	
	private OverlayPreferenceStore createOverlayStore() {
		ArrayList<OverlayKey> overlayKeys= new ArrayList<OverlayKey>();
		for (int i= 0; i < fHighlightingColorList.size(); i++) {
			HighlightingColorListItem item= fHighlightingColorList.get(i);
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, item.getColorKey()));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, item.getBoldKey()));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, item.getItalicKey()));
		}
		OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return new OverlayPreferenceStore(Activator.getDefault().getPreferenceStore(), keys);
	}

	@Override
	protected Control createContents(Composite parent) {
		fOverlayStore.load();
		fOverlayStore.start();
		Composite colorComposite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		colorComposite.setLayout(layout);
		
		Composite editorComposite= new Composite(colorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		GridData gd= new GridData(GridData.FILL_BOTH);
		editorComposite.setLayoutData(gd);

		fHighlightingColorListViewer = new TableViewer(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		fHighlightingColorListViewer.setLabelProvider(new ColorListLabelProvider());
		fHighlightingColorListViewer.setContentProvider(new ColorListContentProvider());
		fHighlightingColorListViewer.setComparator(new WorkbenchViewerComparator());
		gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint= convertHeightInCharsToPixels(5);
		fHighlightingColorListViewer.getControl().setLayoutData(gd);
		
		Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label label = new Label(stylesComposite, SWT.LEFT);
		label.setText("Color:");
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		label.setLayoutData(gd);
		
		fSyntaxForegroundColorEditor= new ColorEditor(stylesComposite);
		Button foregroundColorButton= fSyntaxForegroundColorEditor.getButton();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);
		
		fBoldCheckBox = new Button(stylesComposite, SWT.CHECK);
		fBoldCheckBox.setText("Bold");
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 2;
		fBoldCheckBox.setLayoutData(gd);
		
		fItalicCheckBox = new Button(stylesComposite, SWT.CHECK);
		fItalicCheckBox.setText("Italic");
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 2;
		fItalicCheckBox.setLayoutData(gd);
		
		Control previewer= createPreviewer(colorComposite);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint= convertWidthInCharsToPixels(20);
		previewer.setLayoutData(gd);
		
		fHighlightingColorListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSyntaxColorListSelection();
			}
		});
		
		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				HighlightingColorListItem item= getHighlightingColorListItem();
				PreferenceConverter.setValue(getOverlayStore(), item.getColorKey(), fSyntaxForegroundColorEditor.getColorValue());
			}
		});

		fBoldCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				HighlightingColorListItem item= getHighlightingColorListItem();
				getOverlayStore().setValue(item.getBoldKey(), fBoldCheckBox.getSelection());
			}
		});
				
		fItalicCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				HighlightingColorListItem item= getHighlightingColorListItem();
				getOverlayStore().setValue(item.getItalicKey(), fItalicCheckBox.getSelection());
			}
		});	
		initialize();

		return colorComposite;
	}
	
	public OverlayPreferenceStore getOverlayStore() {
		return fOverlayStore;
	}
	
	/**
	 * Returns the current highlighting color list item.
	 * 
	 * @return the current highlighting color list item
	 * @since 3.0
	 */
	private HighlightingColorListItem getHighlightingColorListItem() {
		IStructuredSelection selection= (IStructuredSelection) fHighlightingColorListViewer.getSelection();
		return (HighlightingColorListItem) selection.getFirstElement();
	}
	
	private void handleSyntaxColorListSelection() {
		HighlightingColorListItem item= getHighlightingColorListItem();
		RGB rgb= PreferenceConverter.getColor(fOverlayStore, item.getColorKey());
		fSyntaxForegroundColorEditor.setColorValue(rgb);
		fBoldCheckBox.setSelection(getOverlayStore().getBoolean(item.getBoldKey()));
		fItalicCheckBox.setSelection(getOverlayStore().getBoolean(item.getItalicKey()));
	}
	
	private void initialize() {				
		fHighlightingColorListViewer.setInput(fHighlightingColorList);
		fHighlightingColorListViewer.setSelection(new StructuredSelection(fHighlightingColorListViewer.getElementAt(0)));
	}
	
	@Override
	public boolean performOk() {		
		fOverlayStore.propagate();
		return super.performOk();
	}
	
	@Override
	protected void performDefaults() {
		getOverlayStore().loadDefaults();
		handleSyntaxColorListSelection();
		super.performDefaults();
	}
	
	private Control createPreviewer(Composite parent) {
		IPreferenceStore store= new ChainedPreferenceStore(new IPreferenceStore[] { getOverlayStore(), Activator.getDefault().getPreferenceStore() });
		fPreviewViewer = new SourceViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		
		fPreviewViewer.setEditable(false);
		Font font= JFaceResources.getFont(JFaceResources.TEXT_FONT);
		fPreviewViewer.getTextWidget().setFont(font);		
		
		String content= TypeScriptEditorUtils.loadPreviewContentFromFile(getClass().getResourceAsStream("SyntaxPreviewCode.txt")); //$NON-NLS-1$
		IDocument document = new Document(content);
		IDocumentPartitioner partitioner = new FastPartitioner(new TypeScriptPartitionScanner(),
                TypeScriptPartitionScanner.TS_PARTITION_TYPES);
        partitioner.connect(document);
        document.setDocumentPartitioner(partitioner);
		fPreviewViewer.setDocument(document);		
		
		TypeScriptEditorConfiguration configuration = new TypeScriptEditorConfiguration();	
		fPreviewViewer.configure(configuration);
		new TypescriptPreviewerUpdater(fPreviewViewer, configuration, store);
		
		return fPreviewViewer.getControl();
	}	
}

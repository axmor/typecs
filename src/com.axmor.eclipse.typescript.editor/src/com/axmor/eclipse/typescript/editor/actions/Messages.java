package com.axmor.eclipse.typescript.editor.actions;

import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.axmor.eclipse.typescript.editor.actions.messages"; //$NON-NLS-1$
	private static final ResourceBundle fgResourceBundle = ResourceBundle.getBundle(BUNDLE_NAME); 
	
	public static String ToggleMarkOccurrencesAction_label;
	public static String TypescriptEditorPreferencePage_references;
    public static String TypescriptEditorPreferencePage_keywords;
    public static String TypescriptEditorPreferencePage_string;
    public static String TypescriptEditorPreferencePage_numbers;
    public static String TypescriptEditorPreferencePage_comments;
    public static String TypescriptEditorPreferencePage_java_doc;
    public static String TypescriptEditorPreferencePage_brackets;
	public static String TypescriptSyntaxColoringPage_default;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

	public static ResourceBundle getResourceBundle() {
		return fgResourceBundle;
	}
}

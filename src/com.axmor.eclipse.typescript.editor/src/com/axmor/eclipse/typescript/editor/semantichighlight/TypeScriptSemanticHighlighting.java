package com.axmor.eclipse.typescript.editor.semantichighlight;

import org.eclipse.swt.graphics.RGB;

import us.monoid.json.JSONObject;

public abstract class TypeScriptSemanticHighlighting {	
	
	/**
	 * @return the preference key, will be augmented by a prefix and a suffix for each preference
	 */
	public abstract String getPreferenceKey();
	
	/**
     * @return the default default text color
     */
    public abstract RGB getDefaultTextColor();

    /**
     * @return <code>true</code> if the text attribute bold is set by default
     */
    public abstract boolean isBoldByDefault();

    /**
     * @return <code>true</code> if the text attribute italic is set by default
     */
    public abstract boolean isItalicByDefault();
    
    /**
     * @return <code>true</code> if the text attribute italic is enabled by default
     */
    public abstract boolean isEnabledByDefault();

	/**
	 * @return the display name
	 */
	public abstract String getDisplayName();		
	
	public abstract boolean consumes(JSONObject obj);
	
}

/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor.parser;

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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.NumberRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import com.axmor.eclipse.typescript.editor.Activator;
import com.axmor.eclipse.typescript.editor.color.ColorManager;


/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptSyntaxScanner extends RuleBasedScanner {
	/**
	 * Tokens store
	 */
	private Map<String, Token> tokens = new HashMap<>();
	
	private static String[] tokenProperties= {
		TS_DEFAULT,
		TS_REFERENCE,
		TS_KEYWORD,
		TS_STRING,
		TS_COMMENT,
		TS_NUMBER,
		TS_JAVA_DOC,
		TS_BRACKETS
	};	
	private String[] fPropertyNamesBold;
	private String[] fPropertyNamesItalic;

	/**
	 * A constructor
	 */
	public TypeScriptSyntaxScanner() {
		initTokensNames();
		ArrayList<IRule> rules = new ArrayList<>();
		
		//import reference
		rules.add(new SingleLineRule("///", "", getToken(TS_REFERENCE), '>', true));
		
		//keywords
		rules.add(new TypeScriptKeywordRuler(getToken(TS_KEYWORD)));

		// Add a rule for double quotes
		rules.add(new SingleLineRule("\"", "\"", getToken(TS_STRING), '\\'));
		// Add a rule for single quotes
		rules.add(new SingleLineRule("'", "'", getToken(TS_STRING), '\\'));
		// Add a rule for template string
		rules.add(new SingleLineRule("`", "`", getToken(TS_STRING), '\\'));
		
		//Add rules for brackets highlighting
		rules.add(new SymbolRule('\u007B', getToken(TS_BRACKETS)));
        rules.add(new SymbolRule('\u007D', getToken(TS_BRACKETS)));

        // Add a rule for numbers highlighting
        rules.add(new NumberRule(getToken(TS_NUMBER)));
        
		// comments
		rules.add(new MultiLineRule("/**", "*/", getToken(TS_JAVA_DOC)));
		rules.add(new MultiLineRule("/*", "*/", getToken(TS_COMMENT)));
		rules.add(new SingleLineRule("//", "", getToken(TS_COMMENT),  (char) 0, true));

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new TypeScriptWhitespaceDetector()));

		setRules(rules.toArray(new IRule[rules.size()]));
		setDefaultReturnToken(getToken(TS_DEFAULT));
	}

	/**
	 * Gets token on id
	 * 
	 * @param name id of token
	 * @return token
	 */
	private Token getToken(String name) {		
		return tokens.get(name);
	}
	
	private void addToken(String colorKey, String boldKey, String italicKey) {
		tokens.put(colorKey, new Token(createTextAttribute(colorKey, boldKey, italicKey)));
	}
	
	private int indexOf(String property) {
		if (property != null) {
			int length= tokenProperties.length;
			for (int i= 0; i < length; i++) {
				if (property.equals(tokenProperties[i]) || property.equals(fPropertyNamesBold[i]) || property.equals(fPropertyNamesItalic[i]))
					return i;
			}
		}
		return -1;
	}
	
	private void initTokensNames() {
		int length= tokenProperties.length;
		fPropertyNamesBold= new String[length];
		fPropertyNamesItalic= new String[length];
		for (int i= 0; i < length; i++) {
			fPropertyNamesBold[i]= tokenProperties[i] + TS_BOLD_SUFFIX;
			fPropertyNamesItalic[i]= tokenProperties[i] + TS_ITALIC_SUFFIX;
			addToken(tokenProperties[i], fPropertyNamesBold[i], fPropertyNamesItalic[i]);
		}		
	}
	
	/**
	 * Create a text attribute
	 */
	private TextAttribute createTextAttribute(String colorID, String boldKey, String italicKey) {
		Color color= null;		
		IPreferenceStore store= Activator.getDefault().getPreferenceStore();
		if (colorID != null) {
			color = ColorManager.getDefault().getColor(PreferenceConverter.getColor(store, colorID));
		}
		int style= store.getBoolean(boldKey) ? SWT.BOLD : SWT.NORMAL;
		if (store.getBoolean(italicKey)) {
			style |= SWT.ITALIC;
		}
		
		return new TextAttribute(color, null, style);
	}
	
	private void adaptToColorChange(PropertyChangeEvent event, Token token) {
		RGB rgb= null;
		
		Object value= event.getNewValue();
		if (value instanceof RGB) {
			rgb= (RGB) value;
		} else if (value instanceof String && !((String) value).isEmpty()) {
			rgb= StringConverter.asRGB((String) value);
		}
			
		if (rgb != null) {
			TextAttribute attr= (TextAttribute) token.getData();
			token.setData(new TextAttribute(ColorManager.getDefault().getColor(rgb), attr.getBackground(), attr.getStyle()));
		}
	}

	private void adaptToStyleChange(PropertyChangeEvent event, Token token, int styleAttribute) {
	 	if (token == null) {
			return;
		}
		boolean eventValue= false;
		Object value= event.getNewValue();
		if (value instanceof Boolean) {
			eventValue= ((Boolean) value).booleanValue();
		} else if (IPreferenceStore.TRUE.equals(value)) {
			eventValue= true;
		}
		
		TextAttribute attr= (TextAttribute) token.getData();
		boolean activeValue= (attr.getStyle() & styleAttribute) == styleAttribute;
		if (activeValue != eventValue) { 
			token.setData(new TextAttribute(attr.getForeground(), attr.getBackground(), eventValue ? attr.getStyle() | styleAttribute : attr.getStyle() & ~styleAttribute));
		}
	}	
	
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
    	String property= event.getProperty();
    	int index= indexOf(property);
    	if (property.endsWith(TS_BOLD_SUFFIX)) {
			adaptToStyleChange(event, getToken(tokenProperties[index]), SWT.BOLD);
		} else if (property.endsWith(TS_ITALIC_SUFFIX)) {
			adaptToStyleChange(event, getToken(tokenProperties[index]), SWT.ITALIC);
		} else if (index >= 0) {
			adaptToColorChange(event, getToken(tokenProperties[index]));
		}
    }
	
	public boolean affectsBehavior(PropertyChangeEvent event) {
        return indexOf(event.getProperty()) >= 0;
    }
}

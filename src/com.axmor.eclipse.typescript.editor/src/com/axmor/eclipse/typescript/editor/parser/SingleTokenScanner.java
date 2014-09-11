package com.axmor.eclipse.typescript.editor.parser;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.Token;

/**
 * 
 * @author Konstantin Zaitcev
 *
 */
public class SingleTokenScanner extends BufferedRuleBasedScanner {
	/**
	 * A constructor
	 * 
	 * @param attribute for default return token 
	 */
	public SingleTokenScanner(TextAttribute attribute) {
		setDefaultReturnToken(new Token(attribute));
	}
}

package com.axmor.eclipse.typescript.editor.parser;

import static com.axmor.eclipse.typescript.editor.color.TypeScriptColorRegistry.COLOR_REGISTRY;
import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.NumberRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.swt.SWT;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptSyntaxScanner extends RuleBasedScanner {
	/**
	 * Tokens store
	 */
	private Map<String, Token> tokens = new HashMap<>();

	/**
	 * A constructor
	 */
	public TypeScriptSyntaxScanner() {
		ArrayList<IRule> rules = new ArrayList<>();
		
		//import reference
		rules.add(new SingleLineRule("///", "", getToken(TS_REFERENCE), '>', true));
		
		//keywords
		rules.add(new TypeScriptKeywordRuler());

		// Add a rule for double quotes
		rules.add(new SingleLineRule("\"", "\"", getToken(TS_STRING), '\\'));
		// Add a rule for single quotes
		rules.add(new SingleLineRule("'", "'", getToken(TS_STRING), '\\'));
		
		//Add rules for brackets highlighting
		rules.add(new SymbolRule('\u007B', getToken(TS_BOLD)));
        rules.add(new SymbolRule('\u007D', getToken(TS_BOLD)));

        // Add a rule for numbers highlighting
        rules.add(new NumberRule(getToken(TS_NUMBER)));
        
		// comments
		rules.add(new MultiLineRule("/**", "*/", getToken(TS_JAVA_DOC)));
		rules.add(new MultiLineRule("/*", "*/", getToken(TS_COMMENT)));
		rules.add(new SingleLineRule("//", "", getToken(TS_COMMENT),  (char) 0, true));

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new TypeScriptWhitespaceDetector()));

		setRules((IRule[]) rules.toArray(new IRule[rules.size()]));
		setDefaultReturnToken(getToken(TS_DEFAULT));
	}

	/**
	 * Gets token on id
	 * 
	 * @param name id of token
	 * @return token
	 */
	private Token getToken(String name) {
		if (!tokens.containsKey(name)) {
	        if (name.equals(TS_BOLD)) {
	            tokens.put(name, new Token(new TextAttribute(COLOR_REGISTRY.get(TS_STRING),  null, SWT.BOLD)));
	            return tokens.get(name);
	        }
			tokens.put(name, new Token(new TextAttribute(COLOR_REGISTRY.get(name))));
		}
		return tokens.get(name);
	}
}

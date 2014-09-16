package com.axmor.eclipse.typescript.editor.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptPartitionScanner extends RuleBasedPartitionScanner {
	/** Doc id */
	public static final String TS_JAVA_DOC = "__ts_java_doc";
	
	/** Comment id */
	public static final String TS_COMMENT = "__ts_comment";
	
	/** Reference id */
	public static final String TS_REFERENCE = "__ts_reference";

	/**
	 * TypeScript partition types
	 */
	public static final String[] TS_PARTITION_TYPES = new String[] { 
			TS_JAVA_DOC,
			TS_COMMENT,
			TS_REFERENCE
			};

	/**
	 * A constructor
	 */
	public TypeScriptPartitionScanner() {
		IToken javaDoc = new Token(TS_JAVA_DOC);
		IToken comment = new Token(TS_COMMENT);

		List<IPredicateRule> rules = new ArrayList<>();

		// rule for reference
		rules.add(new EndOfLineRule("///", new Token(TS_REFERENCE)));

		// Add rule for single line comments.
		rules.add(new EndOfLineRule("//", comment));

		// Add rule for strings and character constants.
		rules.add(new SingleLineRule("\"", "\"", Token.UNDEFINED, '\\'));
		rules.add(new SingleLineRule("'", "'", Token.UNDEFINED, '\\'));

		// Add rules for multi-line comments and javadoc.
		rules.add(new MultiLineRule("/**", "*/", javaDoc, (char) 0, true));
		rules.add(new MultiLineRule("/*", "*/", comment, (char) 0, true));

		IPredicateRule[] result = new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);
	}
}

package com.axmor.eclipse.typescript.editor.parser;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptKeywordRuler extends WordRule {
    
	/**
	 * TypeScript keywords set
	 */
	public static final Set<String> KEYWORDS = new HashSet<String>(Arrays.asList(new String[] {
        "any", /* AnyKeyword */
        "bool", /* BoolKeyword */
        "boolean", /* BooleanKeyword */
        "break", /* BreakKeyword */
        "case", /* CaseKeyword */
        "catch", /* CatchKeyword */
        "class", /* ClassKeyword */
        "continue", /* ContinueKeyword */
        "const", /* ConstKeyword */
        "constructor", /* ConstructorKeyword */
        "debugger", /* DebuggerKeyword */
        "declare", /* DeclareKeyword */
        "default", /* DefaultKeyword */
        "delete", /* DeleteKeyword */
        "do", /* DoKeyword */
        "else", /* ElseKeyword */
        "enum", /* EnumKeyword */
        "export", /* ExportKeyword */
        "extends", /* ExtendsKeyword */
        "false", /* FalseKeyword */
        "finally", /* FinallyKeyword */
        "for", /* ForKeyword */
        "function", /* FunctionKeyword */
        "get", /* GetKeyword */
        "if", /* IfKeyword */
        "implements", /* ImplementsKeyword */
        "import", /* ImportKeyword */
        "in", /* InKeyword */
        "instanceof", /* InstanceOfKeyword */
        "interface", /* InterfaceKeyword */
        "let", /* LetKeyword */
        "module", /* ModuleKeyword */
        "new", /* NewKeyword */
        "null", /* NullKeyword */
        "number", /* NumberKeyword */
        "package", /* PackageKeyword */
        "private", /* PrivateKeyword */
//        "protected", /* ProtectedKeyword */ TypeScript hasn't protected modifier
        "public", /* PublicKeyword */
        "require", /* RequireKeyword */
        "return", /* ReturnKeyword */
        "set", /* SetKeyword */
        "static", /* StaticKeyword */
        "string", /* StringKeyword */
        "super", /* SuperKeyword */
        "switch", /* SwitchKeyword */
        "this", /* ThisKeyword */
        "throw", /* ThrowKeyword */
        "true", /* TrueKeyword */
        "try", /* TryKeyword */
        "typeof", /* TypeOfKeyword */
        "var", /* VarKeyword */
        "void", /* VoidKeyword */
        "while", /* WhileKeyword */
        "with", /* WithKeyword */
        "yield" /* YieldKeyword */
	}
	));
	
	/**
	 * A constructor which determine a word detector for keywords
	 */
	public TypeScriptKeywordRuler(Token token) {
		super(new IWordDetector() {
			@Override
			public boolean isWordStart(char c) {
				return Character.isJavaIdentifierStart(c);			
			}

			@Override
			public boolean isWordPart(char c) {
				return Character.isJavaIdentifierPart(c);
			}
		});
		for (String keyword : KEYWORDS) {
			addWord(keyword, token);
		}
	}	
	
	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		scanner.unread();
		int previous = scanner.read();
		if (Character.isJavaIdentifierPart((char) previous)) {
			return super.fDefaultToken;
		}
		else {
			return super.evaluate(scanner);
		}		
	}
}

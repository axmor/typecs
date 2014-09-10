package com.axmor.eclipse.typescript.editor.parser;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

/**
 * Class that helps to detect whitespaces
 * 
 * @author Konstantin Zaitcev
 */
public class TypeScriptWhitespaceDetector implements IWhitespaceDetector {
    @Override
	public boolean isWhitespace(char c) {
		return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
	}
}
